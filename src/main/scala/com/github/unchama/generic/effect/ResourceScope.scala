package com.github.unchama.generic.effect

import cats.Monad
import cats.data.OptionT
import cats.effect.{Async, Concurrent, Ref, Resource, Sync}
import com.github.unchama.generic.{ContextCoercion, OptionTExtra}

import scala.collection.immutable

/**
 * Spigotプラグインのようなユースケースでは、 ティックをまたぐような計算において任意のタイミングでサーバーが停止する可能性がある。 これにより、サーバー停止時の処理を適切に行うために
 * プラグインインスタンス等の場所から「確保したリソースすべてに対する参照を持つ」必要性が発生するケースがある。
 * 「あとで必ず破壊しなければいけない一時的に生成されたエンティティ」などはその最たる例である。
 *
 * `ResourceScope`はこのような状況に対応するためのオブジェクトのtraitである。
 *
 * cats-effectで提供される代数データ型である`Resource[F, R]`は、 `R` 型のリソースの確保・解放の`F`による計算の組を扱いやすいようにまとめた構造である。
 * `ResourceScope`オブジェクト`r`は、与えられたリソース `resource: Resource[F, R]` に対して、
 * 「`resource`により確保された資源を解放されるまで`r`の管理下に置く」ような 新たな `Resource` を `tracked` により生成する。
 *
 * `release` によりスコープ内で使用している資源を強制的に解放することができる。 `release`は、資源を使用している `Fiber` に対してキャンセルのシグナルを送り、
 * 資源が解放されるのを待ち続ける。これにより、スコープ内の資源が解放されるのを待つことができる。
 *
 * `use` に渡した計算の中で `release` をした際の動作は未定義となる。 実際、解放処理は資源の解放をawaitする、かつ資源を使用しているプログラムが
 * 解放処理を要求すると、解放に必要なキャンセル処理を受け付けなくなるため、 ハングすることが想定される。
 */
trait ResourceScope[ResourceUsageContext[_], DataAccessContext[_], ResourceHandler] {
  implicit val ResourceUsageContext: Monad[ResourceUsageContext]
  implicit val DataAccessContext: Monad[DataAccessContext]

  import cats.implicits._

  /**
   * 与えられた`resource`とそれを使用する関数`f`を引数に取る。 確保した資源が`use`によって使われている最中にこのスコープ下に置くような計算を返す。
   */
  def useTracked[R <: ResourceHandler, A](resource: Resource[ResourceUsageContext, R])(
    use: R => ResourceUsageContext[A]
  ): ResourceUsageContext[A]

  /**
   * 「与えられたハンドラがこのスコープの管理下にあるならば解放する計算を、そうでなければ空の値を返す」計算を返す。
   */
  def getCancelToken(
    handler: ResourceHandler
  ): DataAccessContext[Option[ResourceUsageContext[Unit]]]

  /**
   * 確保されている資源の集合の計算
   */
  val trackedHandlers: DataAccessContext[Set[ResourceHandler]]

  /**
   * 管理下にあるすべてのリソースを解放する計算
   */
  lazy val getReleaseAllAction: DataAccessContext[ResourceUsageContext[Unit]] = {
    for {
      handlersToRelease <- trackedHandlers
      releaseActions <- handlersToRelease.toList.traverse(getReleaseAction)
    } yield {
      releaseActions.sequence.as(())
    }
  }

  /**
   * 与えられたハンドラが管理下にあれば解放するような計算
   */
  def getReleaseAction(
    handler: ResourceHandler
  ): DataAccessContext[ResourceUsageContext[Unit]] = {
    for {
      optionToken <- getCancelToken(handler)
    } yield {
      optionToken.getOrElse(ResourceUsageContext.unit)
    }
  }

  /**
   * 与えられたハンドラがこのスコープの管理下にあるかどうかを判定する計算
   */
  def isTracked(handler: ResourceHandler): DataAccessContext[Boolean] =
    trackedHandlers.map(_.contains(handler))
}

object ResourceScope {

  /**
   * 新たな資源スコープを作成する。
   *
   * 参照透明性が無いためunsafe-接頭語がつけられている。 実際、`unsafeCreate` を二回呼んだ時には異なるスコープが作成される。
   *
   * @tparam F
   *   リソースを扱う計算
   * @tparam R
   *   リソースハンドラの型
   */
  def unsafeCreate[F[_]: Concurrent, G[_]: Sync: [f[_]] =>> ContextCoercion[f, F], R]
    : ResourceScope[F, G, R] = {
    new MultiDictResourceScope()
  }

  /**
   * 新たな資源スコープを作成する計算。
   */
  def create[F[_]: Async, G[_]: Sync: [f[_]] =>> ContextCoercion[f, F], R]
    : F[ResourceScope[F, G, R]] = {
    Sync[F].delay(unsafeCreate[F, G, R])
  }

  /**
   * 新たな資源スコープを作成する。 返される資源スコープ内では高々一つの資源しか確保されないことが保証される。
   *
   * 参照透明性が無いためunsafe-接頭語がつけられている。 実際、`unsafeCreateSingletonScope` を二回呼んだ時には異なるスコープが作成される。
   *
   * @tparam F
   *   リソースを扱う計算
   * @tparam R
   *   リソースハンドラの型
   */
  def unsafeCreateSingletonScope[F[_]: Concurrent, G[_]: Sync: [f[_]] =>> ContextCoercion[
    f,
    F
  ], R]: SingleResourceScope[F, G, R] = new SingleResourceScope()

  /**
   * `ResourceScope` の標準的な実装。
   */
  class MultiDictResourceScope[F[_], G[_], ResourceHandler] private[ResourceScope] (
    implicit val ResourceUsageContext: Concurrent[F],
    val DataAccessContext: Sync[G],
    contextCoercion: ContextCoercion[G, F]
  ) extends ResourceScope[F, G, ResourceHandler] {

    /**
     * この`Map`の終域にある`F[Unit]`は、
     *   - ハンドラを管理下から外し
     *   - ハンドラをリソースとして解放する 計算である。
     *
     * ここで、管理下から外すというのは、単にこの`Map`からハンドラを取り除く処理である。
     */
    private val handlerToCancelTokens: Ref[G, immutable.MultiDict[ResourceHandler, F[Unit]]] =
      Ref.unsafe(immutable.MultiDict.empty[ResourceHandler, F[Unit]])

    import ResourceUsageContext._
    import cats.implicits._

    override def useTracked[R <: ResourceHandler, A](
      resource: Resource[F, R]
    )(use: R => F[A]): F[A] = {
      for {
        allocated <- resource.allocated
        (handler, releaseResource) = allocated

        // use中に解放命令が入った時、useをキャンセルしforgetUsage >> releaseResourceを行うように
        a <- ConcurrentExtra.withSelfCancellation[F, A] { cancelToken =>
          val registerHandler = handlerToCancelTokens.update(_.add(handler, cancelToken))
          val forgetUsage = handlerToCancelTokens.update(_.remove(handler, cancelToken))

          guarantee(
            ContextCoercion(registerHandler) >> use(handler),
            ContextCoercion(forgetUsage) >> releaseResource
          )
        }
      } yield a
    }

    private def sequenceCancelToken(tokens: Iterable[F[Unit]]): F[Unit] = {
      tokens.toList.sequence.as(())
    }

    override def getCancelToken(handler: ResourceHandler): G[Option[F[Unit]]] =
      handlerToCancelTokens.get.map(dict => dict.sets.get(handler).map(sequenceCancelToken))

    override val trackedHandlers: G[Set[ResourceHandler]] =
      handlerToCancelTokens.get.map(_.keySet.toSet)

    override lazy val getReleaseAllAction: G[F[Unit]] = {
      for {
        mapping <- handlerToCancelTokens.get
      } yield {
        sequenceCancelToken(mapping.values)
      }
    }
  }

  class SingleResourceScope[F[_]: Concurrent, G[_], ResourceHandler] private[ResourceScope] (
    implicit override val DataAccessContext: Sync[G],
    contextCoercion: ContextCoercion[G, F]
  ) extends ResourceScope[[a] =>> OptionT[F, a], G, ResourceHandler] {

    type OptionF[a] = OptionT[F, a]

    val F: Concurrent[F] = implicitly

    override val ResourceUsageContext: Concurrent[OptionF] = implicitly

    /**
     * トップレベルのRefがリソースの「割り当て操作」に対応し、内側のRefが実際のリソースを持つ。
     *
     * 具体的には、新しくリソースを作成したいとき、
     *   - まずトップレベルのRefがNoneであることを確認してからSome(newRef)を入れ (ここでNoneではなかった場合リソース確保を失敗させる)
     *   - それが成功したらリソース確保を実行し、
     *   - newRefの中に確保したリソースを入れる
     *
     * という手順を踏む。
     */
    private val resourceSlot: Ref[G, Option[Ref[G, Option[(ResourceHandler, OptionF[Unit])]]]] =
      Ref.unsafe(None)

    import ContextCoercion._
    import cats.implicits._

    override def useTracked[R <: ResourceHandler, A](
      resource: Resource[OptionF, R]
    )(use: R => OptionF[A]): OptionF[A] = {
      for {

        /**
         * Refを作成し、それを `resourceSlot` に格納する試行をする。 試行が成功して初めてリソースの確保を行ってから、確保したリソースでRefを埋める。
         */
        newRef <- OptionT.liftF(
          Ref.of[G, Option[(ResourceHandler, OptionF[Unit])]](None).coerceTo[F]
        )

        // `resourceSlot` が空で`newPromise` の格納が成功した場合のみSome(true)が結果となる
        promiseAllocation <- OptionT.liftF(
          resourceSlot
            .tryModify {
              case None                => (Some(newRef), true)
              case allocated @ Some(_) => (allocated, false)
            }
            .coerceTo[F]
        )

        _ <- OptionTExtra.failIf[F](!promiseAllocation.contains(true))

        forgetUsage = OptionT.liftF(resourceSlot.set(None).coerceTo[F])

        // リソースの確保自体に失敗した場合は
        // `resourceSlot` を別のリソースの確保のために開ける。
        allocated <- resource
          .allocated(ResourceUsageContext)
          .orElse(forgetUsage *> OptionT.none)
        (handler, releaseResource) = allocated

        // use中に解放命令が入った時、useをキャンセルしforgetUsage >> releaseResourceを行うように
        a <- ConcurrentExtra.withSelfCancellation[OptionF, A] { cancelToken =>
          val registerHandler =
            OptionT.liftF(newRef.set(Some(handler, cancelToken)).coerceTo[F])

          ResourceUsageContext.guarantee(
            registerHandler >> use(handler),
            forgetUsage >> releaseResource
          )
        }
      } yield a
    }

    override def getCancelToken(handler: ResourceHandler): G[Option[OptionF[Unit]]] = {
      // 与えられたハンドラと同一のハンドラが管理下にある場合のみ解放するようなFを計算する

      val program: OptionT[G, OptionF[Unit]] = for {
        internalRef <- OptionT(resourceSlot.get)

        handlerTokenPair <- OptionT(internalRef.get)
        (acquiredHandler, release) = handlerTokenPair

        token <- OptionT.pure[G](release).filter(_ => handler == acquiredHandler)
      } yield token

      program.value
    }

    override val trackedHandlers: G[Set[ResourceHandler]] = for {
      promiseOption <- resourceSlot.get
      tracked <- promiseOption match {
        case Some(ref) =>
          ref.get.map {
            case Some((handler, _)) => Set(handler) // 確保済み
            case None               => Set.empty[ResourceHandler] // 確保が確定したが確保中
          }
        case None => Monad[G].pure(Set[ResourceHandler]()) // 確保予定のリソースが無い
      }
    } yield tracked

    def useTrackedForSome[R <: ResourceHandler, A](resource: Resource[F, R])(
      f: R => F[A]
    ): F[Option[A]] =
      useTracked(resource.mapK(OptionT.liftK[F]))(f.andThen(OptionT.liftF(_))).value

    override lazy val getReleaseAllAction: G[OptionF[Unit]] = {
      // 解放するリソースがあれば解放し、なければ何もしないようなFを計算する

      val getReleaseAction: OptionT[G, OptionF[Unit]] =
        for {
          internalRef <- OptionT(resourceSlot.get)

          handlerTokenPair <- OptionT(internalRef.get)
          (_, release) = handlerTokenPair
        } yield release

      val doNothing: OptionF[Unit] = OptionT.pure[F](())

      getReleaseAction.value.map(_.getOrElse(doNothing))
    }
  }
}

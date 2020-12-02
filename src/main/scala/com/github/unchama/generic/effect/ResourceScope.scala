package com.github.unchama.generic.effect

import cats.Monad
import cats.data.OptionT
import cats.effect.concurrent.{Deferred, Ref, TryableDeferred}
import cats.effect.{CancelToken, Concurrent, Resource, Sync}
import com.github.unchama.generic.{ContextCoercion, OptionTExtra}

import scala.collection.immutable

/**
 * Spigotプラグインのようなユースケースでは、
 * ティックをまたぐような計算において任意のタイミングでサーバーが停止する可能性がある。
 * これにより、サーバー停止時の処理を適切に行うために
 * プラグインインスタンス等の場所から「確保したリソースすべてに対する参照を持つ」必要性が発生するケースがある。
 * 「あとで必ず破壊しなければいけない一時的に生成されたエンティティ」などはその最たる例である。
 *
 * `ResourceScope`はこのような状況に対応するためのオブジェクトのtraitである。
 *
 * cats-effectで提供される代数データ型である`Resource[F, R]`は、
 * `R` 型のリソースの確保・解放の`F`による計算の組を扱いやすいようにまとめた構造である。
 * `ResourceScope`オブジェクト`r`は、与えられたリソース `resource: Resource[F, R]` に対して、
 * 「`resource`により確保された資源を解放されるまで`r`の管理下に置く」ような
 * 新たな `Resource` を `tracked` により生成する。
 *
 * `release` によりスコープ内で使用している資源を強制的に解放することができる。
 * `release`は、資源を使用している `Fiber` に対してキャンセルのシグナルを送り、
 * 資源が解放されるのを待ち続ける。これにより、スコープ内の資源が解放されるのを待つことができる。
 *
 * `use` に渡した計算の中で `release` をした際の動作は未定義となる。
 * 実際、解放処理は資源の解放をawaitする、かつ資源を使用しているプログラムが
 * 解放処理を要求すると、解放に必要なキャンセル処理を受け付けなくなるため、
 * ハングすることが想定される。
 */
trait ResourceScope[ResourceUsageContext[_], DataAccessContext[_], ResourceHandler] {
  implicit val ResourceUsageContext: Monad[ResourceUsageContext]
  implicit val DataAccessContext: Monad[DataAccessContext]

  import cats.implicits._

  /**
   * 与えられた`resource`とそれを使用する関数`f`を引数に取る。
   * 確保した資源が`use`によって使われている最中にこのスコープ下に置くような計算を返す。
   */
  def useTracked[R <: ResourceHandler, A](resource: Resource[ResourceUsageContext, R])
                                         (use: R => ResourceUsageContext[A]): ResourceUsageContext[A]

  /**
   * 「与えられたハンドラがこのスコープの管理下にあるならば解放する計算を、そうでなければ空の値を返す」計算を返す。
   */
  def getCancelToken(handler: ResourceHandler): DataAccessContext[Option[CancelToken[ResourceUsageContext]]]

  /**
   * 確保されている資源の集合の計算
   */
  val trackedHandlers: DataAccessContext[Set[ResourceHandler]]

  /**
   * 管理下にあるすべてのリソースを解放する計算
   */
  lazy val getReleaseAllAction: DataAccessContext[CancelToken[ResourceUsageContext]] = {
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
  def getReleaseAction(handler: ResourceHandler): DataAccessContext[CancelToken[ResourceUsageContext]] = {
    for {
      optionToken <- getCancelToken(handler)
    } yield {
      optionToken.getOrElse(ResourceUsageContext.unit)
    }
  }

  /**
   * 与えられたハンドラがこのスコープの管理下にあるかどうかを判定する計算
   */
  def isTracked(handler: ResourceHandler): DataAccessContext[Boolean] = trackedHandlers.map(_.contains(handler))
}

object ResourceScope {
  /**
   * 新たな資源スコープを作成する。
   *
   * 参照透明性が無いためunsafe-接頭語がつけられている。
   * 実際、`unsafeCreate` を二回呼んだ時には異なるスコープが作成される。
   *
   * @tparam F リソースを扱う計算
   * @tparam R リソースハンドラの型
   */
  def unsafeCreate[F[_] : Concurrent, G[_] : Sync : ContextCoercion[*[_], F], R]: ResourceScope[F, G, R] = {
    new MultiDictResourceScope()
  }

  /**
   * 新たな資源スコープを作成する計算。
   */
  def create[F[_] : Concurrent, G[_] : Sync : ContextCoercion[*[_], F], R]: F[ResourceScope[F, G, R]] = {
    Concurrent[F].delay(unsafeCreate[F, G, R])
  }

  /**
   * 新たな資源スコープを作成する。
   * 返される資源スコープ内では高々一つの資源しか確保されないことが保証される。
   *
   * 参照透明性が無いためunsafe-接頭語がつけられている。
   * 実際、`unsafeCreateSingletonScope` を二回呼んだ時には異なるスコープが作成される。
   *
   * @tparam F リソースを扱う計算
   * @tparam R リソースハンドラの型
   */
  def unsafeCreateSingletonScope[F[_]: Concurrent, R]: SingleResourceScope[F, R] = new SingleResourceScope()

  /**
   * `ResourceScope` の標準的な実装。
   */
  class MultiDictResourceScope[F[_], G[_], ResourceHandler] private[ResourceScope](implicit val ResourceUsageContext: Concurrent[F],
                                                                                   val DataAccessContext: Sync[G],
                                                                                   contextCoercion: ContextCoercion[G, F])
    extends ResourceScope[F, G, ResourceHandler] {

    /**
     * この`Map`の終域にある`CancelToken[F]`は、
     *  - ハンドラを管理下から外し
     *  - ハンドラをリソースとして解放する
     *    計算である。
     *
     * ここで、管理下から外すというのは、単にこの`Map`からハンドラを取り除く処理である。
     */
    private val handlerToCancelTokens: Ref[G, immutable.MultiDict[ResourceHandler, CancelToken[F]]] =
      Ref.unsafe(immutable.MultiDict.empty[ResourceHandler, CancelToken[F]])

    import ResourceUsageContext._
    import cats.implicits._

    override def useTracked[R <: ResourceHandler, A](resource: Resource[F, R])(use: R => F[A]): F[A] = {
      for {
        allocated <- resource.allocated
        (handler, releaseResource) = allocated

        // use中に解放命令が入った時、useをキャンセルしforgetUsage >> releaseResourceを行うように
        a <- ConcurrentExtra.withSelfCancellation[F, A] { cancelToken =>
          val registerHandler = handlerToCancelTokens.update(_.add(handler, cancelToken))
          val forgetUsage = handlerToCancelTokens.update(_.remove(handler, cancelToken))

          guarantee(ContextCoercion(registerHandler) >> use(handler))(ContextCoercion(forgetUsage) >> releaseResource)
        }
      } yield a
    }

    private def sequenceCancelToken(tokens: Iterable[CancelToken[F]]): CancelToken[F] = {
      tokens.toList.sequence.as(())
    }

    override def getCancelToken(handler: ResourceHandler): G[Option[CancelToken[F]]] =
      handlerToCancelTokens.get.map(dict =>
        dict.sets.get(handler).map(sequenceCancelToken)
      )

    override val trackedHandlers: G[Set[ResourceHandler]] =
      handlerToCancelTokens.get.map(_.keySet.toSet)

    override lazy val getReleaseAllAction: G[CancelToken[F]] = {
      for {
        mapping <- handlerToCancelTokens.get
      } yield {
        sequenceCancelToken(mapping.values)
      }
    }
  }

  class SingleResourceScope[F[_] : Concurrent, ResourceHandler] private[ResourceScope]() extends ResourceScope[OptionT[F, *], F, ResourceHandler] {
    type OptionF[a] = OptionT[F, a]

    val F: Concurrent[F] = implicitly

    override val ResourceUsageContext: Concurrent[OptionF] = implicitly
    override val DataAccessContext: Concurrent[F] = F

    private val promiseSlot: Ref[F, Option[TryableDeferred[F, (ResourceHandler, CancelToken[OptionF])]]] =
      Ref.unsafe(None)

    import cats.implicits._

    override def useTracked[R <: ResourceHandler, A](resource: Resource[OptionF, R])(use: R => OptionF[A]): OptionF[A] = {
      for {
        /**
         * Deferredプロミスを作成し、それを `promiseSlot` に格納する試行をする。
         * 試行が成功して初めてリソースの確保を行ってから、確保したリソースでプロミスを埋める。
         */
        newPromise <- OptionT.liftF(Deferred.tryableUncancelable[F, (ResourceHandler, CancelToken[OptionF])])

        // `promiseSlot` が空で`newPromise` の格納が成功した場合のみSome(true)が結果となる
        promiseAllocation <- OptionT.liftF(
          promiseSlot.tryModify {
            case None => (Some(newPromise), true)
            case allocated@Some(_) => (allocated, false)
          }
        )
        _ <- OptionTExtra.failIf[F](promiseAllocation.contains(false))

        forgetUsage = OptionT.liftF(promiseSlot.set(None))

        // リソースの確保自体に失敗した場合は
        // `promiseSlot` を別のリソースの確保のために開ける。
        allocated <- resource.allocated(ResourceUsageContext).orElse(forgetUsage *> OptionT.none)
        (handler, releaseResource) = allocated

        // use中に解放命令が入った時、useをキャンセルしforgetUsage >> releaseResourceを行うように
        a <- ConcurrentExtra.withSelfCancellation[OptionF, A] { cancelToken =>
          val registerHandler = OptionT.liftF(newPromise.complete((handler, cancelToken)))

          ResourceUsageContext.guarantee(registerHandler >> use(handler))(forgetUsage >> releaseResource)
        }
      } yield a
    }

    override def getCancelToken(handler: ResourceHandler): F[Option[CancelToken[OptionF]]] = {
      // 与えられたハンドラと同一のハンドラが管理下にある場合のみ解放するようなFを計算する

      val program: OptionT[F, CancelToken[OptionF]] = for {
        internalPromise <- OptionT(promiseSlot.get)

        handlerTokenPair <- OptionT(internalPromise.tryGet)
        (acquiredHandler, release) = handlerTokenPair

        token <- OptionT.pure[F](release).filter(_ => handler == acquiredHandler)
      } yield token

      program.value
    }

    override val trackedHandlers: F[Set[ResourceHandler]] = for {
      promiseOption <- promiseSlot.get
      tracked <- promiseOption match {
        case Some(promise) => promise.get.map(p => Set(p._1))
        case None => Monad[F].pure(Set[ResourceHandler]())
      }
    } yield tracked

    def useTrackedForSome[R <: ResourceHandler, A](resource: Resource[F, R])(f: R => F[A]): F[Option[A]] =
      useTracked(resource.mapK(OptionT.liftK[F]))(f.andThen(OptionT.liftF(_))).value

    override lazy val getReleaseAllAction: F[CancelToken[OptionF]] = {
      // 解放するリソースがあれば解放し、なければ何もしないようなFを計算する

      val releaseAction: OptionT[F, Unit] =
        for {
          internalPromise <- OptionT(promiseSlot.get)

          handlerTokenPair <- OptionT.liftF(internalPromise.get)
          (_, release) = handlerTokenPair

          _ <- release
        } yield ()

      releaseAction.value.as(ResourceUsageContext.unit)
    }
  }
}

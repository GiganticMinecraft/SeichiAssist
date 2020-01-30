package com.github.unchama.generic.effect

import cats.data.OptionT
import cats.effect.concurrent.{Deferred, Ref}
import cats.effect.{Async, CancelToken, Resource, Sync}
import com.github.unchama.generic.{OptionTExtra, ResourceExtra}

import scala.collection.concurrent.TrieMap

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
 * `R` 型のリソースの確保・開放の`F`による計算の組を扱いやすいようにまとめた構造である。
 * `ResourceScope`オブジェクト`r`は、与えられたリソース `resource: Resource[F, R]` に対して、
 * 「`resource`により確保された資源を開放されるまで`r`の管理下に置く」ような
 * 新たな `Resource` を `tracked` により生成する。
 */
trait ResourceScope[F[_], ResourceHandler] {
  implicit val syncF: Sync[F]

  import cats.implicits._

  /**
   * 与えられた`resource`に対して、確保時にこのスコープの管理下に置き、
   * 開放前に管理下から外してから開放するような新たなリソースを作成する。
   */
  def tracked[R <: ResourceHandler](resource: Resource[F, R]): Resource[F, R]

  /**
   * 与えられたハンドラがこのスコープの管理下にあるならば開放する計算を返し、そうでなければ空の値を返す計算
   */
  def getCancelToken(handler: ResourceHandler): F[Option[CancelToken[F]]]

  /**
   * 管理下にあるすべてのリソースを開放する計算
   */
  val releaseAll: CancelToken[F]

  /**
   * 与えられたハンドラが管理下にあれば開放するような計算
   */
  def release(handler: ResourceHandler): CancelToken[F] = for {
    optionToken <- getCancelToken(handler)
    _ <- optionToken.getOrElse(syncF.unit)
  } yield ()

  /**
   * とあるハンドラがこのオブジェクトの管理下にあるかどうかを判定する計算
   */
  def isTracked(handler: ResourceHandler): F[Boolean] = getCancelToken(handler).map(_.nonEmpty)
}

object ResourceScope {
  /**
   * 新たな資源スコープを作成する。
   *
   * インスタンス等価性により挙動が変わるオブジェクトを作成するのでunsafe-接頭語がつけられている。
   * @tparam F リソースを扱う計算
   * @tparam R リソースハンドラの型
   */
  def unsafeCreate[F[_]: Sync, R]: ResourceScope[F, R] = new TrieMapResourceScope()

  /**
   * 新たな資源スコープを作成する。
   * 返される資源スコープ内では高々一つの資源しか確保されないことが保証される。
   *
   * インスタンス等価性により挙動が変わるオブジェクトを作成するのでunsafe-接頭語がつけられている。
   * @tparam F リソースを扱う計算
   * @tparam R リソースハンドラの型
   */
  def unsafeCreateSingletonScope[F[_]: Async, R]: SingleResourceScope[F, R] = new SingleResourceScope()

  class TrieMapResourceScope[F[_], ResourceHandler] private[ResourceScope] (implicit val syncF: Sync[F])
    extends ResourceScope[F, ResourceHandler] {
    import scala.collection.mutable

    /**
     * リソースへのハンドラから`F[Unit]`の値への`Map`。
     *
     * この`Map`の終域にある`F[Unit]`は、原像のハンドラが指し示すリソースに対し
     * ・このオブジェクトの管理下から外す
     * ・リソースの意味で開放する
     * の順で処理をする計算である。
     * ここで、このオブジェクトの管理下から外すというのは、単にこの`Map`からハンドラを取り除く処理である。
     */
    private val trackedResources: mutable.Map[ResourceHandler, CancelToken[F]] = TrieMap()

    import cats.implicits._
    import syncF._

    override def tracked[R <: ResourceHandler](resource: Resource[F, R]): Resource[F, R] = {
      /*
       * アイデアとしては、resourceで確保したものをtrackedResourcesに開放トークンとともに格納し、
       * 開放時に自動的にtrackedResourcesから削除されるようなリソースを定義すれば良い。
       *
       * 意図的にリソースハンドラの参照をこちら側へ「漏らす」ために`Resource[F, R].allocated`を使用している。
       */
      val trackedResource =
        for {
          allocated <- resource.allocated
          (handler, releaseToken) = allocated

          dismiss = defer { trackedResources.remove(handler).getOrElse(Sync[F].unit) }

          dismissAndRelease = dismiss *> releaseToken

          register = delay { trackedResources += (handler -> dismissAndRelease) }

          // 二重確保を避けるため確保されていたリソースがあれば開放してから確保する
          _ <- dismiss *> register
        } yield (handler, dismissAndRelease)

      Resource(trackedResource)
    }

    override def getCancelToken(handler: ResourceHandler): F[Option[CancelToken[F]]] =
      delay { trackedResources.get(handler) }

    override val releaseAll: CancelToken[F] = trackedResources.values.toList.sequence.map(_ => ())
  }

  class SingleResourceScope[F[_]: Async, ResourceHandler] private[ResourceScope]() extends ResourceScope[OptionT[F, *], ResourceHandler] {
    type OptionF[a] = OptionT[F, a]

    override implicit val syncF: Async[OptionF] = implicitly

    private val promiseSlot: Ref[F, Option[Deferred[F, (ResourceHandler, CancelToken[OptionF])]]] =
      Ref.unsafe(None)

    import cats.implicits._

    override def tracked[R <: ResourceHandler](resource: Resource[OptionF, R]): Resource[OptionF, R] = {
      /**
       * Deferredプロミスを作成し、それを `promiseSlot` に格納する試行をし、
       * 試行が成功して初めてリソースの確保を行ってから確保したリソースでプロミスを埋める。
       *
       * 開放時には、`promiseSlot` を空にしてから資源をすぐに開放する。
       * これにより、資源が確保されてから開放される間は常にこのスコープの管理下に置かれる。
       */
      val trackedResource: OptionF[(R, CancelToken[OptionF])] = for {
        newPromise <- OptionT.liftF(Deferred.uncancelable[F, (ResourceHandler, CancelToken[OptionF])])

        // `newPromise` の `promiseSlot` への格納が成功した場合のみSome(true)が結果となる
        promiseAllocation <- OptionT.liftF(
          promiseSlot.tryModify {
            case None => (Some(newPromise), true)
            case allocated@Some(_) => (allocated, false)
          }
        )
        _ <- OptionTExtra.failIf[F](promiseAllocation.contains(true))

        setSlotToNone = OptionT.liftF(promiseSlot.set(None))

        // リソースの確保自体に失敗した場合は
        // `promiseSlot` を別のリソースの確保のために開ける。
        allocated <- resource.allocated.orElse(setSlotToNone *> OptionT.none)
        (handler, releaseToken) = allocated

        _ <- OptionT.liftF(newPromise.complete(allocated))
      } yield (handler, setSlotToNone *> releaseToken)

      Resource(trackedResource)
    }

    override def getCancelToken(handler: ResourceHandler): OptionF[Option[CancelToken[OptionF]]] =
      // 与えられたハンドラと同一のハンドラが管理下にある場合のみ開放するようなFを計算する
      for {
        internalPromise <- OptionT(promiseSlot.get)

        handlerTokenPair <- OptionT.liftF(internalPromise.get)
        (acquiredHandler, release) = handlerTokenPair

        token = if (handler == acquiredHandler) Some(release) else None
      } yield token

    override val releaseAll: CancelToken[OptionF] =
      // 解放するリソースがあれば解放し、なければ何もしないようなFを計算する
      for {
        internalPromise <- OptionT(promiseSlot.get)

        handlerTokenPair <- OptionT.liftF(internalPromise.get)
        (_, release) = handlerTokenPair

        _ <- release
      } yield ()

    def trackedForSome[R <: ResourceHandler](resource: Resource[F, R]): Resource[F, Option[R]] =
      ResourceExtra.unwrapOptionTResource(tracked(resource.mapK(OptionT.liftK)))
  }
}

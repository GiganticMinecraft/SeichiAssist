package com.github.unchama.generic.effect

import cats.Monad
import cats.data.OptionT
import cats.effect.concurrent.{Deferred, Ref, TryableDeferred}
import cats.effect.{CancelToken, Concurrent, Resource}
import com.github.unchama.generic.OptionTExtra

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
 *
 * `release` によりスコープ内で使用している資源を強制的に開放することができる。
 * `release`は、資源を使用している `Fiber` に対してキャンセルのシグナルを送り、
 * 資源が開放されるのを待ち続ける。これにより、スコープ内の資源が開放されるのを待つことができる。
 *
 * `use` に渡した計算の中で `release` をした際の動作は未定義となる。
 */
trait ResourceScope[F[_], ResourceHandler] {
  implicit val monadF: Monad[F]

  import cats.implicits._

  /**
   * 与えられた`resource`とそれを使用する関数`f`に対して、
   * 確保した資源の`f`による使用中にこのスコープ下に資源を置くような計算を返す。
   */
  def useTracked[R <: ResourceHandler, A](resource: Resource[F, R])(f: R => F[A]): F[A]

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
    _ <- optionToken.getOrElse(monadF.unit)
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
  def unsafeCreate[F[_]: Concurrent, R]: ResourceScope[F, R] = new TrieMapResourceScope()

  /**
   * 新たな資源スコープを作成する。
   * 返される資源スコープ内では高々一つの資源しか確保されないことが保証される。
   *
   * インスタンス等価性により挙動が変わるオブジェクトを作成するのでunsafe-接頭語がつけられている。
   * @tparam F リソースを扱う計算
   * @tparam R リソースハンドラの型
   */
  def unsafeCreateSingletonScope[F[_]: Concurrent, R]: SingleResourceScope[F, R] = new SingleResourceScope()

  class TrieMapResourceScope[F[_], ResourceHandler] private[ResourceScope] (implicit val monadF: Concurrent[F])
    extends ResourceScope[F, ResourceHandler] {
    import scala.collection.mutable

    /**
     * この`Map`の終域にある`CancelToken[F]`は、
     *  - ハンドラを管理下から外し
     *  - ハンドラをリソースとして開放する
     * 計算である。
     *
     * ここで、管理下から外すというのは、単にこの`Map`からハンドラを取り除く処理である。
     */
    private val usageCancellationMap: mutable.Map[ResourceHandler, CancelToken[F]] = TrieMap()

    import cats.implicits._
    import monadF._

    override def useTracked[R <: ResourceHandler, A](resource: Resource[F, R])(use: R => F[A]): F[A] = {
      for {
        allocated <- resource.allocated
        (handler, releaseResource) = allocated

        forgetUsage = delay { usageCancellationMap.remove(handler) }

        // こちら側からuse実行側でregisterHandlerする際、キャンセルトークンを共有する必要がある
        cancelTokenPromise <- Deferred[F, CancelToken[F]]

        registerHandler = for {
          // こちら側からのcompleteでFiberのキャンセルトークンが埋め込まれるまでawaitする
          cancelToken <- cancelTokenPromise.get
          _ <- delay { usageCancellationMap += (handler -> cancelToken) }
        } yield ()

        // 二重確保を避けるため確保されていたリソースがあれば開放してから確保する
        _ <- release(handler)

        usageFiber <- start(
          // 途中でキャンセルされても管理下から外しリソースの解放は行う
          guarantee(registerHandler >> use(handler))(forgetUsage >> releaseResource)
        )
        // completeすることでusageFiberが動き出す
        _ <- cancelTokenPromise.complete(usageFiber.cancel)
        a <- usageFiber.join
      } yield a
    }

    override def getCancelToken(handler: ResourceHandler): F[Option[CancelToken[F]]] =
      delay { usageCancellationMap.get(handler) }

    override val releaseAll: CancelToken[F] =
      defer { usageCancellationMap.values.toList.sequence.as(()) }
  }

  class SingleResourceScope[F[_]: Concurrent, ResourceHandler] private[ResourceScope]() extends ResourceScope[OptionT[F, *], ResourceHandler] {
    type OptionF[a] = OptionT[F, a]

    override val monadF: Concurrent[OptionF] = implicitly
    val concF: Concurrent[F] = implicitly

    private val promiseSlot: Ref[F, Option[TryableDeferred[F, (ResourceHandler, CancelToken[OptionF])]]] =
      Ref.unsafe(None)

    import cats.implicits._

    override def useTracked[R <: ResourceHandler, A](resource: Resource[OptionF, R])(use: R => OptionF[A]): OptionF[A] = {
      /**
       * Deferredプロミスを作成し、それを `promiseSlot` に格納する試行をし、
       * 試行が成功して初めてリソースの確保を行ってから確保したリソースでプロミスを埋める。
       *
       * 開放時には、`promiseSlot` を空にしてから資源をすぐに開放する。
       * これにより、資源が確保されてから開放される間は常にこのスコープの管理下に置かれる。
       */
      for {
        newPromise <- OptionT.liftF(Deferred.tryableUncancelable[F, (ResourceHandler, CancelToken[OptionF])])

        // `newPromise` の `promiseSlot` への格納が成功した場合のみSome(true)が結果となる
        promiseAllocation <- OptionT.liftF(
          promiseSlot.tryModify {
            case None => (Some(newPromise), true)
            case allocated@Some(_) => (allocated, false)
          }
        )
        _ <- OptionTExtra.failIf[F](promiseAllocation.contains(false))

        forgetUsage = OptionT.liftF(promiseSlot.set(None))

        // こちら側からuse実行側でregisterHandlerする際、キャンセルトークンを共有する必要がある
        cancelTokenPromise <- Deferred[OptionF, CancelToken[OptionF]]

        // リソースの確保自体に失敗した場合は
        // `promiseSlot` を別のリソースの確保のために開ける。
        allocated <- resource.allocated(monadF).orElse(forgetUsage *> OptionT.none)
        (handler, releaseResource) = allocated

        registerHandler = for {
          // こちら側からのcompleteでFiberのキャンセルトークンが埋め込まれるまでawaitする
          cancelToken <- cancelTokenPromise.get
          _ <- OptionT.liftF(newPromise.complete((handler, cancelToken)))
        } yield ()

        usageFiber <- monadF.start(
          // 途中でキャンセルされても管理下から外しリソースの解放は行う
          monadF.guarantee(registerHandler >> use(handler))(forgetUsage >> releaseResource)
        )
        _ <- cancelTokenPromise.complete(usageFiber.cancel)
        a <- usageFiber.join
      } yield a
    }

    override def getCancelToken(handler: ResourceHandler): OptionF[Option[CancelToken[OptionF]]] =
      OptionT.liftF(getCancelTokenUnlifted(handler))

    def getCancelTokenUnlifted(handler: ResourceHandler): F[Option[CancelToken[OptionF]]] =
    // 与えられたハンドラと同一のハンドラが管理下にある場合のみ開放するようなFを計算する
      {
        for {
          internalPromise <- OptionT(promiseSlot.get)

          handlerTokenPair <- OptionT(internalPromise.tryGet)
          (acquiredHandler, release) = handlerTokenPair

          token <- OptionT.pure[F](release).filter(_ => handler == acquiredHandler)
        } yield token
      }.value

    def isTrackedUnlifted(handler: ResourceHandler): F[Boolean] = getCancelTokenUnlifted(handler).map(_.nonEmpty)

    def releaseSome(handler: ResourceHandler): CancelToken[F] = release(handler).value.as(())

    override val releaseAll: CancelToken[OptionF] =
      // 解放するリソースがあれば解放し、なければ何もしないようなFを計算する
      for {
        internalPromise <- OptionT(promiseSlot.get)

        handlerTokenPair <- OptionT.liftF(internalPromise.get)
        (_, release) = handlerTokenPair

        _ <- release
      } yield ()

    def useTrackedForSome[R <: ResourceHandler, A](resource: Resource[F, R])(f: R => F[A]): F[Option[A]] =
      useTracked(resource.mapK(OptionT.liftK[F]))(f.andThen(OptionT.liftF(_))).value
  }
}

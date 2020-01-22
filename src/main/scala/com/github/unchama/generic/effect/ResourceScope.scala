package com.github.unchama.generic.effect

import cats.effect.{CancelToken, Resource, Sync}

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
trait ResourceScope[ResourceHandler, F[_]] {
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
   * @tparam R リソースハンドラの型
   * @tparam F リソースを扱う計算
   */
  def unsafeCreate[R, F[_]: Sync]: ResourceScope[R, F] = new TrieMapResourceScope()

  class TrieMapResourceScope[ResourceHandler, F[_]](implicit val syncF: Sync[F]) extends ResourceScope[ResourceHandler, F] {
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
}

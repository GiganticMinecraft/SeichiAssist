package com.github.unchama.generic.effect

import cats.effect.{CancelToken, Resource, Sync}

import scala.collection.concurrent.TrieMap

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
   * 与えられたハンドラが管理下にあれば開放するような計算
   */
  def release(handler: ResourceHandler): CancelToken[F] = for {
    optionToken <- getCancelToken(handler)
    _ <- optionToken.getOrElse(syncF.unit)
  } yield ()

  /**
   * 管理下にあるすべてのリソースを開放する計算
   */
  val releaseAll: CancelToken[F]

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

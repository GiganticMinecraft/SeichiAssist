package com.github.unchama.generic.effect

import cats.effect.{CancelToken, Resource, Sync}

import scala.collection.concurrent.TrieMap

trait ResourceScope[ResourceHandler, F[_]] {
  implicit val syncF: Sync[F]

  import cats.implicits._

  /**
   * 与えられた`resource`の確保を行い、
   * 得られたリソースとその開放を行う計算の組を返す(c.f. [[Resource.allocated]])。
   *
   * ここで確保したリソースは必ずしも開放されるとは限らない。
   * 具体的には、この関数より返される開放の計算を走らせるか、
   * [[releaseAll]]を走らせるかを行わない限りリソースは開放されずに残り続ける。
   */
  def allocateAndTrack(resource: Resource[F, ResourceHandler]): F[CancelToken[F]]

  /**
   * 与えられたハンドラがこのスコープの管理下にあるならば開放する計算を返し、そうでなければ空の値を返す計算
   */
  def getCancelToken(handler: ResourceHandler): F[Option[CancelToken[F]]]

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
  def unsafe[R, F[_]: Sync]: ResourceScope[R, F] = new TrieMapResourceScope()

  class TrieMapResourceScope[ResourceHandler, F[_]](implicit val syncF: Sync[F]) extends ResourceScope[ResourceHandler, F] {
    import scala.collection.mutable

    /**
     * リソースへのハンドラから`F[Unit]`の値への`Map`。
     *
     * この`Map`の終域にある`F[Unit]`は、原像のハンドラが指し示すリソースに対し
     * ・リソースの意味で開放する
     * ・このオブジェクトの管理下から外す
     * の順で処理をする計算である。
     * ここで、このオブジェクトの管理下から外すというのは、単にこの`Map`からハンドラを取り除く処理である。
     */
    private val trackedResources: mutable.Map[ResourceHandler, CancelToken[F]] = TrieMap()

    import cats.implicits._
    import syncF._

    override def allocateAndTrack(resource: Resource[F, ResourceHandler]): F[CancelToken[F]] =
      for {
        allocated <- resource.allocated
        (handler, releaseToken) = allocated

        releaseAndDismiss = delay { trackedResources.remove(handler) } *> releaseToken

        _ <- delay { trackedResources += (handler -> releaseAndDismiss) }
      } yield releaseAndDismiss

    override def getCancelToken(handler: ResourceHandler): F[Option[CancelToken[F]]] =
      delay { trackedResources.get(handler) }

    override val releaseAll: CancelToken[F] = trackedResources.values.toList.sequence.map(_ => ())
  }
}

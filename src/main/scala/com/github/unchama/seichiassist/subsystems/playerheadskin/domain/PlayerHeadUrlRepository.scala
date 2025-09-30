package com.github.unchama.seichiassist.subsystems.playerheadskin.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.all._

import scala.collection.immutable.Queue

/**
 * LRUキャッシュを使用したプレイヤーヘッドURLのリポジトリ
 *
 * @param maxCacheSize キャッシュの最大サイズ（デフォルト: 500エントリ）
 */
class PlayerHeadUrlRepository[F[_]: Sync](maxCacheSize: Int = 500)(
  implicit fetcher: PlayerHeadSkinUrlFetcher[F]
) {

  /**
   * LRUキャッシュのデータ構造
   * @param cache プレイヤー名からHeadSkinUrlへのマップ
   * @param accessOrder アクセス順序を記録するキュー（最近アクセスされたものが後ろ）
   */
  private case class LRUCache(cache: Map[String, HeadSkinUrl], accessOrder: Queue[String]) {
    def get(key: String): Option[HeadSkinUrl] = cache.get(key)

    def put(key: String, value: HeadSkinUrl): LRUCache = {
      val newAccessOrder = accessOrder.filterNot(_ == key).enqueue(key)

      if (newAccessOrder.size > maxCacheSize) {
        val (evictedKey, remainingOrder) = newAccessOrder.dequeue
        val newCache = (cache - evictedKey).updated(key, value)
        LRUCache(newCache, remainingOrder)
      } else {
        val newCache = cache.updated(key, value)
        LRUCache(newCache, newAccessOrder)
      }
    }

    def updateAccess(key: String): LRUCache = {
      if (cache.contains(key)) {
        val newAccessOrder = accessOrder.filterNot(_ == key).enqueue(key)
        LRUCache(cache, newAccessOrder)
      } else {
        this
      }
    }
  }

  private val skinUrlLRUCache: Ref[F, LRUCache] =
    Ref.unsafe(LRUCache(Map.empty, Queue.empty))

  def readUrl(targetPlayer: String): F[Option[HeadSkinUrl]] = {
    skinUrlLRUCache.get.flatMap { cache =>
      cache.get(targetPlayer) match {
        case Some(url) =>
          // キャッシュヒット: アクセス順序を更新
          skinUrlLRUCache.update(_.updateAccess(targetPlayer)).map(_ => Some(url))
        case None =>
          // キャッシュミス: 新規取得
          fetcher.fetchHeadSkinUrl(targetPlayer).flatMap {
            case Some(url) =>
              skinUrlLRUCache.update(_.put(targetPlayer, url)).map(_ => Some(url))
            case None =>
              Sync[F].pure(None)
          }
      }
    }
  }

}

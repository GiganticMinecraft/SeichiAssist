package com.github.unchama.seichiassist.subsystems.playerheadskin.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.all._
import com.github.unchama.generic.ApplicativeExtra

class PlayerHeadUrlRepository[F[_]: Sync](implicit fetcher: PlayerHeadSkinUrlFetcher[F]) {

  // NOTE: 一日で再起動される前提ならまぁ良いが、LRU cache にした方がベターではある
  private val skinUrlUnboundedCache: Ref[F, Map[ /*targetPlayer*/ String, HeadSkinUrl]] =
    Ref.unsafe(Map.empty)

  def readUrl(targetPlayer: String): F[Option[HeadSkinUrl]] = for {
    cache <- skinUrlUnboundedCache.get
    skinUrlOpt <- ApplicativeExtra.optionOrElseA(
      cache.get(targetPlayer),
      fetcher.fetchHeadSkinUrl(targetPlayer)
    )
    _ <- skinUrlOpt.traverse(url => skinUrlUnboundedCache.update(_.updated(targetPlayer, url)))
  } yield skinUrlOpt

}

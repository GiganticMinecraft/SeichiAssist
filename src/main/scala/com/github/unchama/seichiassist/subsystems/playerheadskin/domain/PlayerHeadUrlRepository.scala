package com.github.unchama.seichiassist.subsystems.playerheadskin.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.generic.ApplicativeExtra

class PlayerHeadUrlRepository[F[_]: Sync](implicit fetcher: PlayerHeadSkinUrlFetcher[F]) {

  private val skinUrls: Ref[F, Vector[HeadSkinUrl]] = Ref.unsafe(Vector.empty)

  import cats.implicits._

  def readUrl(targetPlayer: String): F[Option[HeadSkinUrl]] = for {
    urls <- skinUrls.get
    targetPlayersHeadSkinUrl <- ApplicativeExtra.whenAOrElse(
      !urls.exists(_.playerName == targetPlayer)
    )(fetcher.fetchHeadSkinUrl(targetPlayer), None)
    _ <- skinUrls
      .update(_ :+ targetPlayersHeadSkinUrl.get)
      .whenA(targetPlayersHeadSkinUrl.isDefined)
    resultUrls <- skinUrls.get
  } yield resultUrls.find(_.playerName == targetPlayer)

}

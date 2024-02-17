package com.github.unchama.seichiassist.subsystems.playerheadskin.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class PlayerHeadUrlRepository[F[_]: Sync](headSkinUrl: Option[HeadSkinUrl]) {

  private val skinUrl: Ref[F, Option[HeadSkinUrl]] = Ref.unsafe(headSkinUrl)

  def readUrl: F[Option[HeadSkinUrl]] = skinUrl.get

}

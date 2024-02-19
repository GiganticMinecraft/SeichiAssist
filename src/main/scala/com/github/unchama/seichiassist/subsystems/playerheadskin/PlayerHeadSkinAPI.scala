package com.github.unchama.seichiassist.subsystems.playerheadskin

import com.github.unchama.seichiassist.subsystems.playerheadskin.domain.HeadSkinUrl

import java.util.UUID

trait PlayerHeadSkinAPI[F[_], Player] {

  def playerHeadSkinUrlByUUID(player: UUID): F[Option[HeadSkinUrl]]

}

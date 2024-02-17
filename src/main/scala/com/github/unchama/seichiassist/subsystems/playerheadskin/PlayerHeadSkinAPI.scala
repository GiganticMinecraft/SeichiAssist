package com.github.unchama.seichiassist.subsystems.playerheadskin

import com.github.unchama.seichiassist.subsystems.playerheadskin.domain.HeadSkinUrl

trait PlayerHeadSkinAPI[F[_], Player] {

  def playerHeadSkinUrl(player: Player): F[Option[HeadSkinUrl]]

}

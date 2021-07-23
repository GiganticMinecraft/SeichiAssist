package com.github.unchama.seichiassist.subsystems.gacha.domain.actions

import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizeTemplate

/**
 * ガチャ景品テンプレートを実体化し、プレーヤーに景品を付与する操作。
 */
trait GrantGachaPrize[F[_], Player, IS] {

  def to(player: Player)(template: GachaPrizeTemplate[IS]): F[Unit]

}

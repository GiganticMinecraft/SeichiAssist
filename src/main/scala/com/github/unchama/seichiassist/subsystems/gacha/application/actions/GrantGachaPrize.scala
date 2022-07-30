package com.github.unchama.seichiassist.subsystems.gacha.application.actions

import com.github.unchama.seichiassist.subsystems.gacha.domain.GrantState
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

trait GrantGachaPrize[F[_]] {

  /**
   * GachaPrizeをPlayerに付与します。
   * まずMineStackに入るかどうか検証し、
   * 入らなければプレイヤーに直接付与します
   */
  def grantGachaPrize(player: Player): F[GrantState]

  def createNewItem(owner: Option[String]): F[ItemStack]

}

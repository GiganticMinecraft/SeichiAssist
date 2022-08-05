package com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.domain.{GachaPrize, GrantState}
import com.github.unchama.seichiassist.util.{BreakUtil, InventoryOperations}
import com.github.unchama.util.bukkit.ItemStackUtil.appendOwnerInformation
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BukkitGrantGachaPrize[F[_]: Sync](gachaPrize: GachaPrize[ItemStack])
    extends GrantGachaPrize[F, ItemStack] {

  import cats.implicits._

  /**
   * GachaPrizeをPlayerに付与します。
   * まずMineStackに入るかどうか検証し、
   * 入らなければプレイヤーに直接付与します
   */
  def grantGachaPrize(player: Player): F[GrantState] = for {
    item <- createNewItem(Some(player.getName))
  } yield {
    if (BreakUtil.tryAddItemIntoMineStack(player, item)) {
      GrantState.grantedMineStack
    } else if (!InventoryOperations.isPlayerInventoryFull(player)) {
      GrantState.addedInventory
    } else {
      GrantState.dropped
    }
  }

  def createNewItem(owner: Option[String]): F[ItemStack] = Sync[F].delay {
    val givenItem =
      if (gachaPrize.hasOwner && owner.nonEmpty)
        appendOwnerInformation(owner.get)(gachaPrize.itemStack)
      else gachaPrize.itemStack
    givenItem
  }

}

package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit.listeners

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  CanBeSignedAsGachaPrize,
  GachaPrize
}
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaprizefactory.bukkit.StaticGachaPrizeFactory
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit.traderules.BukkitTrade
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.util.InventoryUtil.InventoryOps
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

class GtToSiinaringo[F[_]: ConcurrentEffect](gachaPrizeTable: Vector[GachaPrize[ItemStack]])(
  implicit canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack]
) extends Listener {

  @EventHandler
  def onGachaRingoEvent(event: InventoryCloseEvent): Unit = {
    // インベントリをクローズしたのがプレイヤーじゃないとき終了
    val player = event.getPlayer match {
      case p: Player => p
      case _         => return
    }

    val inventory = event.getInventory
    val name = player.getName

    // インベントリサイズが4列でない時終了
    if (inventory.row != 4) return

    if (inventory.getTitle != s"$GOLD${BOLD}椎名林檎と交換したい景品を入れてネ") return
    // 交換後の情報
    val tradedInformation =
      new BukkitTrade(name, gachaPrizeTable).trade(inventory.getContents.toList)

    /*
     * 非対象アイテムをインベントリに戻す
     */
    tradedInformation.nonTradableItemStacks.filterNot(_ == null).foreach { itemStack =>
      if (!InventoryOperations.isPlayerInventoryFull(player))
        InventoryOperations.addItem(player, itemStack)
      else InventoryOperations.dropItem(player, itemStack)
    }

    val tradedAmount = tradedInformation.tradedSuccessResult.map(result => result.amount).sum

    if (tradedAmount == 0) {
      player.sendMessage(s"${YELLOW}ギガンティック大当たり景品を認識しませんでした。すべてのアイテムを返却します")
    } else {
      player.sendMessage(
        s"${GREEN}ギガンティック大当たり景品を${tradedInformation.tradedSuccessResult.map(_.amount / SeichiAssist.seichiAssistConfig.rateGiganticToRingo).sum}個認識しました"
      )
    }

    /*
     * 椎名林檎をインベントリへ
     */
    val siinaringo = StaticGachaPrizeFactory.getMaxRingo(name)
    (0 until tradedAmount).foreach { _ =>
      if (!InventoryOperations.isPlayerInventoryFull(player))
        InventoryOperations.addItem(player, siinaringo)
      else InventoryOperations.dropItem(player, siinaringo)
    }

    /*
     * お知らせする
     */
    if (tradedAmount > 0) {
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
      player.sendMessage(s"$GREEN${tradedAmount}個の${GOLD}椎名林檎${WHITE}を受け取りました。")
    }
  }

}

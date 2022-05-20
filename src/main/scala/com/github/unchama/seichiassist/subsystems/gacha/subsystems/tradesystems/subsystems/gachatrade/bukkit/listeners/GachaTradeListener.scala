package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.subsystems.gachatrade.bukkit.listeners

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaskull.bukkit.GachaSkullData
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.subsystems.gachatrade.bukkit.actions.BukkitTrade
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.util.InventoryUtil.InventoryOps
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.{EventHandler, Listener}

class GachaTradeListener[F[_]: ConcurrentEffect](
  implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
) extends Listener {

  @EventHandler
  def onGachaTrade(event: InventoryCloseEvent): Unit = {
    // インベントリをクローズしたのがプレイヤーじゃないとき終了
    val player = event.getPlayer match {
      case p: Player => p
      case _         => return
    }

    val inventory = event.getInventory
    val name = player.getName

    // インベントリサイズが4列でない時終了
    if (inventory.row != 4) return

    if (inventory.getTitle != s"$LIGHT_PURPLE${BOLD}交換したい景品を入れてください") return

    // 交換後の情報
    val tradedInformation =
      BukkitTrade[F](name).trade(inventory.getContents.toList).toIO.unsafeRunSync()

    /*
     * 非対象商品をインベントリに戻す
     */
    tradedInformation.nonTradableItemStacks.filterNot(_ == null).foreach { itemStack =>
      if (!InventoryOperations.isPlayerInventoryFull(player))
        InventoryOperations.addItem(player, itemStack)
      else InventoryOperations.dropItem(player, itemStack)
    }

    val tradeAmount = tradedInformation.tradedAmounts.map(_.amount).sum

    /*
     * ガチャ券を付与する
     */
    val skull = GachaSkullData.gachaForExchanging
    (0 until tradeAmount).foreach { _ =>
      if (!InventoryOperations.isPlayerInventoryFull(player))
        InventoryOperations.addItem(player, skull)
      else InventoryOperations.dropItem(player, skull)
    }

    /*
     * お知らせする
     */
    val tradableItemStacks = tradedInformation.tradedAmounts
    if (tradeAmount == 0) {
      player.sendMessage(s"${YELLOW}景品を認識しませんでした。すべてのアイテムを返却します")
    } else {
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
      player.sendMessage(s"${GREEN}大当たり景品を${tradableItemStacks
          .count(_.amount == 12)}個、あたり景品を${tradableItemStacks.count(_.amount == 3)}個認識しました。")
      player.sendMessage(s"$GREEN${tradeAmount}枚の${GOLD}ガチャ券${WHITE}を受け取りました。")
    }
  }

}

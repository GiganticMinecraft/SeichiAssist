package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gachatrade.bukkit.listeners

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaskull.bukkit.GachaSkullData
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gachatrade.bukkit.actions.BukkitTrade
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
    tradedInformation._3.filterNot(_ == null).foreach { itemStack =>
      if (!InventoryOperations.isPlayerInventoryFull(player))
        InventoryOperations.addItem(player, itemStack)
      else InventoryOperations.dropItem(player, itemStack)
    }

    /*
     * ガチャ券を付与する
     */
    val skull = GachaSkullData.gachaForExchanging
    (0 until tradedInformation._1 + tradedInformation._2).foreach { _ =>
      if (!InventoryOperations.isPlayerInventoryFull(player))
        InventoryOperations.addItem(player, skull)
      else InventoryOperations.dropItem(player, skull)
    }

    /*
     * お知らせする
     */
    if (tradedInformation._1 == 0 && tradedInformation._2 == 0) {
      player.sendMessage(s"${YELLOW}景品を認識しませんでした。すべてのアイテムを返却します")
    } else {
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
      player.sendMessage(
        s"${GREEN}大当たり景品を${tradedInformation._1 / 12}個、あたり景品を${tradedInformation._2}個認識しました。"
      )
      player.sendMessage(
        s"$GREEN${tradedInformation._1 + tradedInformation._2}枚の${GOLD}ガチャ券${WHITE}を受け取りました。"
      )
    }
  }

}

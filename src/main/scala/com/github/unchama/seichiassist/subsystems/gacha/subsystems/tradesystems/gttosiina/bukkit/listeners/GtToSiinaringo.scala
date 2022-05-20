package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gttosiina.bukkit.listeners

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaprizefactory.bukkit.StaticGachaPrizeFactory
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gttosiina.bukkit.actions.BukkitTrade
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.util.InventoryUtil.InventoryOps
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.{EventHandler, Listener}

class GtToSiinaringo[F[_]: ConcurrentEffect](
  implicit gachaPrizesDataOperations: GachaPrizesDataOperations[F]
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
      BukkitTrade[F](name).trade(inventory.getContents.toList).toIO.unsafeRunSync()

    /**
     * 非対象商品をインベントリに戻す
     */
    tradedInformation._2.filterNot(_ == null).foreach { itemStack =>
      if (!InventoryOperations.isPlayerInventoryFull(player))
        InventoryOperations.addItem(player, itemStack)
      else InventoryOperations.dropItem(player, itemStack)
    }

    if (tradedInformation._1 == 0) {
      player.sendMessage(s"${YELLOW}ギガンティック大当たり景品を認識しませんでした。すべてのアイテムを返却します")
    } else {
      player.sendMessage(
        s"${GREEN}ギガンティック大当たり景品を${tradedInformation._1 / SeichiAssist.seichiAssistConfig.rateGiganticToRingo}個認識しました"
      )
    }

    /**
     * 椎名林檎をインベントリへ
     */
    val siinaringo = StaticGachaPrizeFactory.getMaxRingo(name)
    (0 until tradedInformation._1).foreach { _ =>
      if (!InventoryOperations.isPlayerInventoryFull(player))
        InventoryOperations.addItem(player, siinaringo)
      else InventoryOperations.dropItem(player, siinaringo)
    }

    /**
     * お知らせする
     */
    if (tradedInformation._1 > 0) {
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
      player.sendMessage(s"$GREEN${tradedInformation._1}個の${GOLD}椎名林檎${WHITE}を受け取りました。")
    }
  }

}

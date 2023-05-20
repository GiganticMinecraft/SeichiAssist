package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit.listeners

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.bukkit.traderules.BukkitTrade
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.domain.StaticTradeItemFactory
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.util.InventoryUtil.InventoryOps
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

class GtToSiinaringo[F[_]: ConcurrentEffect](
  implicit canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack],
  gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player],
  tradeItemFactory: StaticTradeItemFactory[ItemStack]
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
      new BukkitTrade(name, gachaPrizeAPI.listOfNow.toIO.unsafeRunSync())
        .trade(inventory.getContents.toList)

    val totalAmountOfTradeResult =
      tradedInformation.tradedSuccessResult.map(result => result.amount).sum

    if (totalAmountOfTradeResult == 0) {
      player.sendMessage(s"${YELLOW}ギガンティック大当たり景品を認識しませんでした。すべてのアイテムを返却します")
    } else {
      player.sendMessage(
        s"${GREEN}ギガンティック大当たり景品を${tradedInformation.tradedSuccessResult.length}個認識しました"
      )
    }

    /*
     * 椎名林檎と非対象アイテムをインベントリへ
     */
    val nonTradableItemStacksToReturn =
      tradedInformation.nonTradableItemStacks.filterNot(_ == null)
    val tradableItemStacksToReturn = tradedInformation
      .tradedSuccessResult
      .flatMap(result => Seq.fill(result.amount)(result.itemStack))

    InventoryOperations
      .grantItemStacksEffect[IO](
        nonTradableItemStacksToReturn ++ tradableItemStacksToReturn: _*
      )
      .apply(player)
      .unsafeRunAsyncAndForget()

    /*
     * お知らせする
     */
    if (totalAmountOfTradeResult > 0) {
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
      player.sendMessage(s"$GREEN${totalAmountOfTradeResult}個の${GOLD}椎名林檎${WHITE}を受け取りました。")
    }
  }

}

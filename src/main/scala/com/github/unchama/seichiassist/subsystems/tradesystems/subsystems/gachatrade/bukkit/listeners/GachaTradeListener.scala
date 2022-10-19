package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.listeners

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.gacha.GachaAPI
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.factories.BukkitGachaSkullData
import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules.BukkitTrade
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.util.InventoryUtil.InventoryOps
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack

class GachaTradeListener[F[_]: ConcurrentEffect](
  implicit canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack],
  gachaAPI: GachaAPI[F, ItemStack, Player]
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
      new BukkitTrade(name, gachaAPI.list.toIO.unsafeRunSync())
        .trade(inventory.getContents.toList)

    val tradeAmount = tradedInformation.tradedSuccessResult.map(_.amount).sum

    /*
     * ガチャ券を付与する
     */
    val skull = BukkitGachaSkullData.gachaForExchanging

    /*
     * ガチャ券と交換できなかったアイテムをインベントリに
     */
    InventoryOperations
      .grantItemStacksEffect[IO](
        tradedInformation.nonTradableItemStacks.filterNot(_ == null) ++ Seq
          .fill(tradeAmount)(skull): _*
      )
      .apply(player)
      .unsafeRunAsyncAndForget()

    /*
     * お知らせする
     */
    val tradableItemStacks = tradedInformation.tradedSuccessResult
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

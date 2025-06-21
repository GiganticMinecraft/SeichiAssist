package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.listeners

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeSuccessResult
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules.BigOrRegular
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.{
  GachaListProvider,
  GachaTradeRule
}
import com.github.unchama.util.InventoryUtil.InventoryOps
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import com.github.unchama.seichiassist.subsystems.tradesystems.application.TradeAction
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.generic.effect.unsafe.EffectEnvironment

class GachaTradeListener[F[_]: ConcurrentEffect, G[_]](rule: GachaTradeRule[ItemStack])(
  gachaListProvider: GachaListProvider[F, ItemStack],
  tradeAction: TradeAction[F, Player, ItemStack, (BigOrRegular, Int)],
  effectEnvironment: EffectEnvironment
) extends Listener {

  import cats.implicits._

  @EventHandler
  def onGachaTrade(event: InventoryCloseEvent): Unit = {
    // インベントリをクローズしたのがプレイヤーじゃないとき終了
    val player = event.getPlayer match {
      case p: Player => p
      case _         => return
    }

    val inventory = event.getInventory
    val name = player.getName

    // インベントリサイズが6列でない時終了
    if (inventory.row != 6) return

    if (event.getView.getTitle != s"$LIGHT_PURPLE${BOLD}交換したい景品を入れてください") return

    val program = for {
      gachaList <- gachaListProvider.readGachaList
      tradeRule <- ConcurrentEffect[F].pure(rule.ruleFor(name, gachaList))
      tradeResult <- tradeAction.execute(
        player,
        inventory.getContents().filterNot(_ == null).toList
      )(tradeRule)
      (big, regular) = tradeResult.tradedSuccessResult.partition {
        case TradeSuccessResult(_, _, (rarity, _)) => rarity == BigOrRegular.Big
      }
      bigItemAmount = big.map(_.amount).sum
      regularItemAmount = regular.map(_.amount).sum
    } yield {
      if (tradeResult.tradedSuccessResult.isEmpty) {
        MessageEffect(s"${YELLOW}景品を認識しませんでした。すべてのアイテムを返却します").apply(player)
      } else {
        SequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1f, 1f),
          MessageEffect(s"${GREEN}大当たり景品を${bigItemAmount}個、あたり景品を${regularItemAmount}個認識しました。"),
          MessageEffect(
            s"$GREEN${tradeResult.tradedSuccessResult.map(_.amount).sum}枚の${GOLD}ガチャ券${WHITE}を受け取りました。"
          )
        ).apply(player)
      }
    }

    effectEnvironment.unsafeRunEffectAsync("ガチャ景品とガチャ券の交換処理", program)
  }

}

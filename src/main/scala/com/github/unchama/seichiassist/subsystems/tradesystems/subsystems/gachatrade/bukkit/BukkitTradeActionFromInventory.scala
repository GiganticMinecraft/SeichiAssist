package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit

import com.github.unchama.seichiassist.subsystems.tradesystems.application.TradeAction
import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules.BigOrRegular
import org.bukkit.inventory.ItemStack
import com.github.unchama.seichiassist.util.InventoryOperations
import org.bukkit.entity.Player
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeResult

class BukkitTradeActionFromInventory[F[_]: Sync: OnMinecraftServerThread, G[_]](
  implicit val gachaPointApi: GachaPointApi[F, G, Player]
) extends TradeAction[F, Player, ItemStack, (BigOrRegular, Int)] {

  import cats.implicits._

  protected override val F: Sync[F] = implicitly

  protected override def applyTradeResult(
    player: Player,
    tradeResult: TradeResult[ItemStack, (BigOrRegular, Int)]
  ): F[Unit] = {
    val tradeAmount = tradeResult.tradedSuccessResult.map(_.amount).sum

    val nonTradableItemStacksToReturn =
      tradeResult.nonTradableItemStacks.filterNot(_ == null)

    InventoryOperations
      .grantItemStacksEffect[F](nonTradableItemStacksToReturn: _*)
      .apply(player) >> gachaPointApi
      .addGachaPoint(GachaPoint.gachaPointBy(tradeAmount))
      .apply(player)
  }

}

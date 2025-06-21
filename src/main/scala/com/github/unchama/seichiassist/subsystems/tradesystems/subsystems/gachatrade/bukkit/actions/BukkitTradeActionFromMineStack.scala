package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.actions

import com.github.unchama.seichiassist.subsystems.tradesystems.application.TradeAction
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.bukkit.traderules.BigOrRegular
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeResult
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint

class BukkitTradeActionFromMineStack[F[_], G[_]](
  implicit mineStackAPI: MineStackAPI[F, Player, ItemStack],
  gachaPointApi: GachaPointApi[F, G, Player]
) extends TradeAction[F, Player, ItemStack, (BigOrRegular, Int)] {

  import cats.implicits._

  override implicit protected val F: Sync[F] = implicitly

  override protected def applyTradeResult(
    player: Player,
    tradeResult: TradeResult[ItemStack, (BigOrRegular, Int)]
  ): F[Unit] = {
    val tradeAmount = tradeResult.tradedSuccessResult.map(_.amount).sum

    for {
      successResultWithMineStackObject <- tradeResult.tradedSuccessResult.traverse { result =>
        val mineStackObject =
          mineStackAPI
            .mineStackObjectList
            .findBySignedItemStacks(Vector(result.itemStack), player)

        mineStackObject.map(mineStackObject => (result, mineStackObject.head))
      }
      _ <- successResultWithMineStackObject.traverse {
        case (_, (itemStack, Some(mineStackObject))) =>
          mineStackAPI
            .mineStackRepository
            .subtractStackedAmountOf(player, mineStackObject, itemStack.getAmount())
            .void
        case _ => F.unit
      }
      _ <- gachaPointApi.addGachaPoint(GachaPoint.gachaPointBy(tradeAmount)).apply(player)
    } yield ()
  }

}

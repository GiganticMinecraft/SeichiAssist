package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.application

import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject
import com.github.unchama.seichiassist.subsystems.tradesystems.application.TradeAction
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.GachaListProvider
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.GachaTradeRule
import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.minecraft.algebra.HasName
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.tradesystems.domain.TradeResult
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.TradeError
import com.github.unchama.generic.effect.concurrent.RecoveringSemaphore
import cats.effect.Concurrent
import cats.effect.Timer

class TradeFromMineStack[F[_]: Sync, ItemStack, Player: HasName, TransactionInfo](
  recoveringSemaphore: RecoveringSemaphore[F]
)(
  implicit tradeAction: TradeAction[F, Player, ItemStack, TransactionInfo],
  gachaListProvider: GachaListProvider[F, ItemStack],
  gachaTradeRule: GachaTradeRule[ItemStack, TransactionInfo],
  mineStackAPI: MineStackAPI[F, Player, ItemStack],
  gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player]
) {

  import cats.implicits._

  def tryTradeFromMineStack(
    player: Player,
    mineStackObject: MineStackObject[ItemStack],
    amount: Int
  ): F[Either[TradeError, TradeResult[ItemStack, TransactionInfo]]] = {
    val program: F[Either[TradeError, TradeResult[ItemStack, TransactionInfo]]] = for {
      stackedAmount <- mineStackAPI
        .mineStackRepository
        .getStackedAmountOf(player, mineStackObject)
      signedItemStackOpt <-
        mineStackObject.tryToSignedItemStack(HasName[Player].of(player))
      signedItemStack <- Sync[F].pure(signedItemStackOpt.getOrElse(mineStackObject.itemStack))
      gachaList <- gachaListProvider.readGachaList
      tradeRule <- Sync[F].pure(gachaTradeRule.ruleFor(HasName[Player].of(player), gachaList))
      result <- {
        if (stackedAmount < amount) {
          Sync[F].pure(Left(TradeError.NotEnougthItemAmount))
        } else if (!tradeRule.tradableItems.contains(signedItemStack)) {
          Sync[F].pure(Left(TradeError.NotTradableItem))
        } else {
          val tradeContents = List.fill(amount)(signedItemStack)
          tradeAction.execute(player, tradeContents)(tradeRule).map(Right(_))
        }
      }
    } yield result

    val semaphoreError: F[Either[TradeError, TradeResult[ItemStack, TransactionInfo]]] =
      Sync[F].pure(Left(TradeError.UsageSemaphoreIsLocked))

    recoveringSemaphore.tryUse(program, semaphoreError)(TradeFromMineStack.usageInterval)
  }

}

object TradeFromMineStack {

  import cats.implicits._
  import scala.concurrent.duration._

  final val usageInterval = 1.second

  def newIn[G[_]: Sync, F[_]: Concurrent: Timer, ItemStack, Player: HasName, TransactionInfo](
    implicit tradeAction: TradeAction[F, Player, ItemStack, TransactionInfo],
    gachaListProvider: GachaListProvider[F, ItemStack],
    gachaTradeRule: GachaTradeRule[ItemStack, TransactionInfo],
    mineStackAPI: MineStackAPI[F, Player, ItemStack],
    gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player]
  ): G[TradeFromMineStack[F, ItemStack, Player, TransactionInfo]] = {
    RecoveringSemaphore.newIn[G, F].map(rs => new TradeFromMineStack(rs))
  }

}

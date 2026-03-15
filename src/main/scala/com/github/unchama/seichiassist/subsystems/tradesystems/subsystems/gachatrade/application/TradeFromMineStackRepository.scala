package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.application

import cats.effect.Sync
import cats.effect.Timer
import cats.effect.Concurrent
import com.github.unchama.minecraft.algebra.HasName
import com.github.unchama.seichiassist.subsystems.tradesystems.application.TradeAction
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.GachaListProvider
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.GachaTradeRule
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.datarepository.template.RepositoryDefinition

object TradeFromMineStackRepository {

  def inSyncContext[G[_]: Sync, F[
    _
  ]: Concurrent: Timer, ItemStack, Player: HasName, TransactionInfo](
    implicit tradeAction: TradeAction[F, Player, ItemStack, TransactionInfo],
    gachaListProvider: GachaListProvider[F, ItemStack],
    gachaTradeRule: GachaTradeRule[ItemStack, TransactionInfo],
    mineStackAPI: MineStackAPI[F, Player, ItemStack],
    gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player]
  ): RepositoryDefinition[
    G,
    Player,
    TradeFromMineStack[F, ItemStack, Player, TransactionInfo]
  ] =
    RepositoryDefinition
      .Phased
      .SinglePhased
      .withSupplierAndTrivialFinalization(
        TradeFromMineStack.newIn[G, F, ItemStack, Player, TransactionInfo]
      )

}

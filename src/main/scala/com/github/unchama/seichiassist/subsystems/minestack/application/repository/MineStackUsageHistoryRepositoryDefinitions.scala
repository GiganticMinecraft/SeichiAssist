package com.github.unchama.seichiassist.subsystems.minestack.application.repository

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackUsageHistory

object MineStackUsageHistoryRepositoryDefinitions {

  def initialization[F[_]: Sync, Player, ItemStack]
    : TwoPhasedRepositoryInitialization[F, Player, MineStackUsageHistory[F, ItemStack]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, MineStackUsageHistory[F, ItemStack]] { _ =>
        Applicative[F].pure(new MineStackUsageHistory[F, ItemStack])
      }

  def finalization[F[_]: Applicative, Player, ItemStack]
    : RepositoryFinalization[F, Player, MineStackUsageHistory[F, ItemStack]] =
    RepositoryFinalization.trivial

}

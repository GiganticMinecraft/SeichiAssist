package com.github.unchama.seichiassist.subsystems.minestack.application.repository

import cats.Applicative
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackUsageHistory

object MineStackUsageHistoryRepositoryDefinitions {

  def initialization[F[_]: Applicative, Player, ItemStack <: Cloneable]
    : TwoPhasedRepositoryInitialization[F, Player, MineStackUsageHistory[ItemStack]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, MineStackUsageHistory[ItemStack]] { _ =>
        Applicative[F].pure(new MineStackUsageHistory[ItemStack])
      }

  def finalization[F[_]: Applicative, Player, ItemStack <: Cloneable]
    : RepositoryFinalization[F, Player, MineStackUsageHistory[ItemStack]] =
    RepositoryFinalization.trivial

}

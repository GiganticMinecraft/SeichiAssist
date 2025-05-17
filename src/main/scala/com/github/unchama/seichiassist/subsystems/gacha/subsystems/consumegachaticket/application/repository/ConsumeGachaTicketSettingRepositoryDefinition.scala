package com.github.unchama.seichiassist.subsystems.gacha.subsystems.consumegachaticket.application.repository

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.consumegachaticket.domain.ConsumeGachaTicketSettings

object ConsumeGachaTicketSettingRepositoryDefinition {

  def initialization[F[_]: Sync, Player]
    : TwoPhasedRepositoryInitialization[F, Player, ConsumeGachaTicketSettings[F]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, ConsumeGachaTicketSettings[F]] { _ =>
        Sync[F].pure(new ConsumeGachaTicketSettings[F])
      }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, ConsumeGachaTicketSettings[F]] =
    RepositoryFinalization.trivial

}

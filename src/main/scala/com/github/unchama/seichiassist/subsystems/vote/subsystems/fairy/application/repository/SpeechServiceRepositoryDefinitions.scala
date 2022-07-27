package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairySpeechGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.service.FairySpeechService

object SpeechServiceRepositoryDefinitions {

  def initialization[F[_]: Applicative: Sync, Player](
    implicit gatewayProvider: Player => FairySpeechGateway[F]
  ): TwoPhasedRepositoryInitialization[F, Player, FairySpeechService[F]] =
    TwoPhasedRepositoryInitialization.withoutPrefetching[F, Player, FairySpeechService[F]] {
      player => Sync[F].delay(new FairySpeechService[F](gatewayProvider(player)))
    }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, FairySpeechService[F]] = RepositoryFinalization.trivial

}

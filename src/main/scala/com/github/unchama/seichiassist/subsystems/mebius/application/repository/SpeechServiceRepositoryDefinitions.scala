package com.github.unchama.seichiassist.subsystems.mebius.application.repository

import cats.{Applicative, Monad}
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.seichiassist.subsystems.mebius.domain.speech.{
  MebiusSpeechBlockageState,
  MebiusSpeechGateway
}
import com.github.unchama.seichiassist.subsystems.mebius.service.MebiusSpeechService

object SpeechServiceRepositoryDefinitions {

  import cats.implicits._

  def initialization[F[_]: Monad, Player](
    implicit getFreshBlockageState: F[MebiusSpeechBlockageState[F]],
    gatewayProvider: Player => MebiusSpeechGateway[F]
  ): TwoPhasedRepositoryInitialization[F, Player, MebiusSpeechService[F]] =
    TwoPhasedRepositoryInitialization.withoutPrefetching[F, Player, MebiusSpeechService[F]] {
      player =>
        getFreshBlockageState.map { blockageState =>
          new MebiusSpeechService[F](gatewayProvider(player), blockageState)
        }
    }

  def finalization[F[_]: Applicative, Player]
    : RepositoryFinalization[F, Player, MebiusSpeechService[F]] = RepositoryFinalization.trivial

}

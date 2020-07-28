package com.github.unchama.seichiassist.mebius.bukkit.repository

import cats.effect.{Effect, IO, Sync, SyncIO}
import com.github.unchama.playerdatarepository.JoinToQuitPlayerDataRepository
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeechBlockageState, MebiusSpeechGateway}
import com.github.unchama.seichiassist.mebius.service.MebiusSpeechService
import org.bukkit.entity.Player

class SpeechServiceRepository[F[_] : Sync](implicit
                                           getFreshBlockageState: SyncIO[MebiusSpeechBlockageState[F]],
                                           gatewayProvider: Player => MebiusSpeechGateway[F])
  extends JoinToQuitPlayerDataRepository[MebiusSpeechService[F]] {

  override protected def initialValue(player: Player): MebiusSpeechService[F] = {
    val freshBlockingState = getFreshBlockageState.unsafeRunSync()
    new MebiusSpeechService[F](gatewayProvider(player), freshBlockingState)
  }

  override protected def unloadData(player: Player, r: MebiusSpeechService[F]): IO[Unit] = IO.unit
}

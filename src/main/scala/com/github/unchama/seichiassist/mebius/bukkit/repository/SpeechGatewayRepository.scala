package com.github.unchama.seichiassist.mebius.bukkit.repository

import cats.effect.{IO, Sync}
import com.github.unchama.playerdatarepository.JoinToQuitPlayerDataRepository
import com.github.unchama.seichiassist.mebius.domain.speech.MebiusSpeechGateway
import org.bukkit.entity.Player

class SpeechGatewayRepository[F[_] : Sync](implicit gatewayProvider: Player => MebiusSpeechGateway[F])
  extends JoinToQuitPlayerDataRepository[MebiusSpeechGateway[F]] {

  override protected def initialValue(player: Player): MebiusSpeechGateway[F] = gatewayProvider(player)

  override protected def unloadData(player: Player, r: MebiusSpeechGateway[F]): IO[Unit] = IO.unit
}

package com.github.unchama.seichiassist.mebius.controller.repository

import java.util.UUID

import cats.effect.{IO, SyncIO}
import com.github.unchama.playerdatarepository.PlayerDataOnMemoryRepository
import com.github.unchama.seichiassist.mebius.domain.{MebiusSpeechPresentation, MebiusSpeechGateway}
import org.bukkit.entity.Player

class SpeechGatewayRepository[F[_] : MebiusSpeechPresentation] extends PlayerDataOnMemoryRepository[MebiusSpeechGateway[F]] {

  override val loadData: (String, UUID) => SyncIO[Either[Option[String], MebiusSpeechGateway[F]]] =
    (_, _) => SyncIO {
      new MebiusSpeechGateway()
    }

  override val unloadData: (Player, MebiusSpeechGateway[F]) => IO[Unit] =
    (_, _) => IO.unit

}

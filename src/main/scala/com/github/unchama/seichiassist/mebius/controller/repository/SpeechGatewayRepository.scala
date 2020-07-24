package com.github.unchama.seichiassist.mebius.controller.repository

import java.util.UUID

import cats.effect.{IO, Sync, SyncIO}
import com.github.unchama.playerdatarepository.PlayerDataOnMemoryRepository
import com.github.unchama.seichiassist.mebius.domain.{MebiusSpeechGateway, MebiusSpeechPresentation}
import org.bukkit.entity.Player

class SpeechGatewayRepository[F[_] : Sync](implicit presentation: MebiusSpeechPresentation[F[Unit]])
  extends PlayerDataOnMemoryRepository[MebiusSpeechGateway[F]] {

  override val loadData: (String, UUID) => SyncIO[Either[Option[String], MebiusSpeechGateway[F]]] =
    (_, _) => SyncIO(Right {
      new MebiusSpeechGateway()
    })

  override val unloadData: (Player, MebiusSpeechGateway[F]) => IO[Unit] =
    (_, _) => IO.unit

}

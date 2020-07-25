package com.github.unchama.seichiassist.mebius.bukkit.repository

import cats.effect.{Fiber, IO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.playerdatarepository.JoinToQuitPlayerDataRepository
import com.github.unchama.seichiassist.mebius.bukkit.routines.PeriodicMebiusSpeechRoutine
import com.github.unchama.seichiassist.mebius.domain.speech.MebiusSpeechGateway
import org.bukkit.entity.Player

class PeriodicMebiusSpeechRoutineFiberRepository(implicit gatewayRepository: JoinToQuitPlayerDataRepository[MebiusSpeechGateway[IO]],
                                                 repeatingTaskContext: RepeatingTaskContext)
  extends JoinToQuitPlayerDataRepository[Fiber[IO, Nothing]] {

  override protected def initialValue(player: Player): Fiber[IO, Nothing] = {
    PeriodicMebiusSpeechRoutine
      .start(player)
      .start(IO.contextShift(repeatingTaskContext))
      .unsafeRunSync()
  }

  override protected def unloadData(player: Player, r: Fiber[IO, Nothing]): IO[Unit] =
    r.cancel

}

package com.github.unchama.seichiassist.subsystems.gachapoint.application.process

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Timer}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.minecraft.actions.GetConnectedPlayers
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.BatchUsageSemaphore
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.settings.GachaTicketReceivingSettings

object ConvertPointToTickets {

  import cats.implicits._

  import scala.concurrent.duration._

  def stream[
    F[_] : ConcurrentEffect : Timer : GetConnectedPlayers[*[_], Player],
    G[_],
    Player
  ](settingsRepository: KeyedDataRepository[Player, Ref[F, GachaTicketReceivingSettings]],
    semaphoreRepository: KeyedDataRepository[Player, BatchUsageSemaphore[F, G]]): fs2.Stream[F, Unit] =
    fs2.Stream
      .awakeEvery[F](1.minute)
      .evalTap { _ =>
        GetConnectedPlayers[F, Player].now >>= (_.traverse { player =>
          val shouldRunConversion =
            settingsRepository.lift(player)
              .traverse(_.get)
              .map(_.contains(GachaTicketReceivingSettings.EveryMinute))

          val conversion =
            semaphoreRepository
              .lift(player)
              .traverse(_.tryBatchTransaction)
              .as(())

          Monad[F].ifM(shouldRunConversion)(conversion, Monad[F].unit)
        })
      }
      .as(())

}

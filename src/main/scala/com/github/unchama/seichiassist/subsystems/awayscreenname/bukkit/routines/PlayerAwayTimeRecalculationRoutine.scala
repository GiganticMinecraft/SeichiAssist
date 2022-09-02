package com.github.unchama.seichiassist.subsystems.awayscreenname.bukkit.routines

import cats.effect.{IO, SyncIO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.awayscreenname.domain.{
  PlayerIdleMinuteRepository,
  PlayerLocationRepository
}
import org.bukkit.Location
import org.bukkit.entity.Player

object PlayerAwayTimeRecalculationRoutine {

  import cats.implicits._
  import scala.concurrent.duration._

  /**
   * @return リポジトリのデータを現在のプレイヤーの位置と放置時間に更新する作用
   */
  private def updatePlayerLocationAndPlayerIdleMinute(player: Player)(
    implicit locationRepository: KeyedDataRepository[
      Player,
      PlayerLocationRepository[SyncIO, Location, Player]
    ],
    idleMinuteRepository: KeyedDataRepository[Player, PlayerIdleMinuteRepository[SyncIO]]
  ): SyncIO[Unit] = {
    val playerLocationRepository = locationRepository(player)
    val playerIdleMinuteRepository = idleMinuteRepository(player)
    for {
      playerLocation <- playerLocationRepository.getRepositoryLocation
      _ <- playerLocationRepository.updateNowLocation()
      _ <- playerIdleMinuteRepository
        .addOneMinute()
        .whenA(playerLocation.location == player.getLocation)
      _ <- playerIdleMinuteRepository
        .reset()
        .whenA(playerLocation.location != player.getLocation)
    } yield ()
  }

  def start(player: Player)(
    implicit locationRepository: KeyedDataRepository[
      Player,
      PlayerLocationRepository[SyncIO, Location, Player]
    ],
    idleMinuteRepository: KeyedDataRepository[Player, PlayerIdleMinuteRepository[SyncIO]],
    repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO]
  ): IO[Nothing] = {
    val repeatInterval: IO[FiniteDuration] = IO(1.minute)

    implicit val timer: Timer[IO] = IO.timer(repeatingTaskContext)

    RepeatingRoutine.permanentRoutine(
      repeatInterval,
      onMainThread.runAction {
        updatePlayerLocationAndPlayerIdleMinute(player)
      }
    )
  }

}

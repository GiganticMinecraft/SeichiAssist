package com.github.unchama.seichiassist.subsystems.idletime.bukkit.routines

import cats.effect.SyncIO
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.idletime.domain.{
  PlayerIdleTimeRecalculationRoutine,
  PlayerIdleMinuteRepository,
  PlayerLocationRepository
}
import org.bukkit.Location
import org.bukkit.entity.Player

class BukkitPlayerIdleTimeRecalculationRoutine(player: Player)(
  implicit idleTimeRepository: KeyedDataRepository[Player, PlayerIdleMinuteRepository[SyncIO]],
  locationRepository: KeyedDataRepository[
    Player,
    PlayerLocationRepository[SyncIO, Location, Player]
  ]
) extends PlayerIdleTimeRecalculationRoutine[Player] {

  override def updatePlayerLocationAndPlayerIdleMinute: SyncIO[Unit] = {
    val playerIdleTimeRepository = idleTimeRepository(player)
    val playerLocationRepository = locationRepository(player)
    for {
      playerLocation <- playerLocationRepository.getRepositoryLocation
      _ <- playerLocationRepository.updateNowLocation
      _ <-
        if (playerLocation.location == player.getLocation) {
          playerIdleTimeRepository.addOneMinute
        } else {
          playerIdleTimeRepository.reset
        }
    } yield ()
  }

}

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

  import cats.implicits._

  /**
   * @return リポジトリのデータを現在のプレイヤーの位置と放置時間を更新する作用
   */
  override def updatePlayerLocationAndPlayerIdleMinute(): SyncIO[Unit] = {
    val playerIdleTimeRepository = idleTimeRepository(player)
    val playerLocationRepository = locationRepository(player)
    for {
      playerLocation <- playerLocationRepository.getRepositoryLocation
      _ <- playerLocationRepository.updateNowLocation()
      _ <- playerIdleTimeRepository
        .addOneMinute()
        .whenA(playerLocation.location == player.getLocation)
      _ <- playerIdleTimeRepository.reset().whenA(playerLocation.location != player.getLocation)
    } yield ()
  }

}

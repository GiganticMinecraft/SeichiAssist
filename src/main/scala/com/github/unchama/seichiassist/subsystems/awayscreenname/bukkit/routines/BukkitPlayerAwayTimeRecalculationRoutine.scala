package com.github.unchama.seichiassist.subsystems.awayscreenname.bukkit.routines

import cats.effect.SyncIO
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.awayscreenname.domain.{
  PlayerAwayTimeRecalculationRoutine,
  PlayerIdleMinuteRepository,
  PlayerLocationRepository,
  UpdatePlayerScreenName
}
import org.bukkit.Location
import org.bukkit.entity.Player

class BukkitPlayerAwayTimeRecalculationRoutine(player: Player)(
  implicit locationRepository: KeyedDataRepository[
    Player,
    PlayerLocationRepository[SyncIO, Location, Player]
  ],
  idleMinuteRepository: KeyedDataRepository[Player, PlayerIdleMinuteRepository[SyncIO]],
  updatePlayerScreenName: UpdatePlayerScreenName[SyncIO, Player]
) extends PlayerAwayTimeRecalculationRoutine[Player] {

  import cats.implicits._

  /**
   * @return リポジトリのデータを現在のプレイヤーの位置と放置時間を更新する作用
   */
  def updatePlayerLocationAndPlayerIdleMinute(): SyncIO[Unit] = {
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
      _ <- updatePlayerScreenName.updatePlayerNameColor(player)
    } yield ()
  }

}

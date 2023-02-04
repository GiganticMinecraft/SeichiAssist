package com.github.unchama.seichiassist.subsystems.idletime.bukkit

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.idletime.domain.{
  PlayerLocation,
  PlayerLocationRepository
}
import org.bukkit.Location
import org.bukkit.entity.Player

class BukkitPlayerLocationRepository[F[_]: Sync](player: Player)
    extends PlayerLocationRepository[F, Location, Player] {

  override protected val locationRepository: Ref[F, PlayerLocation[Location]] =
    Ref.unsafe[F, PlayerLocation[Location]](PlayerLocation(player.getLocation))

  override def updateNowLocation: F[Unit] =
    locationRepository.set(PlayerLocation[Location](player.getLocation))
}

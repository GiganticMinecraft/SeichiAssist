package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gridregion.domain._
import com.github.unchama.util.external.{WorldEditWrapper, WorldGuardWrapper}
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import org.bukkit.Location
import org.bukkit.entity.Player

class BukkitRegionRegister[F[_]: Sync](
  implicit regionCountAllUntilNowRepository: KeyedDataRepository[Player, Ref[F, RegionCount]]
) extends RegionRegister[F, Location, Player] {

  import cats.implicits._

  override def canCreateRegion(
    player: Player,
    shape: SubjectiveRegionShape
  ): F[RegionCreationResult] = {
    for {
      world <- Sync[F].delay(player.getWorld)
      wgManager = WorldGuardWrapper.getRegionManager(world)
      isGridProtectionEnabled <- Sync[F].delay(
        SeichiAssist.seichiAssistConfig.isGridProtectionEnabled(world)
      )
      worldEditSelection <- Sync[F].delay(WorldEditWrapper.getSelection(player))
      applicableRegions <- Sync[F].delay(wgManager.getApplicableRegions(worldEditSelection))
      regionCountPerPlayer <- Sync[F].delay(WorldGuardWrapper.getNumberOfRegions(player, world))
      maxRegionCountPerWorld <- Sync[F].delay(WorldGuardWrapper.getWorldMaxRegion(world))
    } yield {
      if (!isGridProtectionEnabled) {
        RegionCreationResult.WorldProhibitsRegionCreation
      } else if (
        regionCountPerPlayer < maxRegionCountPerWorld && applicableRegions.size() == 0
      ) {
        RegionCreationResult.Success
      } else {
        RegionCreationResult.Error
      }
    }

  }
}

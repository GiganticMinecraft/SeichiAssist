package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.gridregion.application.actions.CreateRegion
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  RegionCount,
  RegionSelectionCorners
}
import org.bukkit.Location
import org.bukkit.entity.Player
import com.github.unchama.util.external.{WorldEditWrapper, WorldGuardWrapper}
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion

class BukkitCreateRegion[F[_]: Sync](
  implicit regionCountAllUntilNowRepository: KeyedDataRepository[Player, Ref[F, RegionCount]]
) extends CreateRegion[F, Player, Location] {

  import cats.implicits._

  override def apply(player: Player, corners: RegionSelectionCorners[Location]): F[Unit] =
    for {
      regionCount <- regionCountAllUntilNowRepository(player).get
      regionName = s"${player.getName}_${regionCount.value}"
      selectedProtectedCuboidRegion <- Sync[F].delay {
        WorldEditWrapper.getSelectedRegion(player).map { region =>
          new ProtectedCuboidRegion(
            regionName,
            region.getMinimumPoint.withY(-64),
            region.getMaximumPoint.withY(320)
          )
        }
      }
      wgManager = WorldGuardWrapper.getRegionManager(player.getWorld)
      _ <- Sync[F].delay {
        selectedProtectedCuboidRegion.foreach(wgManager.addRegion)
      }
      _ <- Sync[F].delay {
        selectedProtectedCuboidRegion.foreach(protectedCuboidRegion =>
          WorldGuardWrapper.addRegionOwner(protectedCuboidRegion, player)
        )
      }
      _ <- regionCountAllUntilNowRepository(player).update(_.increment)
    } yield ()
}

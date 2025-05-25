package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import cats.Monad
import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  GetGridUnitSizeLimitPerWorld,
  GetRegionCountLimit,
  RegionCount,
  RegionCreationPolicy,
  RegionSelectionCorners,
  SubjectiveRegionShape
}
import com.github.unchama.util.external.WorldGuardWrapper
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import org.bukkit.entity.Player
import org.bukkit.{Location, World}

class BukkitRegionCreationPolicy[F[_]: Sync]
    extends RegionCreationPolicy[F, Player, World, Location] {
  override def isGridProtectionEnabledWorld(world: World): F[Boolean] = Sync[F].delay {
    SeichiAssist.seichiAssistConfig.isGridProtectionEnabled(world)
  }

  override def isNotOverlapping(
    world: World,
    regionSelectionCorners: RegionSelectionCorners[Location]
  ): F[Boolean] =
    Sync[F].delay {
      val min = BlockVector3.at(
        regionSelectionCorners.startPosition.getBlockX,
        -64,
        regionSelectionCorners.startPosition.getBlockZ
      )
      val max = BlockVector3.at(
        regionSelectionCorners.endPosition.getBlockX,
        320,
        regionSelectionCorners.endPosition.getBlockZ
      )

      val protectedCuboidRegion = new ProtectedCuboidRegion("regionName", min, max)

      WorldGuardWrapper.isNotOverlapping(world, protectedCuboidRegion)
    }

  import cats.implicits._

  override def isWithinRegionUnitSizeLimit(
    shape: SubjectiveRegionShape,
    world: World,
    getGridLimitPerWorld: GetGridUnitSizeLimitPerWorld[F, World]
  ): F[Boolean] = for {
    gridSizeLimit <- getGridLimitPerWorld(world)
  } yield gridSizeLimit.limit.count >= shape.regionUnits.count

  override def isNotOverRegionCountLimit(
    world: World,
    player: Player,
    getRegionCountLimit: GetRegionCountLimit[F, World]
  ): F[Boolean] = for {
    regionCountOfPlayer <- Sync[F].delay(WorldGuardWrapper.getNumberOfRegions(player, world))
    limit <- getRegionCountLimit(world)
  } yield regionCountOfPlayer < limit.value

  override protected implicit val F: Monad[F] = implicitly
}

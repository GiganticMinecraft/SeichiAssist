package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  GetRegionCountLimit,
  RegionCount
}
import com.github.unchama.util.external.WorldGuardWrapper
import org.bukkit.World

class BukkitGetRegionCountLimit[F[_]: Sync] extends GetRegionCountLimit[F, World] {
  override def apply(world: World): F[RegionCount] = Sync[F].delay {
    RegionCount(WorldGuardWrapper.getWorldMaxRegion(world))
  }
}

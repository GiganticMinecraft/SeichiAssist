package com.github.unchama.seichiassist.subsystems.gridregion.bukkit

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  GetGridUnitSizeLimitPerWorld,
  RegionUnitCount,
  RegionUnitSizeLimit
}
import org.bukkit.World

class BukkitGetGridUnitSizeLimitPerWorld[F[_]: Sync]
    extends GetGridUnitSizeLimitPerWorld[F, World] {
  override def apply(world: World): F[RegionUnitSizeLimit] = Sync[F].delay {
    val limit = SeichiAssist.seichiAssistConfig.getGridLimitPerWorld(world.getName)
    RegionUnitSizeLimit(RegionUnitCount(limit))
  }
}

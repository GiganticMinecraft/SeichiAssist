package com.github.unchama.seichiassist.subsystems.gridregion.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.gridregion.application.GetGridLimitPerWorld
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  RegionUnitSizeLimit,
  RegionUnitCount
}

class GetGridLimitPerWorldFromConfig[F[_]: Sync] extends GetGridLimitPerWorld[F] {
  override def apply(worldName: String): F[RegionUnitSizeLimit] = Sync[F].delay {
    val limit = SeichiAssist.seichiAssistConfig.getGridLimitPerWorld(worldName)
    RegionUnitSizeLimit(RegionUnitCount(limit))
  }
}

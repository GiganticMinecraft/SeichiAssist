package com.github.unchama.seichiassist.subsystems.gridregion.infrastructure

import com.github.unchama.seichiassist.subsystems.gridregion.domain.{RegionUnits, RegionUnitsPersistence}

import java.util.UUID

class JdbcRegionUnitsPersistence[F[_]] extends RegionUnitsPersistence[F] {
  override def read(key: UUID): F[Option[RegionUnits]] = ???

  override def write(key: UUID, value: RegionUnits): F[Unit] = ???
}

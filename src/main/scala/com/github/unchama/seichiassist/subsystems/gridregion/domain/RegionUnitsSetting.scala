package com.github.unchama.seichiassist.subsystems.gridregion.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class RegionUnitsSetting[F[_]: Sync] {

  private val regionUnitsReference: Ref[F, RegionUnits] = Ref.unsafe(RegionUnits.initial)

  def regionUnits: F[RegionUnits] = regionUnitsReference.get

  def set(regionUnits: RegionUnits): F[Unit] = regionUnitsReference.set(regionUnits)

}

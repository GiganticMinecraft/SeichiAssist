package com.github.unchama.seichiassist.subsystems.gridregion.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class RegionShapeSelectionState[F[_]: Sync] private (
  private val regionUnitsReference: Ref[F, SubjectiveRegionShape]
) {
  def currentShape: F[SubjectiveRegionShape] = regionUnitsReference.get

  def set(regionUnits: SubjectiveRegionShape): F[Unit] = regionUnitsReference.set(regionUnits)
}

object RegionShapeSelectionState {
  import cats.implicits._

  def apply[F[_]: Sync]: F[RegionShapeSelectionState[F]] =
    Ref
      .of[F, SubjectiveRegionShape](SubjectiveRegionShape.minimal)
      .map(new RegionShapeSelectionState(_))
}

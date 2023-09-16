package com.github.unchama.seichiassist.subsystems.gridregion.domain

import cats.effect.Sync
import cats.effect.concurrent.Ref

class RegionShapeSelectionState[F[_]: Sync] {

  private val regionUnitsReference: Ref[F, SubjectiveRegionShape] =
    Ref.unsafe(SubjectiveRegionShape.initial)

  def currentShape: F[SubjectiveRegionShape] = regionUnitsReference.get

  def set(regionUnits: SubjectiveRegionShape): F[Unit] = regionUnitsReference.set(regionUnits)

}

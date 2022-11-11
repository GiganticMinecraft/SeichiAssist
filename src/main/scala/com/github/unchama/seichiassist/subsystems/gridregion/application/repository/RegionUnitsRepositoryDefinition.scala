package com.github.unchama.seichiassist.subsystems.gridregion.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{RegionUnits, RegionUnitsPersistence}

object RegionUnitsRepositoryDefinition {

  def withContext[F[_]: Sync, Player: HasUuid](
    persistence: RegionUnitsPersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, RegionUnits]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, RegionUnits](persistence)(RegionUnits.initial)
      .toRefRepository
      .augmentToTwoPhased((_, ref) =>
        Sync[F].pure(ref)
      )(value => Sync[F].pure(value))

}

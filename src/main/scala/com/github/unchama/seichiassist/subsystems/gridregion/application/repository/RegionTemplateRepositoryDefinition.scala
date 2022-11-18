package com.github.unchama.seichiassist.subsystems.gridregion.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  RegionTemplateId,
  RegionTemplatePersistence,
  RegionUnits
}

object RegionTemplateRepositoryDefinition {

  def withContext[F[_]: Sync, Player: HasUuid](
    implicit persistence: RegionTemplatePersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, Map[RegionTemplateId, RegionUnits]]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, Map[RegionTemplateId, RegionUnits]](persistence)(Map.empty)
      .toRefRepository
      .augmentToTwoPhased((_, ref) => Sync[F].pure(ref))(value => Sync[F].pure(value))

}

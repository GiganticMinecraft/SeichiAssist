package com.github.unchama.seichiassist.subsystems.gridregion.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{GridTemplate, RegionTemplatePersistence}

object RegionTemplateRepositoryDefinition {

  def withContext[F[_]: Sync, Player: HasUuid](
    implicit persistence: RegionTemplatePersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, Vector[GridTemplate]]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, Vector[GridTemplate]](persistence)(Vector.empty)
      .toRefRepository
      .augmentToTwoPhased((_, ref) => Sync[F].pure(ref))(value => Sync[F].pure(value))

}

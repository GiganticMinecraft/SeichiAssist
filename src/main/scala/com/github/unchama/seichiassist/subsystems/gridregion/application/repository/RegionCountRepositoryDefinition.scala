package com.github.unchama.seichiassist.subsystems.gridregion.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.gridregion.domain.RegionCount
import com.github.unchama.seichiassist.subsystems.gridregion.domain.persistence.RegionCountPersistence

object RegionCountRepositoryDefinition {

  def withContext[F[_]: Sync, Player: HasUuid](
    implicit persistence: RegionCountPersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, RegionCount]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, RegionCount](persistence)(RegionCount(0))
      .toRefRepository
      .augmentToTwoPhased((_, ref) => Sync[F].pure(ref))(value => Sync[F].pure(value))

}

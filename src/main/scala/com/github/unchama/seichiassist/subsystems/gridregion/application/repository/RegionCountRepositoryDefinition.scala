package com.github.unchama.seichiassist.subsystems.gridregion.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{RegionCount, RegionCountPersistence}

object RegionCountRepositoryDefinition {

  def withContext[F[_]: Sync, Player: HasUuid](
    persistence: RegionCountPersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, RegionCount]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, RegionCount](persistence)(RegionCount.initial)
      .toRefRepository
      .augmentToTwoPhased((player, ref) => Sync[F].pure(ref))(value => Sync[F].pure(value))

}

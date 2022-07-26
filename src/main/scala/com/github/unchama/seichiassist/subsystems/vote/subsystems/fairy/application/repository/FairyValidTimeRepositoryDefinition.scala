package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  FairyValidTimes,
  FairyValidTimesPersistence
}

object FairyValidTimeRepositoryDefinition {

  def withContext[F[_]: Sync, Player: HasUuid](
    persistence: FairyValidTimesPersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, FairyValidTimes]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, FairyValidTimes](persistence)(FairyValidTimes(None))
      .toRefRepository

}

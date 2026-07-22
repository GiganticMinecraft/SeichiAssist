package com.github.unchama.seichiassist.subsystems.minestack.application.repository

import cats.effect.Sync
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.minestack.domain.AutoCollectPreference
import com.github.unchama.seichiassist.subsystems.minestack.domain.persistence.AutoCollectPreferencePersistence
import com.github.unchama.datarepository.template.RepositoryDefinition
import cats.effect.Ref

object AutoCollectPreferenceRepositoryDefinition {

  def withContext[F[_]: Sync, Player](
    implicit persistence: AutoCollectPreferencePersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, AutoCollectPreference]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, AutoCollectPreference](persistence)(
        AutoCollectPreference.initial
      )
      .toRefRepository

}

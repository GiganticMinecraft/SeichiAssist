package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template._
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.{
  FastDiggingEffectSuppressionState,
  FastDiggingEffectSuppressionStatePersistence
}

object SuppressionSettingsRepositoryDefinition {

  def withContext[G[_]: Sync, Player](
    persistence: FastDiggingEffectSuppressionStatePersistence[G]
  ): RepositoryDefinition[G, Player, Ref[G, FastDiggingEffectSuppressionState]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict(persistence)(FastDiggingEffectSuppressionState.EnabledWithoutLimit)
      .toRefRepository

}

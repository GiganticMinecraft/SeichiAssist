package com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.domain.{
  BreakSkillTriggerConfig,
  BreakSkillTriggerConfigPersistence
}

object BreakSkillTriggerConfigRepositoryDefinition {

  def withContext[F[_]: Sync, Player](
    implicit persistence: BreakSkillTriggerConfigPersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, BreakSkillTriggerConfig]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, BreakSkillTriggerConfig](persistence)(
        BreakSkillTriggerConfig.initial
      )
      .toRefRepository

}

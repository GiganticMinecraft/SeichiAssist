package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.{
  BreakSkillTargetConfig,
  BreakSkillTargetConfigPersistence
}

object BreakSkillTargetConfigRepositoryDefinition {

  def withContext[F[_]: Sync, Player](
    implicit persistence: BreakSkillTargetConfigPersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, BreakSkillTargetConfig]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, BreakSkillTargetConfig](persistence)(
        BreakSkillTargetConfig.initial
      )
      .toRefRepository

}

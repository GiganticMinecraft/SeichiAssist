package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.application.repository

import cats.effect.Sync
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.{
  BreakSkillTargetConfigPersistence,
  BreakSkillTargetConfigRepository
}

object BreakSkillTargetConfigRepositoryDefinition {

  def withContext[F[_]: Sync, Player](
    implicit persistence: BreakSkillTargetConfigPersistence[F]
  ): RepositoryDefinition[F, Player, BreakSkillTargetConfigRepository[F]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, BreakSkillTargetConfigRepository[F]](persistence)(
        new BreakSkillTargetConfigRepository[F]
      )

}

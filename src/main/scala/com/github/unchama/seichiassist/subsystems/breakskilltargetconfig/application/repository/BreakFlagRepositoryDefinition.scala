package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.{
  BreakFlag,
  BreakFlagPersistence
}

object BreakFlagRepositoryDefinition {

  def withContext[F[_]: Sync, Player](
    implicit persistence: BreakFlagPersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, Set[BreakFlag]]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, Set[BreakFlag]](persistence)(Set.empty)
      .toRefRepository

}

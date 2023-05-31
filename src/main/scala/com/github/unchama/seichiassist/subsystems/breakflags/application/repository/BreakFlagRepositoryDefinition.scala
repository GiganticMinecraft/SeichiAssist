package com.github.unchama.seichiassist.subsystems.breakflags.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breakflags.domain.{BreakFlag, BreakFlagPersistence}

object BreakFlagRepositoryDefinition {

  def withContext[F[_]: Sync, Player](
    persistence: BreakFlagPersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, List[BreakFlag]]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, List[BreakFlag]](persistence)(List.empty)
      .toRefRepository

}

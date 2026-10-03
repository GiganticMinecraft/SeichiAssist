package com.github.unchama.seichiassist.subsystems.breaksuppressionpreference.application.repository

import cats.effect.Sync
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.seichiassist.subsystems.breaksuppressionpreference.domain.{
  BreakSuppressionPreference,
  BreakSuppressionPreferencePersistence
}
import cats.effect.Ref

object BreakSuppressionPreferenceRepositoryDefinition {

  def withContext[F[_]: Sync, Player](
    implicit persistence: BreakSuppressionPreferencePersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, BreakSuppressionPreference]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[F, Player, BreakSuppressionPreference](persistence)(
        BreakSuppressionPreference.initial
      )
      .toRefRepository

}

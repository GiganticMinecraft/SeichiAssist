package com.github.unchama.seichiassist.subsystems.buildcount.application.application

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.{
  BuildAmountData,
  BuildAmountDataPersistence
}

object BuildAmountDataRepositoryDefinition {

  def withPersistence[F[_]: Sync, Player](
    persistence: BuildAmountDataPersistence[F]
  ): RepositoryDefinition[F, Player, Ref[F, BuildAmountData]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict(persistence)(BuildAmountData.initial)
      .toRefRepository

}

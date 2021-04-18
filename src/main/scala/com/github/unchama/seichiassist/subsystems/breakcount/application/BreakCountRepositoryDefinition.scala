package com.github.unchama.seichiassist.subsystems.breakcount.application

import cats.effect.concurrent.Ref
import cats.effect.{Effect, Sync}
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.fs2.workaround.Topic
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.seichiassist.subsystems.breakcount.domain.{SeichiAmountData, SeichiAmountDataPersistence}

object BreakCountRepositoryDefinition {

  def withContext[
    F[_] : Effect, G[_] : Sync, Player
  ](topic: Topic[F, Option[(Player, SeichiAmountData)]],
    persistence: SeichiAmountDataPersistence[G]): RepositoryDefinition[G, Player, Ref[G, SeichiAmountData]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[G, Player, SeichiAmountData](persistence)(SeichiAmountData.initial)
      .withAnotherTappingAction { (player, data) =>
        EffectExtra.runAsyncAndForget[F, G, Unit] {
          topic.publish1(Some(player, data))
        }
      }
      .toRefRepository
}

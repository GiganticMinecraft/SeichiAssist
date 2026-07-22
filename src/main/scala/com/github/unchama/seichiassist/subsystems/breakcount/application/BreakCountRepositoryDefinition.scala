package com.github.unchama.seichiassist.subsystems.breakcount.application

import cats.effect.{Async, Sync}
import cats.effect.std.Dispatcher
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.fs2.workaround.fs3.Fs3Topic
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.seichiassist.subsystems.breakcount.domain.{
  SeichiAmountData,
  SeichiAmountDataPersistence
}
import cats.effect.Ref

object BreakCountRepositoryDefinition {

  import cats.implicits._

  def withContext[F[_]: Async: Dispatcher, G[_]: Sync, Player](
    topic: Fs3Topic[F, Option[(Player, SeichiAmountData)]],
    persistence: SeichiAmountDataPersistence[G]
  ): RepositoryDefinition[G, Player, Ref[G, SeichiAmountData]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[G, Player, SeichiAmountData](persistence)(SeichiAmountData.initial)
      .withAnotherTappingAction { (player, data) =>
        EffectExtra.runAsyncAndForget[F, G, Unit] {
          topic.publish1(Some(player, data)).void
        }
      }
      .toRefRepository
}

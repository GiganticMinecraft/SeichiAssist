package com.github.unchama.seichiassist.subsystems.buildcount.application.application

import cats.effect.{Effect, Sync}
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.fs2.workaround.fs3.Fs3Topic
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.{
  BuildAmountData,
  BuildAmountDataPersistence
}

object BuildAmountDataRepositoryDefinition {

  import cats.implicits._

  def withContext[F[_]: Effect, G[_]: Sync, Player](
    topic: Fs3Topic[F, (Player, BuildAmountData)],
    persistence: BuildAmountDataPersistence[G]
  ): RepositoryDefinition[G, Player, Ref[G, BuildAmountData]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[G, Player, BuildAmountData](persistence)(BuildAmountData.initial)
      .withAnotherTappingAction { (player, data) =>
        EffectExtra.runAsyncAndForget[F, G, Unit] {
          topic.publish1(player, data).void
        }
      }
      .toRefRepository

}

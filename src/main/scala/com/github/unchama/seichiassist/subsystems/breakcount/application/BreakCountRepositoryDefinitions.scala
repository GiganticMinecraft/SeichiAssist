package com.github.unchama.seichiassist.subsystems.breakcount.application

import cats.Applicative
import cats.effect.concurrent.Ref
import cats.effect.{Effect, Sync}
import com.github.unchama.datarepository.template.{RefDictBackedRepositoryFinalization, RefDictBackedRepositoryInitialization, RepositoryFinalization, SinglePhasedRepositoryInitialization}
import com.github.unchama.fs2.workaround.Topic
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.seichiassist.subsystems.breakcount.domain.{SeichiAmountData, SeichiAmountDataPersistence}

import java.util.UUID

object BreakCountRepositoryDefinitions {

  import cats.implicits._

  def initialization[F[_] : Sync](persistence: SeichiAmountDataPersistence[F]): SinglePhasedRepositoryInitialization[F, Ref[F, SeichiAmountData]] =
    SinglePhasedRepositoryInitialization.forRefCell(
      RefDictBackedRepositoryInitialization
        .usingUuidRefDict(persistence)(Applicative[F].pure(SeichiAmountData.initial))
    )

  def tappingAction[
    G[_] : Sync,
    F[_] : Effect,
    Player
  ](topic: Topic[F, Option[(Player, SeichiAmountData)]]): (Player, Ref[G, SeichiAmountData]) => G[Unit] =
    (player, seichiAmountData) => {
      for {
        initialData <- seichiAmountData.get
        _ <- EffectExtra.runAsyncAndForget[F, G, Unit] {
          topic.publish1(Some(player, initialData))
        }
      } yield ()
    }

  def finalization[F[_] : Sync](persistence: SeichiAmountDataPersistence[F]): RepositoryFinalization[F, UUID, Ref[F, SeichiAmountData]] =
    RepositoryFinalization.liftToRefFinalization[F, UUID, SeichiAmountData](
      RefDictBackedRepositoryFinalization
        .using(persistence)(identity)
    )

}

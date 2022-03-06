package com.github.unchama.seichiassist.subsystems.mana.application

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.datarepository.definitions.{
  RefDictBackedRepositoryDefinition,
  SignallingRepositoryDefinition
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.mana.domain.{
  LevelCappedManaAmount,
  ManaAmount,
  ManaAmountPersistence
}
import io.chrisdavenport.log4cats.ErrorLogger

object ManaRepositoryDefinition {

  import cats.implicits._

  def withContext[F[_]: ConcurrentEffect: ErrorLogger, G[_]: Sync: ContextCoercion[
    *[_],
    F
  ], Player: HasUuid](
    publishChanges: fs2.Pipe[F, (Player, LevelCappedManaAmount), Unit],
    persistence: ManaAmountPersistence[G]
  )(
    implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]
  ): RepositoryDefinition[G, Player, Ref[G, LevelCappedManaAmount]] = {

    val valueRepository
      : RepositoryDefinition.Phased.TwoPhased[G, Player, LevelCappedManaAmount] =
      RefDictBackedRepositoryDefinition
        .usingUuidRefDict[G, Player, ManaAmount](persistence)(ManaAmount(0))
        .toTwoPhased
        .flatXmapWithPlayer(player =>
          manaAmount =>
            breakCountReadAPI.seichiAmountDataRepository(player).read.map { data =>
              LevelCappedManaAmount.capping(manaAmount, data.levelCorrespondingToExp)
            }
        )(cappedMana => Monad[G].pure(cappedMana.manaAmount))

    SignallingRepositoryDefinition.withPublishSinkHidden[G, F, Player, LevelCappedManaAmount](
      publishChanges
    )(valueRepository)
  }

}

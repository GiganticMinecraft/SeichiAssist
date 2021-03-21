package com.github.unchama.seichiassist.subsystems.breakcountbar.application

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.datarepository.definitions.{RefDictBackedRepositoryDefinition, SignallingRepositoryDefinition}
import com.github.unchama.datarepository.template._
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.{BreakCountBarVisibility, BreakCountBarVisibilityPersistence}
import fs2.Pipe

object BreakCountBarVisibilityRepositoryDefinition {

  def withContext[
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]],
    Player: HasUuid,
  ](persistence: BreakCountBarVisibilityPersistence[G],
    publishChanges: Pipe[F, (Player, BreakCountBarVisibility), Unit]): RepositoryDefinition[G, Player, Ref[G, BreakCountBarVisibility]] =
    SignallingRepositoryDefinition.forPlayerTopic(publishChanges) {
      RefDictBackedRepositoryDefinition.usingUuidRefDict(persistence)(BreakCountBarVisibility.Shown)
    }

}

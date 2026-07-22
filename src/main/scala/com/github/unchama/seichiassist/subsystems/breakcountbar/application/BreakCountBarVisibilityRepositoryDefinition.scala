package com.github.unchama.seichiassist.subsystems.breakcountbar.application

import cats.effect.{Async, Sync}
import cats.effect.std.Dispatcher
import com.github.unchama.datarepository.definitions.{
  RefDictBackedRepositoryDefinition,
  SignallingRepositoryDefinition
}
import com.github.unchama.datarepository.template._
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.{
  BreakCountBarVisibility,
  BreakCountBarVisibilityPersistence
}
import fs2.Pipe
import org.typelevel.log4cats.ErrorLogger
import cats.effect.Ref

object BreakCountBarVisibilityRepositoryDefinition {

  def withContext[G[_]: Sync, F[_]: Async: Dispatcher: [g[_]] =>> ContextCoercion[
    G,
    g
  ]: ErrorLogger, Player: HasUuid](
    persistence: BreakCountBarVisibilityPersistence[G],
    publishChanges: Pipe[F, (Player, BreakCountBarVisibility), Unit]
  ): RepositoryDefinition[G, Player, Ref[G, BreakCountBarVisibility]] =
    SignallingRepositoryDefinition.withPublishSinkHidden(publishChanges) {
      RefDictBackedRepositoryDefinition.usingUuidRefDict(persistence)(
        BreakCountBarVisibility.Shown
      )
    }

}

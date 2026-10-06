package com.github.unchama.datarepository.definitions

import cats.effect.std.Dispatcher
import cats.effect.{Async, Sync}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.SinglePhasedRepositoryInitialization
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.SessionMutex

import java.util.UUID

object SessionMutexRepositoryDefinition {

  import cats.implicits._

  def withRepositoryContext[F[_]: Async: Dispatcher, G[_]: Sync: [f[_]] =>> ContextCoercion[
    f,
    F
  ], Player]: RepositoryDefinition[G, Player, SessionMutex[F, G]] = {
    RepositoryDefinition
      .Phased
      .SinglePhased
      .withoutTappingAction(
        SinglePhasedRepositoryInitialization.withSupplier(SessionMutex.newIn[F, G]),
        RepositoryFinalization.withoutAnyPersistence[G, UUID, SessionMutex[F, G]] {
          (_, mutex) =>
            EffectExtra.runAsyncAndForget[F, G, Unit] {
              mutex.stopAnyFiber.as(())
            }
        }
      )
  }
}

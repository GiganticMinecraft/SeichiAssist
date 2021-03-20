package com.github.unchama.datarepository.definitions

import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.SessionMutex

import java.util.UUID

object SessionMutexRepositoryDefinition {

  def withRepositoryContext[
    F[_] : ConcurrentEffect,
    G[_] : Sync : ContextCoercion[*[_], F],
    Player
  ]: RepositoryDefinition[G, Player, SessionMutex[F, G]] = {
    RepositoryDefinition.SinglePhased.withoutTappingAction(
      SinglePhasedRepositoryInitialization.withSupplier(SessionMutex.newIn[F, G]),
      RepositoryFinalization.withoutAnyPersistence[G, UUID, SessionMutex[F, G]] { (_, mutex) =>
        EffectExtra.runAsyncAndForget[F, G, Unit] {
          mutex.stopAnyFiber.as(())
        }
      }
    )
  }
}

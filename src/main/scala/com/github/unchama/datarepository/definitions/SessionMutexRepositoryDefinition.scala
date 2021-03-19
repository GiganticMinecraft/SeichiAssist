package com.github.unchama.datarepository.definitions

import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.datarepository.template.{RepositoryDefinition, RepositoryFinalization, SinglePhasedRepositoryDefinition, SinglePhasedRepositoryInitialization}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.SessionMutex

import java.util.UUID

object SessionMutexRepositoryDefinition {

  import cats.implicits._

  def withRepositoryContext[
    F[_] : ConcurrentEffect,
    G[_] : Sync : ContextCoercion[*[_], F],
    Player
  ]: RepositoryDefinition[G, Player, SessionMutex[F, G]] = {
    SinglePhasedRepositoryDefinition.withoutTappingAction(
      SinglePhasedRepositoryInitialization.withSupplier(SessionMutex.newIn[F, G]),
      RepositoryFinalization.withoutAnyPersistence[G, UUID, SessionMutex[F, G]] { (_, mutex) =>
        EffectExtra.runAsyncAndForget[F, G, Unit] {
          mutex.stopAnyFiber.as(())
        }
      }
    )
  }
}

package com.github.unchama.datarepository.definitions

import cats.effect.{Concurrent, Effect, Sync}
import com.github.unchama.datarepository.template.{RepositoryFinalization, SinglePhasedRepositoryInitialization}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.SessionMutex

object SessionMutexRepositoryDefinitions {

  import cats.implicits._

  def initialization[
    F[_] : Concurrent,
    G[_] : Sync : ContextCoercion[*[_], F]
  ]: SinglePhasedRepositoryInitialization[G, SessionMutex[F, G]] =
    SinglePhasedRepositoryInitialization.withSupplier {
      SessionMutex.newIn[F, G]
    }

  def finalization[
    F[_] : Effect,
    G[_] : Sync,
    Player
  ]: RepositoryFinalization[G, Player, SessionMutex[F, G]] =
    RepositoryFinalization.withoutAnyPersistence[G, Player, SessionMutex[F, G]] { (_, mutex) =>
      EffectExtra.runAsyncAndForget[F, G, Unit] {
        mutex.stopAnyFiber.as(())
      }
    }

}

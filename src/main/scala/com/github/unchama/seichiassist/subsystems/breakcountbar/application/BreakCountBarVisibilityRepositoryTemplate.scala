package com.github.unchama.seichiassist.subsystems.breakcountbar.application

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import cats.{Applicative, Monad}
import com.github.unchama.datarepository.template._
import com.github.unchama.fs2.workaround.Topic
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.{BreakCountBarVisibility, BreakCountBarVisibilityPersistence}

import java.util.UUID

object BreakCountBarVisibilityRepositoryTemplate {

  def getInitialValue[G[_] : Applicative]: G[BreakCountBarVisibility] = Applicative[G].pure(BreakCountBarVisibility.Shown)

  def initialization[
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]],
    Player,
  ](persistence: BreakCountBarVisibilityPersistence[G],
    topic: Topic[F, Option[(Player, BreakCountBarVisibility)]]): TwoPhasedRepositoryInitialization[G, Player, Ref[G, BreakCountBarVisibility]] =
    SignallingRepositoryInitialization.againstPlayerTopic(topic) {
      TwoPhasedRepositoryInitialization.canonicallyFrom {
        RefDictBackedRepositoryInitialization.usingUuidRefDict(persistence)(getInitialValue[G])
      }
    }

  def finalization[
    F[_] : Monad, Player
  ](persistence: BreakCountBarVisibilityPersistence[F])
   (keyExtractor: Player => UUID): RepositoryFinalization[F, Player, Ref[F, BreakCountBarVisibility]] =
    RepositoryFinalization.liftToRefFinalization {
      RefDictBackedRepositoryFinalization.usingUuidRefDict(persistence).contraMapKey(keyExtractor)
    }
}

package com.github.unchama.seichiassist.subsystems.breakcountbar.domain

import cats.Applicative
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.datarepository.template._
import com.github.unchama.generic.ContextCoercion
import fs2.concurrent.Topic

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
    F[_] : Applicative, Player
  ](persistence: BreakCountBarVisibilityPersistence[F])
   (keyExtractor: Player => UUID): RepositoryFinalization[F, Player, BreakCountBarVisibility] =
    RefDictBackedRepositoryFinalization.using(persistence)(keyExtractor)
}

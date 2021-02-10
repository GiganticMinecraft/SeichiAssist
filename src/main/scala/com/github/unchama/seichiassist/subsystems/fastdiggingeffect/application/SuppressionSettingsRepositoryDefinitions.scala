package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application

import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import cats.{Applicative, Monad}
import com.github.unchama.datarepository.template._
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.{FastDiggingEffectSuppressionState, FastDiggingEffectSuppressionStatePersistence}
import fs2.concurrent.Topic

import java.util.UUID

object SuppressionSettingsRepositoryDefinitions {

  type RepositoryValue[F[_]] = Ref[F, FastDiggingEffectSuppressionState]

  def getInitialValue[G[_] : Applicative]: G[FastDiggingEffectSuppressionState] =
    Applicative[G].pure(FastDiggingEffectSuppressionState.EnabledWithoutLimit)

  def initialization[
    F[_] : ConcurrentEffect,
    Player
  ](persistence: FastDiggingEffectSuppressionStatePersistence[F],
    topic: Topic[F, Option[(Player, FastDiggingEffectSuppressionState)]])
  : TwoPhasedRepositoryInitialization[F, Player, RepositoryValue[F]] = {
    SignallingRepositoryInitialization.againstPlayerTopic(topic) {
      TwoPhasedRepositoryInitialization.canonicallyFrom {
        RefDictBackedRepositoryInitialization.usingUuidRefDict(persistence)(getInitialValue[F])
      }
    }
  }

  def finalization[
    F[_] : Monad,
    Player
  ](persistence: FastDiggingEffectSuppressionStatePersistence[F])
   (keyExtractor: Player => UUID)
  : RepositoryFinalization[F, Player, RepositoryValue[F]] = {
    RepositoryFinalization.liftToRefFinalization {
      RefDictBackedRepositoryFinalization.using(persistence)(keyExtractor)
    }
  }
}

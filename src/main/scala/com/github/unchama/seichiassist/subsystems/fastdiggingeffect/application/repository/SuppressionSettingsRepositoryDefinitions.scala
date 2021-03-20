package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.repository

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.{Applicative, Monad}
import com.github.unchama.datarepository.template._
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.{SinglePhasedRepositoryInitialization, TwoPhasedRepositoryInitialization}
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.{FastDiggingEffectSuppressionState, FastDiggingEffectSuppressionStatePersistence}

import java.util.UUID

object SuppressionSettingsRepositoryDefinitions {

  type RepositoryValue[F[_]] = Ref[F, FastDiggingEffectSuppressionState]

  def getInitialValue[G[_] : Applicative]: G[FastDiggingEffectSuppressionState] =
    Applicative[G].pure(FastDiggingEffectSuppressionState.EnabledWithoutLimit)

  def initialization[
    F[_] : Sync,
    Player
  ](persistence: FastDiggingEffectSuppressionStatePersistence[F])
  : TwoPhasedRepositoryInitialization[F, Player, RepositoryValue[F]] =
    TwoPhasedRepositoryInitialization.canonicallyFrom {
      SinglePhasedRepositoryInitialization.forRefCell {
        RefDictBackedRepositoryInitialization.usingUuidRefDict(persistence)(getInitialValue[F])
      }
    }

  def finalization[
    F[_] : Monad,
    Player
  ](persistence: FastDiggingEffectSuppressionStatePersistence[F])
   (keyExtractor: Player => UUID)
  : RepositoryFinalization[F, Player, RepositoryValue[F]] = {
    RepositoryFinalization.liftToRefFinalization {
      RefDictBackedRepositoryFinalization.usingUuidRefDict(persistence).contraMapKey(keyExtractor)
    }
  }
}

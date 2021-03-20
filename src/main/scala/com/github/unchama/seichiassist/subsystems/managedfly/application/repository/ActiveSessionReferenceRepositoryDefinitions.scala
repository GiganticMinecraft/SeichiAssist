package com.github.unchama.seichiassist.subsystems.managedfly.application.repository

import cats.effect.{ConcurrentEffect, Effect, Sync, SyncEffect}
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.{PrefetchResult, TwoPhasedRepositoryInitialization}
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.managedfly.application.{ActiveSessionFactory, ActiveSessionReference, FlyDurationPersistenceRepository}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{PlayerFlyStatus, RemainingFlyDuration}

import java.util.UUID

object ActiveSessionReferenceRepositoryDefinitions {

  type RepositoryValue[F[_], G[_]] = ActiveSessionReference[F, G]

  import cats.implicits._

  def initialization[
    F[_] : ConcurrentEffect,
    G[_] : SyncEffect,
    Player
  ](factory: ActiveSessionFactory[F, Player],
    persistence: FlyDurationPersistenceRepository[G, UUID])
  : TwoPhasedRepositoryInitialization[G, Player, RepositoryValue[F, G]] =
    new TwoPhasedRepositoryInitialization[G, Player, RepositoryValue[F, G]] {

      override type IntermediateData = Option[RemainingFlyDuration]

      override val prefetchIntermediateValue: (UUID, String) => G[PrefetchResult[IntermediateData]] = {
        (uuid, _) => persistence.read(uuid).map { data => PrefetchResult.Success(data) }
      }

      override val prepareData: (Player, IntermediateData) => G[RepositoryValue[F, G]] = {
        (player, data) =>
          ActiveSessionReference
            .createNew[F, G]
            .flatTap { reference =>
              EffectExtra.runAsyncAndForget[F, G, Option[Unit]] {
                data.traverse { duration =>
                  reference.replaceSession(factory.start[G](duration).run(player))
                }
              }
            }
      }
    }

  def finalization[
    F[_] : Effect,
    G[_] : Sync,
    Player: HasUuid
  ](persistence: FlyDurationPersistenceRepository[G, UUID]): RepositoryFinalization[G, Player, RepositoryValue[F, G]] =
    new RepositoryFinalization[G, Player, RepositoryValue[F, G]] {
      override val persistPair: (Player, RepositoryValue[F, G]) => G[Unit] =
        (player, sessionRef) => for {
          latestStatus <- sessionRef.getLatestFlyStatus
          _ <- persistence.writePair(HasUuid[Player].of(player), PlayerFlyStatus.toDurationOption(latestStatus))
        } yield ()

      override val finalizeBeforeUnload: (Player, RepositoryValue[F, G]) => G[Unit] =
        (_, sessionRef) => EffectExtra.runAsyncAndForget[F, G, Unit] {
          sessionRef.stopAnyRunningSession.as(())
        }
    }
}

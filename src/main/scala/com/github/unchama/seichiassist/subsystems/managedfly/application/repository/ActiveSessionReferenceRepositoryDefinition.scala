package com.github.unchama.seichiassist.subsystems.managedfly.application.repository

import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.managedfly.application.{
  ActiveSessionFactory,
  ActiveSessionReference,
  FlyDurationPersistenceRepository
}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{
  PlayerFlyStatus,
  RemainingFlyDuration
}

object ActiveSessionReferenceRepositoryDefinition {

  import cats.implicits._

  def withContext[F[_]: ConcurrentEffect, G[_]: SyncEffect, Player: HasUuid](
    factory: ActiveSessionFactory[F, Player],
    persistence: FlyDurationPersistenceRepository[G]
  ): RepositoryDefinition[G, Player, ActiveSessionReference[F, G]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[G, Player, Option[RemainingFlyDuration]](persistence)(None)
      .toTwoPhased
      .flatXmapWithPlayerAndIntermediateEffects(player =>
        data =>
          ActiveSessionReference.createNew[F, G].flatTap { reference =>
            EffectExtra.runAsyncAndForget[F, G, Option[Unit]] {
              data.traverse { duration =>
                reference.replaceSession(factory.start[G](duration).run(player))
              }
            }
          }
      )(sessionRef => sessionRef.getLatestFlyStatus.map(PlayerFlyStatus.toDurationOption))(
        sessionRef =>
          EffectExtra.runAsyncAndForget[F, G, Unit] {
            sessionRef.stopAnyRunningSession.as(())
          } >> sessionRef.getLatestFlyStatus.map(PlayerFlyStatus.toDurationOption)
      )
}

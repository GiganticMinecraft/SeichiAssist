package com.github.unchama.seichiassist.subsystems.gachapoint.application.repository

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Sync, Timer}
import com.github.unchama.datarepository.definitions.RefDictBackedRepositoryDefinition
import com.github.unchama.datarepository.template._
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.{
  GachaPoint,
  GachaPointPersistence
}
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.{
  BatchUsageSemaphore,
  GrantGachaTicketToAPlayer
}

object GachaPointRepositoryDefinition {

  type TemporaryValue[F[_]] = Ref[F, GachaPoint]

  case class RepositoryValue[F[_], G[_]](
    pointRef: Ref[G, GachaPoint],
    semaphore: BatchUsageSemaphore[F, G]
  )

  import cats.implicits._

  def withContext[G[_]: Sync: ContextCoercion[*[_], F], F[
    _
  ]: Concurrent: Timer, Player: HasUuid](persistence: GachaPointPersistence[G])(
    grantEffectFactory: Player => GrantGachaTicketToAPlayer[F]
  ): RepositoryDefinition[G, Player, RepositoryValue[F, G]] =
    RefDictBackedRepositoryDefinition
      .usingUuidRefDict[G, Player, GachaPoint](persistence)(GachaPoint.initial)
      .toRefRepository
      .augmentToTwoPhased((player, ref) =>
        BatchUsageSemaphore
          .newIn[G, F](ref, grantEffectFactory(player))
          .map(RepositoryValue(ref, _))
      )(value => Monad[G].pure(value.pointRef))

}

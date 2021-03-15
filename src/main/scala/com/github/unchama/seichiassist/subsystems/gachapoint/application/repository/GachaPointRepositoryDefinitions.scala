package com.github.unchama.seichiassist.subsystems.gachapoint.application.repository

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Sync, Timer}
import cats.{Applicative, Monad}
import com.github.unchama.datarepository.template._
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.{GachaPoint, GachaPointPersistence}
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.{BatchUsageSemaphore, GrantGachaTicketToAPlayer}

object GachaPointRepositoryDefinitions {

  type TemporaryValue[F[_]] = Ref[F, GachaPoint]

  case class RepositoryValue[F[_], G[_]](pointRef: Ref[G, GachaPoint],
                                         semaphore: BatchUsageSemaphore[F, G])

  private def defaultGachaPoint[F[_] : Applicative]: F[GachaPoint] =
    Applicative[F].pure(GachaPoint.initial)

  import cats.implicits._

  import scala.util.chaining._

  def initialization[
    G[_] : Sync : ContextCoercion[*[_], F],
    F[_] : Concurrent : Timer,
    Player
  ](persistence: GachaPointPersistence[G])
   (grantEffectFactory: Player => GrantGachaTicketToAPlayer[F])
  : TwoPhasedRepositoryInitialization[G, Player, RepositoryValue[F, G]] =
    RefDictBackedRepositoryInitialization
      .usingUuidRefDict(persistence)(defaultGachaPoint[G])
      .pipe(SinglePhasedRepositoryInitialization.forRefCell[G, GachaPoint])
      .pipe(TwoPhasedRepositoryInitialization.augment(_)((player, ref) => {
        BatchUsageSemaphore
          .newIn[G, F](ref, grantEffectFactory(player))
          .map(RepositoryValue(ref, _))
      }))

  def finalization[
    G[_] : Monad,
    F[_],
    Player: HasUuid
  ](persistence: GachaPointPersistence[G]): RepositoryFinalization[G, Player, RepositoryValue[F, G]] =
    RefDictBackedRepositoryFinalization
      .usingUuidRefDict(persistence)
      .pipe(RepositoryFinalization.liftToRefFinalization)
      .contraMapKey(HasUuid[Player].asFunction)
      .contraMap[RepositoryValue[F, G]](_.pointRef)

}

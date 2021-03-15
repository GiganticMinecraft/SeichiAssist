package com.github.unchama.seichiassist.subsystems.gachapoint.application.repository

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Timer}
import cats.{Applicative, Monad}
import com.github.unchama.datarepository.template._
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.{GachaPoint, GachaPointPersistence}
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.{BatchUsageSemaphore, GrantGachaTicketToAPlayer}

object GachaPointRepositoryDefinitions {

  type TemporaryValue[F[_]] = Ref[F, GachaPoint]

  case class RepositoryValue[F[_]](pointRef: Ref[F, GachaPoint],
                                   semaphore: BatchUsageSemaphore[F])

  def defaultGachaPoint[F[_]: Applicative]: F[GachaPoint] =
    Applicative[F].pure(GachaPoint.initial)

  import cats.implicits._

  import scala.util.chaining._

  def initialization[
    F[_]: Concurrent: Timer,
    Player
  ](persistence: GachaPointPersistence[F])
   (grantEffectFactory: Player => GrantGachaTicketToAPlayer[F]): TwoPhasedRepositoryInitialization[F, Player, RepositoryValue[F]] =
    RefDictBackedRepositoryInitialization
      .usingUuidRefDict(persistence)(defaultGachaPoint[F])
      .pipe(SinglePhasedRepositoryInitialization.forRefCell[F, GachaPoint])
      .pipe(TwoPhasedRepositoryInitialization.augment(_)((player, ref) => {
        BatchUsageSemaphore
          .newIn[F, F](ref, grantEffectFactory(player))
          .map(RepositoryValue(ref, _))
      }))

  def finalization[
    F[_]: Monad,
    Player: HasUuid
  ](persistence: GachaPointPersistence[F]): RepositoryFinalization[F, Player, RepositoryValue[F]] =
    RefDictBackedRepositoryFinalization
      .usingUuidRefDict(persistence)
      .pipe(RepositoryFinalization.liftToRefFinalization)
      .contraMap[RepositoryValue[F]](_.pointRef)

}

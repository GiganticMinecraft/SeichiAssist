package com.github.unchama.datarepository.template

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.{Applicative, Apply, Monad}
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.{SinglePhasedRepositoryInitialization, TwoPhasedRepositoryInitialization}
import com.github.unchama.minecraft.algebra.HasUuid

import java.util.UUID

sealed trait RepositoryDefinition[F[_], Player, R] {

  type Self[S] <: RepositoryDefinition[F, Player, S]

  def toTwoPhased(implicit F: Monad[F], playerHasUuid: HasUuid[Player]): RepositoryDefinition.TwoPhased[F, Player, R] =
    this match {
      case s@RepositoryDefinition.SinglePhased(_, _, _) => s.augmentToTwoPhased((_, r) => F.pure(r))(F.pure[R])
      case t@RepositoryDefinition.TwoPhased(_, _) => t
    }

  def flatXmapWithIntermediateEffects[S](f: R => F[S])
                                        (beforePersisting: S => F[R])(beforeFinalization: S => F[R])
                                        (implicit F: Monad[F]): Self[S]

  def flatXmap[S](f: R => F[S])(g: S => F[R])(implicit F: Monad[F]): Self[S] =
    flatXmapWithIntermediateEffects(f)(g)(g)

  def xmap[S](f: R => S)(g: S => R)(implicit F: Monad[F]): Self[S] =
    flatXmap(r => F.pure(f(r)))(s => F.pure(g(s)))

  def toRefRepository(implicit F: Sync[F]): Self[Ref[F, R]] =
    flatXmap(r => Ref.of[F, R](r))(ref => ref.get)

  def augmentF[S](f: R => F[S])(implicit F: Monad[F]): Self[(R, S)] =
    flatXmap(r => F.map(f(r))(s => (r, s)))(rs => F.pure(rs._1))
}

object RepositoryDefinition {

  import cats.implicits._

  case class SinglePhased[F[_], Player, R](initialization: SinglePhasedRepositoryInitialization[F, R],
                                           tappingAction: (Player, R) => F[Unit],
                                           finalization: RepositoryFinalization[F, UUID, R])
    extends RepositoryDefinition[F, Player, R] {

    override type Self[S] = SinglePhased[F, Player, S]

    override def flatXmapWithIntermediateEffects[S](f: R => F[S])
                                                   (beforePersisting: S => F[R])(beforeFinalization: S => F[R])
                                                   (implicit F: Monad[F]): SinglePhased[F, Player, S] =
      RepositoryDefinition.SinglePhased(
        (uuid, name) => initialization.prepareData(uuid, name).flatMap(_.traverse(f)),
        (player, s) => beforePersisting(s).flatMap(tappingAction(player, _)),
        finalization.withIntermediateEffects(beforePersisting)(beforeFinalization)
      )

    def withAnotherTappingAction(another: (Player, R) => F[Unit])
                                (implicit F: Apply[F]): SinglePhased[F, Player, R] = this.copy(
      tappingAction = (player, r) => F.productR(tappingAction(player, r))(another(player, r))
    )

    def augmentToTwoPhased[T](prepareFinalData: (Player, R) => F[T])(revertOnFinalization: T => F[R])
                             (implicit F: Monad[F], playerHasUuid: HasUuid[Player]): TwoPhased[F, Player, T] =
      TwoPhased(
        TwoPhasedRepositoryInitialization.augment(initialization)((player, r) =>
          tappingAction(player, r) >> prepareFinalData(player, r)
        ),
        finalization
          .withIntermediateEffect(revertOnFinalization)
          .contraMapKey(playerHasUuid.asFunction)
      )
  }

  object SinglePhased {
    def withoutTappingAction[
      F[_] : Applicative, Player, R
    ](initialization: SinglePhasedRepositoryInitialization[F, R],
      finalization: RepositoryFinalization[F, UUID, R]): SinglePhased[F, Player, R] = {
      SinglePhased(initialization, (_, _) => Applicative[F].unit, finalization)
    }
  }

  case class TwoPhased[F[_], Player, R](initialization: TwoPhasedRepositoryInitialization[F, Player, R],
                                        finalization: RepositoryFinalization[F, Player, R])
    extends RepositoryDefinition[F, Player, R] {
    override type Self[S] = TwoPhased[F, Player, S]

    def flatXmapWithPlayerAndIntermediateEffects[S](f: Player => R => F[S])
                                                   (beforePersisting: S => F[R])(beforeFinalization: S => F[R])
                                                   (implicit F: Monad[F]): TwoPhased[F, Player, S] =
      RepositoryDefinition.TwoPhased(
        initialization.extendPreparation(f),
        finalization.withIntermediateEffects(beforePersisting)(beforeFinalization)
      )

    override def flatXmapWithIntermediateEffects[S](f: R => F[S])
                                                   (beforePersisting: S => F[R])(beforeFinalization: S => F[R])
                                                   (implicit F: Monad[F]): TwoPhased[F, Player, S] =
      flatXmapWithPlayerAndIntermediateEffects(_ => f)(beforePersisting)(beforeFinalization)

    def xmapWithPlayer[S](f: Player => R => F[S])(g: S => F[R])(implicit F: Monad[F]): TwoPhased[F, Player, S] =
      flatXmapWithPlayerAndIntermediateEffects(f)(g)(g)
  }

}

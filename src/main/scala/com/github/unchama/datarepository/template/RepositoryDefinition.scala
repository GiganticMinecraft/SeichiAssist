package com.github.unchama.datarepository.template

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.{Applicative, Monad}

import java.util.UUID

sealed trait RepositoryDefinition[F[_], Player, R] {

  import cats.implicits._

  def flatXmap[S](f: R => F[S])(g: S => F[R])(implicit F: Monad[F]): RepositoryDefinition[F, Player, S] = {
    this match {
      case RepositoryDefinition.SinglePhased(initialization, tappingAction, finalization) =>
        RepositoryDefinition.SinglePhased(
          (uuid, name) => initialization.prepareData(uuid, name).flatMap(_.traverse(f)),
          (player, s) => g(s).flatMap(tappingAction(player, _)),
          finalization.withIntermediateEffect(g)
        )
      case RepositoryDefinition.TwoPhased(initialization, finalization) =>
        RepositoryDefinition.TwoPhased(
          new TwoPhasedRepositoryInitialization[F, Player, S] {
            override type IntermediateData = initialization.IntermediateData
            override val prefetchIntermediateValue: (UUID, String) => F[PrefetchResult[IntermediateData]] =
              initialization.prefetchIntermediateValue
            override val prepareData: (Player, IntermediateData) => F[S] =
              (player, i) => initialization.prepareData(player, i).flatMap(f)
          },
          finalization.withIntermediateEffect(g)
        )
    }
  }

  def imap[S](f: R => S)(g: S => R)(implicit F: Monad[F]): RepositoryDefinition[F, Player, S] =
    flatXmap(r => F.pure(f(r)))(s => F.pure(g(s)))

  def toRefRepository(implicit F: Sync[F]): RepositoryDefinition[F, Player, Ref[F, R]] =
    flatXmap(r => Ref.of[F, R](r))(ref => ref.get)

  def augmentF[S](f: R => F[S])(implicit F: Monad[F]): RepositoryDefinition[F, Player, (R, S)] =
    flatXmap(r => F.map(f(r))(s => (r, s)))(rs => F.pure(rs._1))
}

object RepositoryDefinition {

  case class SinglePhased[F[_], Player, R](initialization: SinglePhasedRepositoryInitialization[F, R],
                                           tappingAction: (Player, R) => F[Unit],
                                           finalization: RepositoryFinalization[F, UUID, R])
    extends RepositoryDefinition[F, Player, R]

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
    extends RepositoryDefinition[F, Player, R]

}

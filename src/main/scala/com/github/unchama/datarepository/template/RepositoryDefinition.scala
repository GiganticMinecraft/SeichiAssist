package com.github.unchama.datarepository.template

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.{Applicative, Apply, Monad}
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.{PrefetchResult, SinglePhasedRepositoryInitialization, TwoPhasedRepositoryInitialization}

import java.util.UUID

sealed trait RepositoryDefinition[F[_], Player, R] {

  type Self[S] <: RepositoryDefinition[F, Player, S]

  def flatXmap[S](f: R => F[S])(g: S => F[R])(implicit F: Monad[F]): Self[S]

  def imap[S](f: R => S)(g: S => R)(implicit F: Monad[F]): Self[S] =
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

    override def flatXmap[S](f: R => F[S])
                            (g: S => F[R])
                            (implicit F: Monad[F]): SinglePhased[F, Player, S] =
      RepositoryDefinition.SinglePhased(
        (uuid, name) => initialization.prepareData(uuid, name).flatMap(_.traverse(f)),
        (player, s) => g(s).flatMap(tappingAction(player, _)),
        finalization.withIntermediateEffect(g)
      )

    def withAnotherTappingAction(another: (Player, R) => F[Unit])
                                (implicit F: Apply[F]): SinglePhased[F, Player, R] = this.copy(
      tappingAction = (player, r) => F.productR(tappingAction(player, r))(another(player, r))
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

    override def flatXmap[S](f: R => F[S])
                            (g: S => F[R])
                            (implicit F: Monad[F]): TwoPhased[F, Player, S] =
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

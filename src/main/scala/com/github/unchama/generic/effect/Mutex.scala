package com.github.unchama.generic.effect

import cats.Monad
import cats.data.OptionT
import cats.effect.concurrent.MVar
import cats.effect.{Concurrent, Sync}

final class Mutex[F[_]: Monad, A] private(mVar: MVar[F, A]) {
  import cats.implicits._

  def lockAndModify[B](use: A => F[(A, B)]): F[B] =
    for {
      a <- mVar.take
      newAB <- use(a)
      (newA, newB) = newAB
      _ <- mVar.put(newA)
    } yield newB

  def tryLockAndModify[B](use: A => F[(A, B)]): F[Option[B]] = {
    for {
      a <- OptionT(mVar.tryTake)
      newAB <- OptionT.liftF(use(a))
      (newA, newB) = newAB
      _ <- OptionT.liftF(mVar.put(newA))
    } yield newB
  }.value
}

object Mutex {
  import cats.implicits._

  def of[F[_]: Sync, G[_]: Concurrent, A](initial: A): F[Mutex[G, A]] =
    MVar.in[F, G, A](initial) >>= (mv => Sync[F].delay(new Mutex(mv)))
}

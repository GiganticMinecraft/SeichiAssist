package com.github.unchama.generic

import cats.effect.Sync

trait ReadWrite[F[_], A] {
  val read: F[A]

  def write(a: A): F[Unit]
}

object ReadWrite {
  def from[F[_], A](_read: F[A], _write: A => F[Unit]): ReadWrite[F, A] = new ReadWrite[F, A] {
    override val read: F[A] = _read

    override def write(a: A): F[Unit] = _write(a)
  }

  def liftUnsafe[F[_]: Sync, A](_read: => A, _write: => A => Unit): ReadWrite[F, A] =
    from(Sync[F].delay(_read), a => Sync[F].delay(_write(a)))
}

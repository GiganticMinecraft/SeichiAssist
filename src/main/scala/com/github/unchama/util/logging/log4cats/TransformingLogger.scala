package com.github.unchama.util.logging.log4cats

import io.chrisdavenport.log4cats.Logger

class TransformingLogger[F[_]: Logger](f: String => String) extends Logger[F] {
  override def error(t: Throwable)(message: => String): F[Unit] = Logger[F].error(t)(f(message))

  override def warn(t: Throwable)(message: => String): F[Unit] = Logger[F].warn(t)(f(message))

  override def info(t: Throwable)(message: => String): F[Unit] = Logger[F].info(t)(f(message))

  override def debug(t: Throwable)(message: => String): F[Unit] = Logger[F].debug(t)(f(message))

  override def trace(t: Throwable)(message: => String): F[Unit] = Logger[F].trace(t)(f(message))

  override def error(message: => String): F[Unit] = Logger[F].error(f(message))

  override def warn(message: => String): F[Unit] = Logger[F].warn(f(message))

  override def info(message: => String): F[Unit] = Logger[F].info(f(message))

  override def debug(message: => String): F[Unit] = Logger[F].debug(f(message))

  override def trace(message: => String): F[Unit] = Logger[F].trace(f(message))
}

object TransformingLogger {

  def apply[F[_]: Logger](f: String => String): TransformingLogger[F] =
    new TransformingLogger[F](f)

}

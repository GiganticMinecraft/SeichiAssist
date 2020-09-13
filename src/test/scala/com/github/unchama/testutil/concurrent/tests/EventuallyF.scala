package com.github.unchama.testutil.concurrent.tests

import cats.effect.Sync
import org.scalactic.source
import org.scalactic.source.Position
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration.{Interval, Timeout}
import org.scalatest.enablers.Retrying

trait EventuallyF extends Eventually {

  def eventuallyF[F[_] : Sync, T](fun: => T)
                                 (implicit retrying: Retrying[T], pos: source.Position): F[T] = {
    Sync[F].delay {
      eventually(fun)
    }
  }

  def eventuallyF[F[_] : Sync, T](interval: Interval)(fun: => T)
                                 (implicit retrying: Retrying[T], pos: Position): F[T] = {
    Sync[F].delay {
      eventually(interval)(fun)
    }
  }

  def eventuallyF[F[_] : Sync, T](timeout: Timeout)(fun: => T)
                                 (implicit retrying: Retrying[T], pos: Position): F[T] = {
    Sync[F].delay {
      eventually(timeout)(fun)
    }
  }

  def eventuallyF[F[_] : Sync, T](timeout: Timeout, interval: Interval)(fun: => T)
                                 (implicit retrying: Retrying[T], pos: Position): F[T] = {
    Sync[F].delay {
      eventually(timeout, interval)(fun)
    }
  }

}

object EventuallyF extends EventuallyF

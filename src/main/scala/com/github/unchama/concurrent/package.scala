package com.github.unchama

import cats.effect.ContextShift
import com.github.unchama.generic.tag.tag.@@

import scala.concurrent.ExecutionContext

package object concurrent {

  trait RepeatingTaskContextTag

  type RepeatingTaskContext = ExecutionContext @@ RepeatingTaskContextTag

  trait NonServerThreadContextShiftTag

  type NonServerThreadContextShift[F[_]] = ContextShift[F] @@ NonServerThreadContextShiftTag

}

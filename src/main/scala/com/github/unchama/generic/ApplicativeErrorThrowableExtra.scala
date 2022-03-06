package com.github.unchama.generic

import cats.ApplicativeError
import io.chrisdavenport.log4cats.ErrorLogger

object ApplicativeErrorThrowableExtra {

  import cats.implicits._

  def recoverWithStackTrace[F[_]: ApplicativeError[*[_], Throwable]: ErrorLogger, A](
    action: F[A]
  )(message: String, recover: => A): F[A] =
    action.handleErrorWith { error => ErrorLogger[F].error(error)(message).as(recover) }

}

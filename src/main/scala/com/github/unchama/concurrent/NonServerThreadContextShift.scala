package com.github.unchama.concurrent

object NonServerThreadContextShift {

  def apply[F[_]](implicit cs: NonServerThreadContextShift[F]): NonServerThreadContextShift[F] = cs

}

package com.github.unchama.concurrent

import cats.effect.concurrent.Ref

trait ReadOnlyRef[F[_], A] {

  /**
   * `A` の値を `F` のコンテキストで読み出す
   */
  def read: F[A]

}

object ReadOnlyRef {

  def fromAnySource[F[_], A](fa: F[A]): ReadOnlyRef[F, A] = new ReadOnlyRef[F, A] {
    override def read: F[A] = fa
  }

  def fromRef[F[_], A](ref: Ref[F, A]): ReadOnlyRef[F, A] = fromAnySource(ref.get)

}

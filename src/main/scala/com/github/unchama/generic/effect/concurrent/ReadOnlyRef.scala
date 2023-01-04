package com.github.unchama.generic.effect.concurrent

import cats.Functor
import cats.effect.concurrent.Ref

trait ReadOnlyRef[F[_], A] {

  /**
   * `A` の値を `F` のコンテキストで読み出す
   */
  def read: F[A]

  /**
   * 読みだした値が `f` で変換される新しい `ReadOnlyRef` を作成する
   */
  def map[B](f: A => B)(implicit F: Functor[F]): ReadOnlyRef[F, B] =
    ReadOnlyRef.fromAnySource(F.map(read)(f))

}

object ReadOnlyRef {

  def fromAnySource[F[_], A](fa: F[A]): ReadOnlyRef[F, A] = new ReadOnlyRef[F, A] {
    override def read: F[A] = fa
  }

  def fromRef[F[_], A](ref: Ref[F, A]): ReadOnlyRef[F, A] = fromAnySource(ref.get)

}

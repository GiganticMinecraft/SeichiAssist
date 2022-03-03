package com.github.unchama.generic

import cats.Eq

/**
 * 異なる二つの値を保持するタプル。
 *
 * [[A]] に関連付いた [[Eq]] インスタンスによって [[left]] と [[right]] が等価でないと判定されることが保証される。
 */
case class Diff[A: Eq] private (left: A, right: A)

object Diff {

  import cats.implicits._

  def ofPairBy[A, B: Eq](pair: (A, A))(f: A => B): Option[Diff[B]] =
    fromValues(f(pair._1), f(pair._2))

  def fromValues[A: Eq](left: A, right: A): Option[Diff[A]] =
    if (left neqv right) Some(Diff(left, right)) else None

  def ofPair[A: Eq](pair: (A, A)): Option[Diff[A]] = fromValues(pair._1, pair._2)

}

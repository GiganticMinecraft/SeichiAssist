package com.github.unchama.util

import cats.Eq

/**
 * 異なる二つの値を保持するタプル。
 *
 * [[A]] に関連付いた [[Eq]] インスタンスによって
 * [[left]] と [[right]] が等価でないと判定されることが保証される。
 */
case class Diff[A: Eq] private(left: A, right: A)

object Diff {

  import cats.implicits._

  def fromValues[A: Eq](left: A, right: A): Option[Diff[A]] =
    if (left neqv right) Some(Diff(left, right)) else None
}

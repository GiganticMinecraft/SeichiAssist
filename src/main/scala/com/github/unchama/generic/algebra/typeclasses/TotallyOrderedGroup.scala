package com.github.unchama.generic.algebra.typeclasses

import cats.{Group, Order}

/**
 * 全順序群。
 *
 * 任意の `a lteqv b` な `a: A, b: A, c: A` について、
 *  - `combine(a, c) lteqv combine(b, c)`
 *  - `combine(c, a) lteqv combine(c, b)`
 *
 * が成り立つ。
 */
trait TotallyOrderedGroup[A] extends Order[A] with Group[A]

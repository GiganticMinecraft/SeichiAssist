package com.github.unchama.generic

trait Cloneable[T] {

  /**
   * @return [[T]]をクローンして返します。
   */
  def clone(x: T): T

}

object Cloneable {

  def apply[T](implicit ev: Cloneable[T]): Cloneable[T] = ev

}

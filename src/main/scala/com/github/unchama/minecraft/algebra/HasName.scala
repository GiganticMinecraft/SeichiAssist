package com.github.unchama.minecraft.algebra

trait HasName[T] {

  def of(x: T): String

}

object HasName {

  def apply[T](implicit ev: HasName[T]): HasName[T] = ev

}

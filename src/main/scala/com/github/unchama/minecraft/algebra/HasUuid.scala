package com.github.unchama.minecraft.algebra

import java.util.UUID

trait HasUuid[T] {

  def of(x: T): UUID

}

object HasUuid {

  def apply[T](implicit ev: HasUuid[T]): HasUuid[T] = ev

}

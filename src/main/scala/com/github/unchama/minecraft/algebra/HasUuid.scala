package com.github.unchama.minecraft.algebra

import java.util.UUID

trait HasUuid[T] {

  def of(x: T): UUID

  def asFunction: T => UUID = t => of(t)

}

object HasUuid {

  def apply[T](using ev: HasUuid[T]): HasUuid[T] = ev

  given trivial: HasUuid[UUID] = x => x

}

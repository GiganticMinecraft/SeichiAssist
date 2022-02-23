package com.github.unchama.minecraft.algebra

import java.util.UUID

trait HasUuid[T] {

  def of(x: T): UUID

  def asFunction: T => UUID = t => of(t)

}

object HasUuid {

  def apply[T](implicit ev: HasUuid[T]): HasUuid[T] = ev

  implicit val trivial: HasUuid[UUID] = x => x

}

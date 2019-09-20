package com.github.unchama.util.syntax
object Nullability {
  implicit class NullabilityExtensionReceiver[T](val receiver: T) extends AnyVal {
    def ifNull(f: => T): T = if (receiver == null) f else receiver
    def ifNotNull[R <: AnyRef](f: T => R): R = if (receiver != null) f(receiver) else null.asInstanceOf[R]
  }

  def ListNotNull[T](elements: T*): Seq[T] = List(elements: _*).filter(_ != null)
}

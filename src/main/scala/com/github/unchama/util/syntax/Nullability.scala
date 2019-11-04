package com.github.unchama.util.syntax

import scala.language.implicitConversions

trait NullabilitySyntax {
  implicit def toNullabilityExtensionReceiverOps[T](x: T): Nullability.NullabilityExtensionReceiver[T] =
    new Nullability.NullabilityExtensionReceiver(x)
}

object Nullability {

  implicit class NullabilityExtensionReceiver[T](val receiver: T) extends AnyVal {
    def ifNull(f: => T): T = if (receiver == null) f else receiver

    def ifNotNull[R](f: T => R): R = if (receiver != null) f(receiver) else null.asInstanceOf[R]
  }

  // TODO move this away
  def ListNotNull[T](elements: T*): Seq[T] = List(elements: _*).filter(_ != null)
}

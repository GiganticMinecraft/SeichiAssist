package com.github.unchama.util.syntax

object Nullability {
  implicit class NullabilityExtensionReceiver[T](val receiver: T) extends AnyVal {
    def ifNull(f: => T): T = if (receiver == null) f else receiver
  }
}

package com.github.unchama.util.syntax

import scala.language.implicitConversions

trait ApplySyntax {
  implicit def toApplyOps[T](x: T): Modify.ModifyOps[T] = new Modify.ModifyOps(x)
}

object Modify {

  // TODO replace with Scala 2.13's tap in scala.util.chaining
  implicit class ModifyOps[T](x: T) {
    def modify(f: T => Unit): T = {
      f(x); x
    }
  }

}

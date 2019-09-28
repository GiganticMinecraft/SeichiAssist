package com.github.unchama.util.syntax

trait ApplySyntax {
  implicit def toApplyOps[T](x: T): Modify.ModifyOps[T] = new Modify.ModifyOps(x)
}

object Modify {
  implicit case class ModifyOps[T](x: T) {
    def modify(f: T => Unit): T = { f(x); x }
  }
}

package com.github.unchama.targetedeffect

import kotlin.coroutines.Continuation

case class UnfocusedEffect(effect: Continuation[Unit] => Any => Unit) extends TargetedEffect[Any] {
  override def runFor(minecraftObject: Any, continuation: Continuation[Unit]) = effect(continuation)(minecraftObject)
}

object UnfocusedEffect {
  /**
   * 副作用を持つ一般の[effect]を[UnfocusedEffect]として扱えるように変換する.
   */
  def apply(effect: Continuation[Unit] => Unit): UnfocusedEffect =
    new UnfocusedEffect((cont: Continuation[Unit]) => (_: Any) => effect(cont))
}

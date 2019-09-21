package com.github.unchama.targetedeffect

import com.github.unchama.util.kotlin2scala.SuspendingMethod
import kotlin.coroutines.Continuation

case class UnfocusedEffect(effect: Continuation[Unit] => Any => Unit) extends TargetedEffect[Any] {
  override @SuspendingMethod def runFor(minecraftObject: Any) = effect(continuation)(minecraftObject)
}

object UnfocusedEffect {
  /**
   * 副作用を持つ一般の[effect]を[UnfocusedEffect]として扱えるように変換する.
   */
  def apply(effect: Continuation[Unit] => Unit): UnfocusedEffect =
    new UnfocusedEffect((cont: Continuation[Unit]) => (_: Any) => effect(cont))
}

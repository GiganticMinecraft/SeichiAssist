package com.github.unchama.targetedeffect

import com.github.unchama.util.kotlin2scala.SuspendingMethod
import kotlin.coroutines.Continuation

case class UnfocusedEffect(effect: Continuation[Unit] => Any => Unit) extends TargetedEffect[Any] {
  override @SuspendingMethod def runFor(minecraftObject: Any) = effect(continuation)(minecraftObject)
}

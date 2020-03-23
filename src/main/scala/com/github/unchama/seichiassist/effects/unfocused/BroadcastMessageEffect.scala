package com.github.unchama.seichiassist.effects.unfocused

import com.github.unchama.targetedeffect.TargetedEffect

object BroadcastMessageEffect {
  import com.github.unchama.targetedeffect.syntax._

  def apply(message: String): TargetedEffect[Any] =
    BroadcastEffect(message.asMessageEffect())
}

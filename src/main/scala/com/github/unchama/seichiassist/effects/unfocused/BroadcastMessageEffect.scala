package com.github.unchama.seichiassist.effects.unfocused

import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect

object BroadcastMessageEffect {
def apply(message: String): TargetedEffect[Any] =
    BroadcastEffect(MessageEffect(message))
}

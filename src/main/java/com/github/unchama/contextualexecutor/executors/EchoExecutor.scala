package com.github.unchama.contextualexecutor.executors

import com.github.unchama.contextualexecutor.{ContextualExecutor, RawCommandContext}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import org.bukkit.command.CommandSender

/**
 * 実行されたときに[effect]を送り返すだけの[ContextualExecutor].
 */
class EchoExecutor(private val effect: TargetedEffect[CommandSender]) extends ContextualExecutor {
  override @SuspendingMethod def executeWith(rawContext: RawCommandContext): Unit = effect.runFor(rawContext.sender, cont)
}
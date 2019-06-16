package com.github.unchama.contextualexecutor.executors

import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.RawCommandContext
import com.github.unchama.effect.TargetedEffect
import org.bukkit.command.CommandSender

/**
 * 実行されたときに[effect]を送り返すだけの[ContextualExecutor].
 */
class EchoExecutor(private val effect: TargetedEffect<CommandSender>): ContextualExecutor {
  override suspend fun executeWith(rawContext: RawCommandContext) = effect.runFor(rawContext.sender)
}
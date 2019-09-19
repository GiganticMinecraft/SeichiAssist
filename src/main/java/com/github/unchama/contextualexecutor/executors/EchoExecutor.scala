package com.github.unchama.contextualexecutor.executors

import org.bukkit.command.CommandSender

/**
 * 実行されたときに[effect]を送り返すだけの[ContextualExecutor].
 */
class EchoExecutor(private val effect: TargetedEffect<CommandSender>): ContextualExecutor {
  override suspend def executeWith(rawContext: RawCommandContext) = effect.runFor(rawContext.sender)
}
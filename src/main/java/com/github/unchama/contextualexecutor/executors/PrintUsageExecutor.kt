package com.github.unchama.contextualexecutor.executors

import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.RawCommandContext
import com.github.unchama.messaging.asResponseToSender
import org.bukkit.command.Command

/**
 * コマンドの[Command.getUsage]を送信者に送り返すだけのアクションを返すExecutor
 */
object PrintUsageExecutor : ContextualExecutor {
  override suspend fun executeWith(rawContext: RawCommandContext) = rawContext.command.command.usage.asResponseToSender()
}

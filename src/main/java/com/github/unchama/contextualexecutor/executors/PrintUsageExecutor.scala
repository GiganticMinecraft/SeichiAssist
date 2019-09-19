package com.github.unchama.contextualexecutor.executors

import com.github.unchama.contextualexecutor.{ContextualExecutor, RawCommandContext}

/**
 * コマンドの[Command.getUsage]を送信者に送り返すだけのアクションを返すExecutor
 */
object PrintUsageExecutor extends ContextualExecutor {
  override def executeWith(rawContext: RawCommandContext) =
      rawContext.command.command.usage.asMessageEffect().runFor(rawContext.sender)
}

package com.github.unchama.contextualexecutor.executors

import com.github.unchama.contextualexecutor.{ContextualExecutor, RawCommandContext}
import com.github.unchama.targetedeffect.MessageEffects._
import kotlin.coroutines.Continuation

/**
 * コマンドの[Command.usage]を送信者に送り返すだけのアクションを返すExecutor
 */
object PrintUsageExecutor extends ContextualExecutor {
  override def executeWith(rawContext: RawCommandContext, cont: Continuation[Unit]) =
      rawContext.command.command.getUsage.asMessageEffect().runFor(rawContext.sender, cont)
}

package com.github.unchama.contextualexecutor.executors

import com.github.unchama.contextualexecutor.{ContextualExecutor, RawCommandContext}
import com.github.unchama.targetedeffect.MessageEffects._
import com.github.unchama.util.kotlin2scala.SuspendingMethod

/**
 * コマンドの[Command.usage]を送信者に送り返すだけのアクションを返すExecutor
 */
object PrintUsageExecutor extends ContextualExecutor {
  override @SuspendingMethod def executeWith(rawContext: RawCommandContext) =
      rawContext.command.command.getUsage.asMessageEffect().runFor(rawContext.sender, cont)
}

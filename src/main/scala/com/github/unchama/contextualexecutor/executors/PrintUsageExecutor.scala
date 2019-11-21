package com.github.unchama.contextualexecutor.executors

import cats.effect.IO
import com.github.unchama.contextualexecutor.{ContextualExecutor, RawCommandContext}
import com.github.unchama.targetedeffect.syntax._

/**
 * コマンドの[Command.usage]を送信者に送り返すだけのアクションを返すExecutor
 */
object PrintUsageExecutor extends ContextualExecutor {
  override def executeWith(rawContext: RawCommandContext): IO[Unit] =
    rawContext.command.command.getUsage.asMessageEffect()(rawContext.sender)
}

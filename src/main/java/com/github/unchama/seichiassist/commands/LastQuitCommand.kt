package com.github.unchama.seichiassist.commands

import arrow.core.some
import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.seichiassist.SeichiAssist

object LastQuitCommand {
  val executor = ContextualExecutorBuilder.beginConfiguration()
      .argumentsParsers(listOf(Parsers.identity))
      .execution { context ->
        val playerName = context.args.parsed[0] as String

        SeichiAssist.databaseGateway.playerDataManipulator.inquireLastQuitOf(playerName).some()
      }
      .build()
      .asNonBlockingTabExecutor()
}
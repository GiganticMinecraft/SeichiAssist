package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}

object LastQuitCommand {
  val executor = ContextualExecutorBuilder.beginConfiguration()
      .argumentsParsers(listOf(Parsers.identity))
      .execution { context =>
        val playerName = context.args.parsed[0] as String

        SeichiAssist.databaseGateway.playerDataManipulator.inquireLastQuitOf(playerName)
      }
      .build()
      .asNonBlockingTabExecutor()
}
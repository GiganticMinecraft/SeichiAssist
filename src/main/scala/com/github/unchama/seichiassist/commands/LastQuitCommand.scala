package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.seichiassist.SeichiAssist

object LastQuitCommand {
  val executor = ContextualExecutorBuilder.beginConfiguration()
      .argumentsParsers(List(Parsers.identity))
      .execution { context =>
        val playerName = context.args.parsed(0).asInstanceOf[String]

        SeichiAssist.databaseGateway.playerDataManipulator.inquireLastQuitOf(playerName)
      }
      .build()
      .asNonBlockingTabExecutor()
}
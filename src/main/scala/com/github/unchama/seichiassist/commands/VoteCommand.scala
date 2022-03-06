package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import org.bukkit.ChatColor.{RED, YELLOW}
import org.bukkit.command.TabExecutor

object VoteCommand {
  private val usageEchoExecutor: EchoExecutor = EchoExecutor(
    MessageEffect(List(s"$RED/vote record <プレイヤー名>", "投票特典配布用コマンドです"))
  )

  private val recordExecutor =
    ContextualExecutorBuilder
      .beginConfiguration()
      .executionCSEffect(context => {
        val playerName: String = context.args.yetToBeParsed.head
        val lowerCasePlayerName = playerName.toLowerCase

        SequentialEffect(
          MessageEffect(s"$YELLOW${lowerCasePlayerName}の投票特典配布処理開始…"),
          UnfocusedEffect {
            SeichiAssist
              .databaseGateway
              .playerDataManipulator
              .incrementVotePoint(lowerCasePlayerName)
          },
          UnfocusedEffect {
            SeichiAssist.databaseGateway.playerDataManipulator.addChainVote(lowerCasePlayerName)
          }
        )
      })
      .build()

  val executor: TabExecutor = {
    BranchedExecutor(
      Map("record" -> recordExecutor),
      whenBranchNotFound = Some(usageEchoExecutor),
      whenArgInsufficient = Some(usageEchoExecutor)
    ).asNonBlockingTabExecutor()
  }
}

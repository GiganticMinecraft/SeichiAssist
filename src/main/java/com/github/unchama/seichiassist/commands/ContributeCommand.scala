package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.contextualexecutor.builder.Parsers.{identity, nonNegativeInteger}
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import org.bukkit.ChatColor._
import org.bukkit.command.{CommandExecutor, CommandSender}

object ContributeCommand {
  private @SuspendingMethod def addContributionPoint(targetPlayerName: String, point: Int): TargetedEffect[CommandSender] =
      SeichiAssist.databaseGateway.playerDataManipulator
          .addContributionPoint(targetPlayerName, point)
          .map {
            val operationResponse =
                if (point >= 0) {
                  s"${GREEN}${targetPlayerName}に貢献度ポイントを${point}追加しました"
                } else {
                  s"${GREEN}${targetPlayerName}の貢献度ポイントを${point}減少させました"
                }

            operationResponse.asMessageEffect()
          }.merge()

  private val helpMessage: TargetedEffect[CommandSender] = List(
    s"${YELLOW}${BOLD}[コマンドリファレンス]",
    s"${RED}/contribute add [プレイヤー名] [増加分ポイント]",
      "指定されたプレイヤーの貢献度ptを指定分増加させます",
    s"${RED}/contribute remove [プレイヤー名] [減少分ポイント]",
      "指定されたプレイヤーの貢献度ptを指定分減少させます(入力ミス回避用)"
  ).asMessageEffect()

  private val printHelpExecutor: ContextualExecutor = ContextualExecutorBuilder.beginConfiguration()
      .execution { helpMessage }
      .build()

  private val parserConfiguredBuilder = ContextualExecutorBuilder.beginConfiguration()
      .argumentsParsers(List(
          identity,
        nonNegativeInteger(s"${RED}増加分ポイントは0以上の整数を指定してください。".asMessageEffect())
      ), onMissingArguments = printHelpExecutor)

  private val addPointExecutor: ContextualExecutor = parserConfiguredBuilder
      .execution { context =>
        val targetPlayerName = context.args.parsed[0].asInstanceOf[String]
        val point = context.args.parsed[1].asInstanceOf[Int]

        addContributionPoint(targetPlayerName, point)
      }
      .build()

  private val removePointExecutor: ContextualExecutor = parserConfiguredBuilder
      .execution { context =>
        val targetPlayerName = context.args.parsed[0].asInstanceOf[String]
        val point = context.args.parsed[1].asInstanceOf[Int]

        addContributionPoint(targetPlayerName, -point)
      }
      .build()

  val executor: CommandExecutor =
      BranchedExecutor(
          mapOf("add" to addPointExecutor, "remove" to removePointExecutor),
          whenArgInsufficient = printHelpExecutor, whenBranchNotFound = printHelpExecutor
      ).asNonBlockingTabExecutor()
}

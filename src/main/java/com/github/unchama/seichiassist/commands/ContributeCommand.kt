package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.contextualexecutor.builder.Parsers.identity
import com.github.unchama.contextualexecutor.builder.Parsers.nonNegativeInteger
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.asMessageEffect
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.util.data.merge
import org.bukkit.ChatColor
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

object ContributeCommand {
  private suspend fun addContributionPoint(targetPlayerName: String, point: Int): TargetedEffect<CommandSender> =
      SeichiAssist.databaseGateway.playerDataManipulator
          .addContributionPoint(targetPlayerName, point)
          .map {
            val operationResponse =
                if (point >= 0) {
                  "${ChatColor.GREEN}${targetPlayerName}に貢献度ポイントを${point}追加しました"
                } else {
                  "${ChatColor.GREEN}${targetPlayerName}の貢献度ポイントを${point}減少させました"
                }

            operationResponse.asMessageEffect()
          }.merge()

  private val helpMessage: TargetedEffect<CommandSender> = listOf(
      "${ChatColor.YELLOW}${ChatColor.BOLD}[コマンドリファレンス]",
      "${ChatColor.RED}/contribute add <プレイヤー名> <増加分ポイント>",
      "指定されたプレイヤーの貢献度ptを指定分増加させます",
      "${ChatColor.RED}/contribute remove <プレイヤー名> <減少分ポイント>",
      "指定されたプレイヤーの貢献度ptを指定分減少させます(入力ミス回避用)"
  ).asMessageEffect()

  private val printHelpExecutor: ContextualExecutor = ContextualExecutorBuilder.beginConfiguration()
      .execution { helpMessage }
      .build()

  private val parserConfiguredBuilder = ContextualExecutorBuilder.beginConfiguration()
      .argumentsParsers(listOf(
          identity,
          nonNegativeInteger("${ChatColor.RED}増加分ポイントは0以上の整数を指定してください。".asMessageEffect())
      ), onMissingArguments = printHelpExecutor)

  private val addPointExecutor: ContextualExecutor = parserConfiguredBuilder
      .execution { context ->
        val targetPlayerName = context.args.parsed[0] as String
        val point = context.args.parsed[1] as Int

        addContributionPoint(targetPlayerName, point)
      }
      .build()

  private val removePointExecutor: ContextualExecutor = parserConfiguredBuilder
      .execution { context ->
        val targetPlayerName = context.args.parsed[0] as String
        val point = context.args.parsed[1] as Int

        addContributionPoint(targetPlayerName, -point)
      }
      .build()

  val executor: CommandExecutor =
      BranchedExecutor(
          mapOf("add" to addPointExecutor, "remove" to removePointExecutor),
          whenArgInsufficient = printHelpExecutor, whenBranchNotFound = printHelpExecutor
      ).asNonBlockingTabExecutor()
}

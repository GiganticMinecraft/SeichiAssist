package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder

object SubHomeCommand {
  private val printDescriptionExecutor = EchoExecutor(
      List(
          s"${ChatColor.GREEN}/subhome コマンドの使い方",
          s"${ChatColor.GREEN}移動する場合",
          s"${ChatColor.GREEN}/subhome warp [移動したいサブホームの番号]",
          s"${ChatColor.GREEN}セットする場合",
          s"${ChatColor.GREEN}/subhome set [セットしたいサブホームの番号]",
          s"${ChatColor.GREEN}名前変更する場合",
          s"${ChatColor.GREEN}/subhome name [名前変更したいサブホームの番号]"
      ).asMessageEffect()
  )

  private val argsAndSenderConfiguredBuilder = playerCommandBuilder
      .argumentsParsers(
          List(
              SeichiAssist.seichiAssistConfig.subHomeMax.let { subHomeMax =>
                Parsers.closedRangeInt(
                    0, subHomeMax,
                    failureMessage = s"サブホームの番号を1～${subHomeMax}の間で入力してください".asMessageEffect())
              }
          ),
          onMissingArguments = printDescriptionExecutor
      )

  private val warpExecutor = argsAndSenderConfiguredBuilder
      .execution { context =>
        val subHomeId = context.args.parsed[0] as Int
        val player = context.sender
        val subHomeLocation = SeichiAssist.playermap[player.uniqueId]?.getSubHomeLocation(subHomeId - 1)

        if (subHomeLocation != null) {
          player.teleport(subHomeLocation)
          s"サブホームポイント${subHomeId}にワープしました".asMessageEffect()
        } else {
          s"サブホームポイント${subHomeId}が設定されてません".asMessageEffect()
        }
      }
      .build()

  private val setExecutor = argsAndSenderConfiguredBuilder
      .execution { context =>
        val subHomeId = context.args.parsed[0] as Int
        val player = context.sender
        val playerData = SeichiAssist.playermap[player.uniqueId]

        playerData.setSubHomeLocation(player.location, subHomeId - 1)

        s"現在位置をサブホームポイント${subHomeId}に設定しました".asMessageEffect()
      }
      .build()

  private val nameExecutor = argsAndSenderConfiguredBuilder
      .execution { context =>
        val subHomeId = context.args.parsed[0] as Int
        val player = context.sender
        val playerData = SeichiAssist.playermap[player.uniqueId]

        // TODO チャット傍受を手続き的に記述できるようにする
        playerData.setHomeNameNum = subHomeId

        List(
            s"サブホームポイント${subHomeId}に設定する名前をチャットで入力してください",
            s"${ChatColor.YELLOW}※入力されたチャット内容は他のプレイヤーには見えません"
        ).asMessageEffect()
      }
      .build()

  val executor = BranchedExecutor(
      mapOf(
          "warp" to warpExecutor,
          "set" to setExecutor,
          "name" to nameExecutor
      ),
      whenArgInsufficient = printDescriptionExecutor, whenBranchNotFound = printDescriptionExecutor
  ).asNonBlockingTabExecutor()
}
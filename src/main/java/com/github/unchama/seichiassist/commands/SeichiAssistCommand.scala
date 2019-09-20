package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import org.bukkit.command.ConsoleCommandSender

object SeichiAssistCommand {
  private val descriptionExecutor = EchoExecutor(listOf(
      s"${ChatColor.YELLOW}${ChatColor.BOLD}[コマンドリファレンス]",
      s"${ChatColor.RED}/seichiassist reload-config",
      "config.ymlの設定値を再読み込みします",
      s"${ChatColor.RED}/seichiassist toggle-debug",
      "デバッグモードのON,OFFを切り替えます",
      "config.ymlのdebugmodeの値が1の場合のみ、コンソールから使用可能",
      s"${ChatColor.RED}/seichiassist set-anniversary-flag",
      "1周年記念フラグを立てる（コンソール限定コマンド）"
  ).asMessageEffect())

  private val reloadConfigExecutor = ContextualExecutorBuilder.beginConfiguration()
      .execution {
        SeichiAssist.seichiAssistConfig.reloadConfig()
        "config.ymlの設定値を再読み込みしました".asMessageEffect()
      }
      .build()

  private val toggleDebugExecutor = ContextualExecutorBuilder.beginConfiguration()
      .execution {
        //debugフラグ反転処理
        if (SeichiAssist.seichiAssistConfig.debugMode == 1) {
          //メッセージフラグを反転
          SeichiAssist.DEBUG = !SeichiAssist.DEBUG
          SeichiAssist.instance.restartRepeatedJobs()

          val resultMessage = if (SeichiAssist.DEBUG) {
            s"${ChatColor.GREEN}デバッグモードを有効にしました"
          } else {
            s"${ChatColor.GREEN}デバッグモードを無効にしました"
          }

          resultMessage.asMessageEffect()
        } else {
          listOf(
            s"${ChatColor.RED}このコマンドは現在の設定では実行できません",
            s"${ChatColor.RED}config.ymlのdebugmodeの値を1に書き換えて再起動またはreloadしてください"
          ).asMessageEffect()
        }
      }
      .build()

  private val setAnniversaryFlagExecutor = ContextualExecutorBuilder.beginConfiguration()
      .refineSenderWithError[ConsoleCommandSender]("コンソール専用コマンドです")
      .execution {
        SeichiAssist.databaseGateway.playerDataManipulator.setAnniversary(true, null)

        "Anniversaryアイテムの配布を開始しました。".asMessageEffect()
      }
      .build()

  val executor = BranchedExecutor(
      mapOf(
          "reload-config" to reloadConfigExecutor,
          "toggle-debug" to toggleDebugExecutor,
          "set-anniversary-flag" to setAnniversaryFlagExecutor
      ),
      whenArgInsufficient = descriptionExecutor, whenBranchNotFound = descriptionExecutor
  ).asNonBlockingTabExecutor()
}

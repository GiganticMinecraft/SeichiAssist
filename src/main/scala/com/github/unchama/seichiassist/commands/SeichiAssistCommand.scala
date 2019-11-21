package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.targetedeffect.syntax._
import org.bukkit.ChatColor._
import org.bukkit.command.{ConsoleCommandSender, TabExecutor}

object SeichiAssistCommand {
  private val descriptionExecutor = new EchoExecutor(List(
    s"$YELLOW$BOLD[コマンドリファレンス]",
    s"$RED/seichiassist reload-config",
    "config.ymlの設定値を再読み込みします",
    s"$RED/seichiassist toggle-debug",
    "デバッグモードのON,OFFを切り替えます",
    "config.ymlのdebugmodeの値が1の場合のみ、コンソールから使用可能",
    s"$RED/seichiassist set-anniversary-flag",
    "1周年記念フラグを立てる（コンソール限定コマンド）"
  ).asMessageEffect())

  private val reloadConfigExecutor = ContextualExecutorBuilder.beginConfiguration()
    .execution { _ =>
      IO {
        SeichiAssist.seichiAssistConfig.reloadConfig()
        "config.ymlの設定値を再読み込みしました".asMessageEffect()
      }
    }
    .build()

  private val toggleDebugExecutor = ContextualExecutorBuilder.beginConfiguration()
    .execution { _ =>
      IO {
        //debugフラグ反転処理
        if (SeichiAssist.seichiAssistConfig.getDebugMode == 1) {
          //メッセージフラグを反転
          SeichiAssist.DEBUG = !SeichiAssist.DEBUG
          SeichiAssist.instance.restartRepeatedJobs()

          val resultMessage = if (SeichiAssist.DEBUG) {
            s"${GREEN}デバッグモードを有効にしました"
          } else {
            s"${GREEN}デバッグモードを無効にしました"
          }

          resultMessage.asMessageEffect()
        } else {
          List(
            s"${RED}このコマンドは現在の設定では実行できません",
            s"${RED}config.ymlのdebugmodeの値を1に書き換えて再起動またはreloadしてください"
          ).asMessageEffect()
        }
      }
    }
    .build()

  private val setAnniversaryFlagExecutor = ContextualExecutorBuilder.beginConfiguration()
    .refineSenderWithError[ConsoleCommandSender]("コンソール専用コマンドです")
    .execution { _ =>
      IO {
        SeichiAssist.databaseGateway.playerDataManipulator.setAnniversary(anniversary = true, null)

        "Anniversaryアイテムの配布を開始しました。".asMessageEffect()
      }
    }
    .build()

  val executor: TabExecutor = BranchedExecutor(
    Map(
      "reload-config" -> reloadConfigExecutor,
      "toggle-debug" -> toggleDebugExecutor,
      "set-anniversary-flag" -> setAnniversaryFlagExecutor
    ),
    whenArgInsufficient = Some(descriptionExecutor), whenBranchNotFound = Some(descriptionExecutor)
  ).asNonBlockingTabExecutor()
}

package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.MessageEffects._
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor

object SubHomeCommand {
  private val printDescriptionExecutor = new EchoExecutor(
    List(
      s"${GREEN}/subhome コマンドの使い方",
      s"${GREEN}移動する場合",
      s"${GREEN}/subhome warp [移動したいサブホームの番号]",
      s"${GREEN}セットする場合",
      s"${GREEN}/subhome set [セットしたいサブホームの番号]",
      s"${GREEN}名前変更する場合",
      s"${GREEN}/subhome name [名前変更したいサブホームの番号]"
    ).asMessageEffect()
  )

  private val subHomeMax = SeichiAssist.seichiAssistConfig.getSubHomeMax

  private val argsAndSenderConfiguredBuilder = playerCommandBuilder
    .argumentsParsers(
      List(
        Parsers.closedRangeInt(
            0, subHomeMax,
            failureMessage = s"サブホームの番号を1～${subHomeMax}の間で入力してください".asMessageEffect())
      ),
      onMissingArguments = printDescriptionExecutor
    )

  private val warpExecutor = argsAndSenderConfiguredBuilder
    .execution { context =>
      val subHomeId = context.args.parsed(0).asInstanceOf[Int]
      val player = context.sender

      val subHomeLocation = SeichiAssist.playermap.get(player.getUniqueId) match {
        case Some(playerData) => playerData.getSubHomeLocation(subHomeId - 1)
      }
      subHomeLocation match {
        case None => IO(s"サブホームポイント${subHomeId}が設定されてません".asMessageEffect())
        case Some(location) => IO {
          player.teleport(location)
          s"サブホームポイント${subHomeId}にワープしました".asMessageEffect()
        }
      }
    }
    .build()

  private val setExecutor = argsAndSenderConfiguredBuilder
    .execution { context =>
      val subHomeId = context.args.parsed(0).asInstanceOf[Int]
      val player = context.sender
      val playerData = SeichiAssist.playermap(player.getUniqueId)

      playerData.setSubHomeLocation(player.getLocation, subHomeId - 1)

      IO(s"現在位置をサブホームポイント${subHomeId}に設定しました".asMessageEffect())
    }
    .build()

  private val nameExecutor = argsAndSenderConfiguredBuilder
    .execution { context =>
      val subHomeId = context.args.parsed(0).asInstanceOf[Int]
      val player = context.sender
      val playerData = SeichiAssist.playermap(player.getUniqueId)

      // TODO チャット傍受を手続き的に記述できるようにする
      playerData.setHomeNameNum = subHomeId

      IO {
        List(
          s"サブホームポイント${subHomeId}に設定する名前をチャットで入力してください",
          s"${YELLOW}※入力されたチャット内容は他のプレイヤーには見えません"
        ).asMessageEffect()
      }
    }
    .build()

  val executor: TabExecutor = BranchedExecutor(
    Map(
      "warp" -> warpExecutor,
      "set" -> setExecutor,
      "name" -> nameExecutor
    ),
    whenArgInsufficient = Some(printDescriptionExecutor), whenBranchNotFound = Some(printDescriptionExecutor)
  ).asNonBlockingTabExecutor()
}
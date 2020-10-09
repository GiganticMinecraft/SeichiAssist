package com.github.unchama.seichiassist.commands

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.chatinterceptor.CancellationReason.Overridden
import com.github.unchama.chatinterceptor.ChatInterceptionScope
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor

object SubHomeCommand {
  private val printDescriptionExecutor = new EchoExecutor(
    MessageEffect(
      List(
        s"$GREEN/subhome コマンドの使い方",
        s"${GREEN}移動する場合",
        s"$GREEN/subhome warp [移動したいサブホームの番号]",
        s"${GREEN}セットする場合",
        s"$GREEN/subhome set [セットしたいサブホームの番号]",
        s"${GREEN}名前変更する場合",
        s"$GREEN/subhome name [名前変更したいサブホームの番号]"
      )
    )
  )
  private val subHomeMax = SeichiAssist.seichiAssistConfig.getSubHomeMax
  private val argsAndSenderConfiguredBuilder = playerCommandBuilder
    .argumentsParsers(
      List(
        Parsers.closedRangeInt(
          1, subHomeMax,
          failureMessage = MessageEffect(s"サブホームの番号を1～${subHomeMax}の間で入力してください"))
      ),
      onMissingArguments = printDescriptionExecutor
    )
  private val warpExecutor = argsAndSenderConfiguredBuilder
    .execution { context =>
      val subHomeId = context.args.parsed.head.asInstanceOf[Int]
      val player = context.sender

      val subHomeLocation = SeichiAssist.playermap(player.getUniqueId).getSubHomeLocation(subHomeId - 1)
      subHomeLocation match {
        case None => IO(MessageEffect(s"サブホームポイント${subHomeId}が設定されてません"))
        case Some(location) => IO {
          player.teleport(location)
          MessageEffect(s"サブホームポイント${subHomeId}にワープしました")
        }
      }
    }
    .build()
  private val setExecutor = argsAndSenderConfiguredBuilder
    .execution { context =>
      val subHomeId = context.args.parsed.head.asInstanceOf[Int]
      val player = context.sender
      val playerData = SeichiAssist.playermap(player.getUniqueId)

      playerData.setSubHomeLocation(player.getLocation, subHomeId - 1)

      IO(MessageEffect(s"現在位置をサブホームポイント${subHomeId}に設定しました"))
    }
    .build()

  private def nameExecutor(implicit scope: ChatInterceptionScope) = argsAndSenderConfiguredBuilder
    .execution { context =>
      val subHomeId = context.args.parsed.head.asInstanceOf[Int]

      IO.pure {
        val sendInterceptionMessage =
          MessageEffect(
            List(
              s"サブホームポイント${subHomeId}に設定する名前をチャットで入力してください",
              s"$YELLOW※入力されたチャット内容は他のプレイヤーには見えません"
            )
          )

        val sendCancellationMessage =
          MessageEffect(s"${YELLOW}入力がキャンセルされました。")

        def sendCompletionMessage(inputName: String) =
          MessageEffect(
            List(
              s"${GREEN}サブホームポイント${subHomeId}の名前を",
              s"$GREEN${inputName}に更新しました"
            )
          )

        import cats.implicits._
        import com.github.unchama.generic.syntax._

        sendInterceptionMessage.followedBy(Kleisli { player =>
          val playerData = SeichiAssist.playermap(player.getUniqueId)

          scope.interceptFrom(player.getUniqueId).flatMap {
            case Left(newName) =>
              IO { playerData.setSubHomeName(newName, subHomeId - 1) } *>
                sendCompletionMessage(newName)(player)
            case Right(Overridden) => sendCancellationMessage(player)
            case Right(_) => IO.pure(())
          }
        })
      }
    }
    .build()

  def executor(implicit scope: ChatInterceptionScope): TabExecutor = BranchedExecutor(
    Map(
      "warp" -> warpExecutor,
      "set" -> setExecutor,
      "name" -> nameExecutor
    ),
    whenArgInsufficient = Some(printDescriptionExecutor), whenBranchNotFound = Some(printDescriptionExecutor)
  ).asNonBlockingTabExecutor()
}
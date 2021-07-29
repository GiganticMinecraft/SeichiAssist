package com.github.unchama.seichiassist.subsystems.subhome.bukkit.command

import cats.data.Kleisli
import cats.effect.implicits._
import cats.effect.{ConcurrentEffect, IO}
import cats.implicits._
import com.github.unchama.chatinterceptor.CancellationReason.Overridden
import com.github.unchama.chatinterceptor.ChatInterceptionScope
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.subhome.domain.{SubHome, SubHomeId}
import com.github.unchama.seichiassist.subsystems.subhome.{SubHomeReadAPI, SubHomeWriteAPI}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.{Bukkit, Location}

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

  private def warpExecutor[
    F[_]
    : ConcurrentEffect
    : NonServerThreadContextShift
    : SubHomeReadAPI
  ] = argsAndSenderConfiguredBuilder
    .execution { context =>
      val subHomeId = SubHomeId(context.args.parsed.head.asInstanceOf[Int])
      val player = context.sender

      val eff = for {
        _ <- NonServerThreadContextShift[F].shift
        subHomeLocation <- SubHomeReadAPI[F].get(player.getUniqueId, subHomeId)
      } yield {
        subHomeLocation match {
          case None => MessageEffect(s"サブホームポイント${subHomeId}が設定されてません")
          case Some(SubHome(_, location)) =>
            // TODO これは副作用
            player.teleport(new Location(Bukkit.getWorld(location.worldName), location.x, location.y, location.z))
            MessageEffect(s"サブホームポイント${subHomeId}にワープしました")
        }
      }

      eff.toIO
    }
    .build()

  private def setExecutor[
    F[_]
    : ConcurrentEffect
    : NonServerThreadContextShift
    : SubHomeWriteAPI
  ] = argsAndSenderConfiguredBuilder
    .execution { context =>
      val subHomeId = SubHomeId(context.args.parsed.head.asInstanceOf[Int])
      val player = context.sender

      val eff = for {
        _ <- NonServerThreadContextShift[F].shift
        _ <- SubHomeWriteAPI[F].updateLocation(player.getUniqueId, subHomeId, player.getLocation)
      } yield {
        MessageEffect(s"現在位置をサブホームポイント${subHomeId}に設定しました")
      }

      eff.toIO
    }
    .build()

  private def nameExecutor[
    F[_]
    : ConcurrentEffect
    : NonServerThreadContextShift
    : SubHomeWriteAPI
  ](implicit scope: ChatInterceptionScope) = argsAndSenderConfiguredBuilder
    .execution { context =>
      val subHomeId = SubHomeId(context.args.parsed.head.asInstanceOf[Int])

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

        import com.github.unchama.generic.syntax._

        sendInterceptionMessage.followedBy(Kleisli { player =>
          val uuid = player.getUniqueId

          scope.interceptFrom(uuid).flatMap {
            case Left(newName) =>
              val eff = for {
                _ <- SubHomeWriteAPI[F].updateName(uuid, subHomeId, newName)
              } yield {}
              eff.toIO *>
                sendCompletionMessage(newName)(player)
            case Right(Overridden) => sendCancellationMessage(player)
            case Right(_) => IO.pure(())
          }
        })
      }
    }
    .build()

  def executor[
    F[_]
    : SubHomeReadAPI
    : SubHomeWriteAPI
    : ConcurrentEffect
    : NonServerThreadContextShift
  ](implicit scope: ChatInterceptionScope): TabExecutor = BranchedExecutor(
    Map(
      "warp" -> warpExecutor,
      "set" -> setExecutor,
      "name" -> nameExecutor
    ),
    whenArgInsufficient = Some(printDescriptionExecutor), whenBranchNotFound = Some(printDescriptionExecutor)
  ).asNonBlockingTabExecutor()
}
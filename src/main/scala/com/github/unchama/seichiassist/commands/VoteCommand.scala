package com.github.unchama.seichiassist.commands

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.{TargetedEffect, UnfocusedEffect}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor.{GREEN, RED, YELLOW}
import org.bukkit.command.{CommandSender, TabExecutor}

object VoteCommand {
  sealed trait Operation
  case object Record extends Operation

  val usageEchoEcexutor: TargetedEffect[CommandSender] = MessageEffect(List(
    s"$RED/vote record <プレイヤー名>",
    "投票特典配布用コマンドです"
  ))
  val executor: TabExecutor = playerCommandBuilder
    .argumentsParsers(
      List(
        Parsers.fromOptionParser({
          case "record" => Some(Record)
          case _ => None
        }, usageEchoEcexutor),
        Parsers.identity
      )
    )
    .execution(context => {
      val args = context.args.parsed
      val command: Operation = args.head.asInstanceOf
      val name: String = args(1).asInstanceOf
      command match {
        case Record => {
          //引数が2つの時の処理
          val lowerCasePlayerName = Util.getName(name)
          //プレイヤーオンライン、オフラインにかかわらずsqlに送信(マルチ鯖におけるコンフリクト防止の為)
          IO {
            for {
              _ <- MessageEffect(s"$YELLOW${lowerCasePlayerName}の投票特典配布処理開始…")
              _ <- UnfocusedEffect {
                SeichiAssist.databaseGateway.playerDataManipulator.incrementVotePoint(lowerCasePlayerName)
              }
              k = if (SeichiAssist.databaseGateway.playerDataManipulator.addChainVote(lowerCasePlayerName)) {
                MessageEffect(s"${GREEN}連続投票数の記録に成功")
              } else {
                MessageEffect(s"${RED}連続投票数の記録に失敗")
              }
            } yield k
          }
        }
      }
    })
    .build()
    .asNonBlockingTabExecutor()
}

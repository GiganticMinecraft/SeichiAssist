package com.github.unchama.seichiassist.subsystems.lastquit.bukkit.commands

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.seichiassist.subsystems.lastquit.LastQuitAPI
import com.github.unchama.seichiassist.subsystems.lastquit.domain.PlayerName
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.command.TabExecutor
import org.bukkit.ChatColor.RED

import java.time.format.DateTimeFormatter

class LastQuitCommand[F[_]: ConcurrentEffect](implicit lastQuitAPI: LastQuitAPI[F]) {

  val executor: TabExecutor = ContextualExecutorBuilder
    .beginConfiguration()
    .argumentsParsers(List(Parsers.identity))
    .execution { context =>
      val playerName = context.args.parsed.head.asInstanceOf[String]
      val lastQuitOpt =
        lastQuitAPI.lastQuitDateTime(PlayerName(playerName)).toIO.unsafeRunSync()
      val eff = IO {
        lastQuitOpt match {
          case Some(lastQuit) =>
            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
            MessageEffect(
              s"${playerName}の最終ログアウト日時：${lastQuit.dateTime.format(dateTimeFormatter)}"
            )
          case None =>
            MessageEffect(
              List(
                s"${RED}最終ログアウト日時の照会に失敗しました。",
                s"${RED}プレイヤー名が変更されていないか確認してください。",
                s"${RED}プレイヤー名が正しいのにこのエラーが出る場合、最終ログイン時間が古い可能性があります。"
              )
            )
        }
      }
      eff
    }
    .build()
    .asNonBlockingTabExecutor()

}

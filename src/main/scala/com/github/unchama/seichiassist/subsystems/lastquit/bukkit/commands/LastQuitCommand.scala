package com.github.unchama.seichiassist.subsystems.lastquit.bukkit.commands

import cats.data.Kleisli
import cats.effect.ConcurrentEffect
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.seichiassist.infrastructure.minecraft.{
  JdbcLastSeenNameToUuid,
  LastSeenNameToUuidError
}
import com.github.unchama.seichiassist.subsystems.lastquit.LastQuitAPI
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import org.bukkit.ChatColor.RED
import org.bukkit.command.TabExecutor

import java.time.format.DateTimeFormatter

class LastQuitCommand[F[_]: ConcurrentEffect](implicit lastQuitAPI: LastQuitAPI[F]) {

  import cats.implicits._

  val executor: TabExecutor = ContextualExecutorBuilder
    .beginConfiguration
    .thenParse(Parsers.identity)
    .buildWithExecutionCSEffect { context =>
      val playerName = context.args.parsed.head

      (for {
        uuidEither <- Kleisli.liftF(new JdbcLastSeenNameToUuid[F].of(playerName))
        lastQuit <- Kleisli.liftF(uuidEither.traverse(uuid => lastQuitAPI.get(uuid)))
      } yield {
        lastQuit match {
          case Left(error) =>
            error match {
              case LastSeenNameToUuidError.MultipleFound =>
                MessageEffectF(s"${RED}指定された名前のプレイヤーが複数見つかりました。")
              case LastSeenNameToUuidError.NotFound =>
                MessageEffectF(s"${RED}指定された名前のプレイヤーが見つかりませんでした。")
            }
          case Right(lastQuitTimeOpt) =>
            lastQuitTimeOpt match {
              case Some(lastQuitTime) =>
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
                MessageEffectF(
                  s"${playerName}の最終ログアウト日時：${lastQuitTime.dateTime.format(dateTimeFormatter)}"
                )
              case None =>
                MessageEffectF(
                  List(
                    s"${RED}最終ログアウト日時の照会に失敗しました。",
                    s"${RED}プレイヤー名が変更されていないか確認してください。",
                    s"${RED}プレイヤー名が正しいのにこのエラーが出る場合、最終ログイン時間が古い可能性があります。"
                  )
                )
            }
        }
      }).flatten
    }
    .asNonBlockingTabExecutor()

}

package com.github.unchama.seichiassist.subsystems.vote.bukkit.command

import com.github.unchama.toIO

import cats.effect.Async
import cats.effect.syntax.all._
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.seichiassist.infrastructure.minecraft.{
  JdbcLastSeenNameToUuid,
  LastSeenNameToUuidError
}
import com.github.unchama.seichiassist.subsystems.vote.domain.VotePersistence
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{DeferredEffect, SequentialEffect}
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor

class VoteCommand[F[_]: Async: [f[_]] =>> ContextCoercion[f, cats.effect.IO]](
  implicit votePersistence: VotePersistence[F]
) {

  private val usageEchoExecutor: EchoExecutor = EchoExecutor(
    MessageEffect(List(s"$RED/vote record <プレイヤー名>", "投票特典配布用コマンドです"))
  )

  import cats.implicits._

  private val recordExecutor = {
    ContextualExecutorBuilder
      .beginConfiguration
      .thenParse(Parsers.identity)
      .buildWithExecutionCSEffect { context =>
        val playerName = context.args.parsed.head
        val distributionProcess = for {
          uuidEither <- new JdbcLastSeenNameToUuid[F].of(playerName)
          program <- uuidEither.traverse { uuid =>
            votePersistence.incrementVoteCount(uuid) >> votePersistence
              .updateConsecutiveVoteStreak(uuid)
          }
        } yield program match {
          case Left(error) =>
            error match {
              case LastSeenNameToUuidError.MultipleFound =>
                MessageEffect(s"${RED}指定された名前のプレイヤーが複数見つかりました。")
              case LastSeenNameToUuidError.NotFound =>
                MessageEffect(s"${RED}指定された名前のプレイヤーが見つかりませんでした。")
            }
          case Right(_) =>
            MessageEffect(s"$AQUA${playerName}への特典配布処理に成功しました。")
        }

        SequentialEffect(
          MessageEffect(s"$YELLOW${playerName}の特典配布処理開始…"),
          DeferredEffect(distributionProcess.toIO)
        )
      }
  }

  val executor: TabExecutor = {
    BranchedExecutor(
      Map("record" -> recordExecutor),
      whenBranchNotFound = Some(usageEchoExecutor),
      whenArgInsufficient = Some(usageEchoExecutor)
    )
  }.asNonBlockingTabExecutor()

}

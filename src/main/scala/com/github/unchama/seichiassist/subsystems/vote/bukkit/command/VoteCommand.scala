package com.github.unchama.seichiassist.subsystems.vote.bukkit.command

import cats.effect.ConcurrentEffect
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.contextualexecutor.executors.{BranchedExecutor, EchoExecutor}
import com.github.unchama.seichiassist.subsystems.vote.domain.{PlayerName, VotePersistence}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, UnfocusedEffect}
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor

class VoteCommand[F[_]: ConcurrentEffect](implicit votePersistence: VotePersistence[F]) {

  private val usageEchoExecutor: EchoExecutor = EchoExecutor(
    MessageEffect(List(s"$RED/vote record <プレイヤー名>", "投票特典配布用コマンドです"))
  )

  import cats.implicits._

  private val recordExecutor = {
    ContextualExecutorBuilder
      .beginConfiguration()
      .executionCSEffect { context =>
        val lowerCasePlayerName = context.args.yetToBeParsed.head

        SequentialEffect(
          MessageEffect(s"$YELLOW${lowerCasePlayerName}の特典配布処理開始…"),
          UnfocusedEffect {
            val playerName = PlayerName(lowerCasePlayerName)
            val eff = for {
              _ <- votePersistence.voteCounterIncrement(playerName)
              _ <- votePersistence.updateChainVote(playerName)
            } yield ()
            eff.toIO.unsafeRunAsyncAndForget()
          }
        )
      }
      .build()
  }

  val executor: TabExecutor = {
    BranchedExecutor(
      Map("record" -> recordExecutor),
      whenBranchNotFound = Some(usageEchoExecutor),
      whenArgInsufficient = Some(usageEchoExecutor)
    )
  }.asNonBlockingTabExecutor()

}

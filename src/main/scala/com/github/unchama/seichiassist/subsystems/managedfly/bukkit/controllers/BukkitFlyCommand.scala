package com.github.unchama.seichiassist.subsystems.managedfly.bukkit.controllers

import com.github.unchama.runSync

import com.github.unchama.toIO

import cats.data.Kleisli
import cats.effect.{Async, IO, Sync, SyncIO}
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.Parsers
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates
import com.github.unchama.seichiassist.subsystems.managedfly.application.{
  ActiveSessionFactory,
  ActiveSessionReference
}
import com.github.unchama.seichiassist.subsystems.managedfly.domain.{
  Flying,
  NotFlying,
  RemainingFlyDuration
}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.{MessageEffect, MessageEffectF}
import io.github.iltotore.iron.:|
import io.github.iltotore.iron.constraint.numeric.Positive
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object BukkitFlyCommand {

  private val commandHelpMessage = List(
    s"${GREEN}fly機能を時間付きで利用したい場合は末尾に「add 利用したい時間(分単位)」の数値を、",
    s"${GREEN}fly機能を無期限で利用したい場合は末尾に「endless」を、",
    s"${GREEN}fly機能を中断したい場合は、末尾に「finish」または「end」を記入してください。"
  )

  private val durationParseFailedMessage =
    s"${GREEN}時間指定は「1」以上の整数を入力してください。"

  private val printUsageExecutor =
    BuilderTemplates
      .playerCommandBuilder
      .buildWithIOAsExecution(IO.pure(MessageEffect(commandHelpMessage)))

  private val durationParser =
    Parsers
      .closedRangeInt[Int :| Positive](
        1,
        Int.MaxValue,
        MessageEffect(durationParseFailedMessage)
      )
      .andThen { parseResult =>
        parseResult.map(r => RemainingFlyDuration.PositiveMinutes.fromPositive(r))
      }

  import cats.effect.syntax.all._
  import cats.implicits._

  private def startEndlessCommand[F[_]: Async: [f[_]] =>> ContextCoercion[f, IO], G[
    _
  ]: Sync: [g[_]] =>> ContextCoercion[g, F]](
    implicit
    sessionReferenceRepository: KeyedDataRepository[Player, ActiveSessionReference[F, G]],
    factory: ActiveSessionFactory[F, Player]
  ): ContextualExecutor =
    BuilderTemplates.playerCommandBuilder.buildWithExecutionF { context =>
      for {
        _ <-
          sessionReferenceRepository(context.sender)
            .replaceSession(factory.start[G](RemainingFlyDuration.Infinity).run(context.sender))
      } yield TargetedEffect.emptyEffect
    }

  private def addCommand[F[_]: Async: [f[_]] =>> ContextCoercion[f, IO], G[_]: Sync: [g[
    _
  ]] =>> ContextCoercion[g, F]: [g[_]] =>> ContextCoercion[g, SyncIO]](
    implicit
    sessionReferenceRepository: KeyedDataRepository[Player, ActiveSessionReference[F, G]],
    factory: ActiveSessionFactory[F, Player]
  ): ContextualExecutor =
    BuilderTemplates.playerCommandBuilder.thenParse(durationParser).buildWithExecutionF {
      context =>
        val duration = context.args.parsed.head

        for {
          currentStatus <- sessionReferenceRepository(context.sender)
            .getLatestFlyStatus
            .runSync[SyncIO]
            .toIO
          newTotalDuration = currentStatus match {
            case Flying(remainingDuration) => remainingDuration.combine(duration)
            case NotFlying                 => duration
          }
          _ <-
            sessionReferenceRepository(context.sender)
              .replaceSession(factory.start[G](newTotalDuration).run(context.sender))
              .toIO
        } yield TargetedEffect.emptyEffect
    }

  private def finishCommand[F[_]: Async: [f[_]] =>> ContextCoercion[f, IO], G[_]](
    implicit
    sessionReferenceRepository: KeyedDataRepository[Player, ActiveSessionReference[F, G]]
  ): ContextualExecutor =
    BuilderTemplates.playerCommandBuilder.buildWithExecutionCSEffect { context =>
      Kleisli.liftF(sessionReferenceRepository(context.sender).stopAnyRunningSession).flatMap {
        sessionStopped =>
          if (sessionStopped) {
            MessageEffectF(s"${GREEN}fly効果を停止しました。")
          } else {
            MessageEffectF(s"${GREEN}fly効果は現在OFFです。")
          }
      }
    }

  def executor[F[_]: Async: [f[_]] =>> ContextCoercion[f, IO], G[_]: Sync: [g[
    _
  ]] =>> ContextCoercion[g, F]: [g[_]] =>> ContextCoercion[g, SyncIO]](
    implicit
    sessionReferenceRepository: KeyedDataRepository[Player, ActiveSessionReference[F, G]],
    factory: ActiveSessionFactory[F, Player]
  ): TabExecutor =
    BranchedExecutor(
      Map(
        "endless" -> startEndlessCommand[F, G],
        "add" -> addCommand[F, G],
        "finish" -> finishCommand[F, G],
        "end" -> finishCommand[F, G]
      ),
      whenArgInsufficient = Some(printUsageExecutor),
      whenBranchNotFound = Some(printUsageExecutor)
    ).asNonBlockingTabExecutor()
}

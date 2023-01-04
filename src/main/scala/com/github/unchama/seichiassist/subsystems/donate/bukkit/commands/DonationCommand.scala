package com.github.unchama.seichiassist.subsystems.donate.bukkit.commands

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, IO}
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.builder.{ContextualExecutorBuilder, Parsers}
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.subsystems.donate.domain.{
  DonatePersistence,
  DonatePremiumEffectPoint,
  PlayerName
}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor

class DonationCommand[F[_]: ConcurrentEffect](
  using donatePersistence: DonatePersistence[F]
) {

  import cats.implicits._

  private val recordExecutor: ContextualExecutor =
    ContextualExecutorBuilder
      .beginConfiguration()
      .argumentsParsers(
        List(
          Parsers.identity,
          Parsers.integer(MessageEffect(s"${RED}付与するプレミアムエフェクトポイントは整数で指定してください。"))
        )
      )
      .execution { context =>
        val args = context.args.parsed
        val playerName = PlayerName(args.head.toString)
        val donatePoint = DonatePremiumEffectPoint(args(1).asInstanceOf[Int])
        val eff = for {
          _ <- donatePersistence.addDonatePremiumEffectPoint(playerName, donatePoint)
        } yield {
          MessageEffect(s"$GREEN${playerName.name}に${donatePoint.value}のプレミアムエフェクトポイントを付与しました。")
        }
        eff.toIO
      }
      .build()

  private val commandDescriptionExecutor: ContextualExecutor =
    ContextualExecutorBuilder
      .beginConfiguration()
      .execution { _ =>
        IO {
          MessageEffect(
            List(
              s"$RED/donation record <プレイヤー名> <ポイント数>",
              "寄付者用プレミアムエフェクトポイント配布コマンドです(マルチ鯖対応済)"
            )
          )
        }
      }
      .build()

  val executor: TabExecutor =
    BranchedExecutor(
      Map("record" -> recordExecutor),
      whenArgInsufficient = Some(commandDescriptionExecutor),
      whenBranchNotFound = Some(commandDescriptionExecutor)
    ).asNonBlockingTabExecutor()

}

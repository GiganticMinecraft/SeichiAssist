package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.syntax._
import com.github.unchama.targetedeffect.{TargetedEffect, emptyEffect}
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object EffectCommand {
  private val printUsageExecutor = playerCommandBuilder
    .execution { _ =>
      val message = List(
        s"$YELLOW$BOLD[コマンドリファレンス]",
        s"$RED/ef",
        "採掘速度上昇効果の制限を変更することができます。",
        s"$RED/ef smart",
        "採掘速度上昇効果の内訳を表示するかしないかを変更することができます。"
      )

      IO {
        message.asMessageEffect()
      }
    }
    .build()

  private val toggleExecutor = playerCommandBuilder
    .execution { context =>
      val playerData = SeichiAssist.playermap(context.sender.getUniqueId)
      val guidance = "再度 /ef コマンドを実行することでトグルします。".asMessageEffect()

      def execution(): TargetedEffect[Player] = {
        import com.github.unchama.generic.syntax._

        if (playerData == null) return emptyEffect

        val toggleResponse = playerData.settings.fastDiggingEffectSuppression.suppressionDegreeToggleEffect
        toggleResponse.followedBy(guidance)
      }

      IO.pure(execution())
    }
    .build()

  private val messageFlagToggleExecutor = playerCommandBuilder
    .execution { context =>
      val playerData = SeichiAssist.playermap(context.sender.getUniqueId)

      def execution(): TargetedEffect[Player] = {
        if (playerData == null) return emptyEffect
        playerData.toggleMessageFlag()
      }

      IO.pure(execution())
    }
    .build()

  val executor: TabExecutor = BranchedExecutor(
    Map("smart" -> messageFlagToggleExecutor),
    whenArgInsufficient = Some(toggleExecutor), whenBranchNotFound = Some(printUsageExecutor)
  ).asNonBlockingTabExecutor()
}

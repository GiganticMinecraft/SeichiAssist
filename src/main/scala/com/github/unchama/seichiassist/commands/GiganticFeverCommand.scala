package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.util.{SendMessageEffect, WorldSettings}
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import org.bukkit.ChatColor._
import org.bukkit.Difficulty
import org.bukkit.command.TabExecutor

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object GiganticFeverCommand {
  private val worldsToToggleDifficulty = ManagedWorld.seichiWorlds.map(_.alphabetName).toList

  val executor: TabExecutor = ContextualExecutorBuilder
    .beginConfiguration[Nothing]()
    .execution { _ =>
      val config = SeichiAssist.seichiAssistConfig

      SendMessageEffect.sendMessageToEveryoneIgnoringPreference(
        s"${AQUA}フィーバー！この時間MOBたちは踊りに出かけてるぞ！今が整地時だ！"
      )
      SendMessageEffect.sendMessageToEveryoneIgnoringPreference(
        s"$AQUA(${config.getGiganticFeverDisplayTime}間)"
      )

      WorldSettings.setDifficulty(worldsToToggleDifficulty, Difficulty.PEACEFUL)

      IO.sleep(
        FiniteDuration(config.getGiganticFeverMinutes * 60, scala.concurrent.duration.MINUTES)
      )(IO.timer(ExecutionContext.global))

      WorldSettings.setDifficulty(worldsToToggleDifficulty, Difficulty.HARD)
      SendMessageEffect.sendMessageToEveryoneIgnoringPreference(s"${AQUA}フィーバー終了！MOBたちは戻ってきたぞ！")

      IO(emptyEffect)
    }
    .build()
    .asNonBlockingTabExecutor()
}

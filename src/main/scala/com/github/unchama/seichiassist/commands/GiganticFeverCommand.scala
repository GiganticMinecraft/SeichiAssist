package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.util.{SendMessageEffect, WorldSettings}
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import org.bukkit.ChatColor._
import org.bukkit.Difficulty
import org.bukkit.command.TabExecutor

import scala.concurrent.ExecutionContext

object GiganticFeverCommand {
  import cats.implicits._
  import scala.concurrent.duration._

  private val worldsToToggleDifficulty = ManagedWorld.seichiWorlds.map(_.alphabetName).toList

  val executor: TabExecutor = ContextualExecutorBuilder
    .beginConfiguration
    .buildWithIOAsExecution {
      val config = SeichiAssist.seichiAssistConfig
      val broadcast = SendMessageEffect.sendMessageToEveryoneIgnoringPreferenceIO(_: String)

      List(
        broadcast(s"${AQUA}フィーバー！この時間MOBたちは踊りに出かけてるぞ！今が整地時だ！"),
        broadcast(s"$AQUA(${config.getGiganticFeverDisplayTime}間)"),
        WorldSettings.setDifficulty(worldsToToggleDifficulty, Difficulty.PEACEFUL),
        IO.sleep((config.getGiganticFeverMinutes * 60).minutes)(
          IO.timer(ExecutionContext.global)
        ),
        WorldSettings.setDifficulty(worldsToToggleDifficulty, Difficulty.HARD),
        broadcast(s"${AQUA}フィーバー終了！MOBたちは戻ってきたぞ！")
      ).sequence.void
    }
    .asNonBlockingTabExecutor()
}

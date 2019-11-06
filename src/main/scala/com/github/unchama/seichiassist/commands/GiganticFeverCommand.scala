package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import com.github.unchama.targetedeffect.emptyEffect
import org.bukkit.ChatColor._
import org.bukkit.Difficulty
import org.bukkit.command.TabExecutor

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object GiganticFeverCommand {
  private val worldsToToggleDifficulty = ManagedWorld.seichiWorlds.map(_.alphabetName).toList

  val executor: TabExecutor = ContextualExecutorBuilder.beginConfiguration()
    .execution { _ =>
      val config = SeichiAssist.seichiAssistConfig

      Util.sendEveryMessage(s"${AQUA}フィーバー！この時間MOBたちは踊りに出かけてるぞ！今が整地時だ！")
      Util.sendEveryMessage(s"$AQUA(${config.getGiganticFeverDisplayTime}間)")

      Util.setDifficulty(worldsToToggleDifficulty, Difficulty.PEACEFUL)

      IO.sleep(FiniteDuration(config.getGiganticFeverMinutes * 60,
        scala.concurrent.duration.MINUTES))(IO.timer(ExecutionContext.global))

      Util.setDifficulty(worldsToToggleDifficulty, Difficulty.HARD)
      Util.sendEveryMessage(s"${AQUA}フィーバー終了！MOBたちは戻ってきたぞ！")

      IO(emptyEffect)
    }
    .build()
    .asNonBlockingTabExecutor()
}

package com.github.unchama.seichiassist.commands

import arrow.core.None
import arrow.core.Some
import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.contextualexecutor.builder.ContextualExecutorBuilder
import com.github.unchama.contextualexecutor.builder.response.asResponseToSender
import com.github.unchama.contextualexecutor.builder.response.plus
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object EffectCommand {

  private val toggleExecutor = ContextualExecutorBuilder
      .beginConfiguration()
      .refineSenderWithError<Player>("${ChatColor.GREEN}このコマンドはゲーム内から実行してください。")
      .execution { context ->
        val playerData = SeichiAssist.playermap[context.sender.uniqueId] ?: return@execution None
        val toggleResponse = playerData.toggleEffect()
        val guidance = "再度 /ef コマンドを実行することでトグルします。".asResponseToSender()

        Some(toggleResponse + guidance)
      }
      .build()

  private val messageFlagToggleExecutor = ContextualExecutorBuilder
      .beginConfiguration()
      .refineSenderWithError<Player>("${ChatColor.GREEN}このコマンドはゲーム内から実行してください。")
      .execution { context ->
        val playerData = SeichiAssist.playermap[context.sender.uniqueId] ?: return@execution None
        val toggleResponse = playerData.toggleMessageFlag()

        Some(toggleResponse)
      }
      .build()

  val executor = BranchedExecutor(
      mapOf("smart" to messageFlagToggleExecutor),
      whenArgInsufficient = toggleExecutor
  ).asNonBlockingTabExecutor()

}

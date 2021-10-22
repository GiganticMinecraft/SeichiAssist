package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.targetedeffect.player.PlayerEffects
import org.bukkit.command.TabExecutor

/**
 * エンダーチェストを開くコマンド
 */
object EnderChestCommand {
  val executor: TabExecutor = playerCommandBuilder
    .argumentsParsers(List())
    .execution { context =>
      val sender = context.sender

      IO {
        PlayerEffects.openInventoryEffect(sender.getEnderChest)
      }
    }
    .build()
    .asNonBlockingTabExecutor()
}

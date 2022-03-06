package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import org.bukkit.command.TabExecutor

object HatCommand {
  val executor: TabExecutor = BuilderTemplates
    .playerCommandBuilder
    .argumentsParsers(List())
    .execution { context =>
      val player = context.sender
      val mainHandItem = player.getInventory.getItemInMainHand
      val currentHeadItem = player.getInventory.getHelmet

      IO {
        SequentialEffect(
          TargetedEffect.delay { p =>
            // swapすることでアイテムの過不足を防ぐ
            p.getInventory.setHelmet(mainHandItem)
            p.getInventory.setItemInOffHand(currentHeadItem)
          },
          MessageEffect("メインハンドに持っていたアイテムを頭にかぶりました。")
        )
      }
    }
    .build()
    .asNonBlockingTabExecutor()
}

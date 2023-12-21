package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.{SequentialEffect, TargetedEffect}
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import org.bukkit.Material
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object HatCommand {
  val executor: TabExecutor = BuilderTemplates
    .playerCommandBuilder
    .buildWith { context =>
      val player = context.sender
      val mainHandItem = player.getInventory.getItemInMainHand
      val currentHeadItem = player.getInventory.getHelmet

      IO {
        if (mainHandItem.getType != Material.AIR) {
          SequentialEffect(
            TargetedEffect.delay[IO, Player] { p =>
              // swapすることでアイテムの過不足を防ぐ
              p.getInventory.setHelmet(mainHandItem)
              p.getInventory.setItemInMainHand(currentHeadItem)
            },
            MessageEffect("メインハンドに持っていたアイテムを頭にかぶりました。")
          )
        } else {
          emptyEffect
        }
      }
    }
    .asNonBlockingTabExecutor()
}

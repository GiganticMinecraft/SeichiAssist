package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.emptyEffect
import org.bukkit.command.TabExecutor
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

object StickCommand {
  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val sender = context.sender
      val stickItemStack = new ItemStack(Material.STICK, 1)

      if (!Util.isPlayerInventoryFull(sender)) {
        Util.addItem(sender, stickItemStack)
        sender.playSound(sender.getLocation, Sound.ENTITY_ITEM_PICKUP, 0.1f, 1.0f)
      } else {
        Util.dropItem(sender, stickItemStack)
      }

      IO(emptyEffect)
    }
    .build()
    .asNonBlockingTabExecutor()
}
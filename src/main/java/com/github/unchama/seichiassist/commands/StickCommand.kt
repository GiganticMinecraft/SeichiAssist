package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.effect.EmptyMessage
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.Util
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack

object StickCommand {
  val executor = playerCommandBuilder
      .execution { context ->
        val sender = context.sender
        val stickItemStack = ItemStack(Material.STICK, 1)

        if (!Util.isPlayerInventoryFull(sender)) {
          Util.addItem(sender, stickItemStack)
          sender.playSound(sender.location, Sound.ENTITY_ITEM_PICKUP, 0.1f, 1.0f)
        } else {
          Util.dropItem(sender, stickItemStack)
        }

        EmptyMessage
      }
      .build()
      .asNonBlockingTabExecutor()
}
package com.github.unchama.seichiassist.commands

import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.EmptyEffect
import org.bukkit.{Material, Sound}

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

        EmptyEffect
      }
      .build()
      .asNonBlockingTabExecutor()
}
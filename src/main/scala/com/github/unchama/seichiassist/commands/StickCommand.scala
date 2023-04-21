package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.menus.StickItemData
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.TabExecutor
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

import scala.util.chaining.scalaUtilChainingOps

object StickCommand {
  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val sender = context.sender
      val stick = StickItemData.stick

      if (!InventoryOperations.isPlayerInventoryFull(sender)) {
        InventoryOperations.addItem(sender, stick)
        sender.playSound(sender.getLocation, Sound.ENTITY_ITEM_PICKUP, 0.1f, 1.0f)
      } else {
        InventoryOperations.dropItem(sender, stick)
      }

      IO(TargetedEffect.emptyEffect)
    }
    .build()
    .asNonBlockingTabExecutor()
}

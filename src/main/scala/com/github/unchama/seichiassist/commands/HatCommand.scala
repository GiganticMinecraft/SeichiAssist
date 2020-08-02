package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.command.TabExecutor
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack
import org.bukkit.Material
import org.bukkit.entity.Player

object HatCommand {
  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val sender = context.sender

      if (sender.isInstanceOf[Player]) {
        val helmet = sender.getInventory.getHelmet
        val itemInMainHand = sender.getInventory.getItemInMainHand
        if (itemInMainHand == null || itemInMainHand.getType == Material.AIR)
          MessageEffect(s"${RED}メインハンドにアイテムを持ってください。")
        else {
          if (helmet == null || helmet.getType == Material.AIR) {
            sender.getInventory.setHelmet(itemInMainHand)
            sender.getInventory.setItemInMainHand(new ItemStack(Material.AIR))
          } else {
            sender.getInventory.setHelmet(itemInMainHand)
            sender.getInventory.setItemInMainHand(helmet)
          }
          MessageEffect(s"${GREEN}手に持っているアイテムをかぶりました。")
        }
      } else MessageEffect(s"${RED}コンソールからは実行できません。")

      IO(TargetedEffect.emptyEffect)
    }
    .build()
    .asNonBlockingTabExecutor()
}

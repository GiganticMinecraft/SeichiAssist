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

object HatCommand {
  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val sender = context.sender
      val args = context.args.yetToBeParsed

      if (args.size != 0) sender.sendMessage(s"${RED}コマンドの使用法が間違っています。")
      else {
        val helmet: ItemStack = sender.getInventory.getHelmet
        val itemInMainHand: ItemStack = sender.getInventory.getItemInMainHand
        if (itemInMainHand == null || itemInMainHand.getType == Material.AIR)
          sender.sendMessage(s"${RED}メインハンドにアイテムを持ってください。")
        else {
          if (helmet == null || helmet.getType == Material.AIR) {
            sender.getInventory.setHelmet(itemInMainHand)
            sender.getInventory.setItemInMainHand(new ItemStack(Material.AIR))
          } else {
            sender.getInventory.setHelmet(itemInMainHand)
            sender.getInventory.setItemInMainHand(helmet)
          }
          sender.sendMessage(s"${GREEN}手に持っているアイテムをかぶりました。")
        }
      }

      IO(TargetedEffect.emptyEffect)
    }
    .build()
    .asNonBlockingTabExecutor()
}

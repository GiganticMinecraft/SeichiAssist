package com.github.unchama.seichiassist.subsystems.rescueplayer.bukkit.itemstack

import org.bukkit.{Bukkit, Material}
import org.bukkit.ChatColor.GREEN
import org.bukkit.inventory.ItemStack

import scala.jdk.CollectionConverters._
import scala.util.chaining._

object BukkitLoginBonusTicketItemStack {
  val loginBonusTicket: ItemStack = {
    val lore = List(
      "1日1回ログインすることでもらえるチケットです。"
    ).asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.PAPER).tap { meta =>
      import meta._
      setDisplayName(s"${GREEN}ログインボーナスチケット")
      setLore(lore)
    }

    val itemStack = new ItemStack(Material.PAPER, 1)
    itemStack.setItemMeta(itemMeta)
    itemStack
  }
}
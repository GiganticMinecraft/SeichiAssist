package com.github.unchama.seichiassist.items

import org.bukkit.{Bukkit, Material}
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.ChatColor._

object ExchangeTicket {
  def itemStack: ItemStack = {
    val item = new ItemStack(Material.PAPER)
    val meta = Bukkit.getItemFactory.getItemMeta(Material.PAPER)
    meta.setDisplayName(s"$DARK_RED${BOLD}交換券")
    meta.addEnchant(Enchantment.PROTECTION_FIRE, 1, false)
    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
    item.setItemMeta(meta)
    item.clone()
  }
}

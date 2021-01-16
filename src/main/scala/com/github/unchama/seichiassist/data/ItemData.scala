package com.github.unchama.seichiassist.data

import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory
import org.bukkit.ChatColor._
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Bukkit, Material}

import java.util

object ItemData {
  def getSuperPickaxe(amount: Int): ItemStack = {
    val itemstack = new ItemStack(Material.DIAMOND_PICKAXE, amount)
    val itemmeta = Bukkit.getItemFactory.getItemMeta(Material.DIAMOND_PICKAXE)
    itemmeta.setDisplayName(s"$YELLOW${BOLD}Thanks for Voting!")
    val lore = util.Arrays.asList("投票ありがとナス♡")
    itemmeta.addEnchant(Enchantment.DIG_SPEED, 3, true)
    itemmeta.addEnchant(Enchantment.DURABILITY, 3, true)
    itemmeta.setLore(lore)
    itemstack.setItemMeta(itemmeta)
    itemstack
  }

  def getForLevelUpskull(name: String, amount: Int): ItemStack = {
    val skull = new ItemStack(Material.SKULL_ITEM, amount)
    val skullmeta = Bukkit.getItemFactory.getItemMeta(Material.SKULL_ITEM).asInstanceOf[SkullMeta]
    skull.setDurability(3.toShort)
    skullmeta.setDisplayName(s"$YELLOW${BOLD}ガチャ券")
    val lore = util.Arrays.asList(s"$RESET${GREEN}右クリックで使えます", s"$RESET${DARK_GREEN}所有者：$name", RESET + "" + DARK_RED + "レベルアップ記念です")
    skullmeta.setLore(lore)
    skullmeta.setOwner("unchama")
    skull.setItemMeta(skullmeta)
    skull
  }

  def getGachaApple(amount: Int): ItemStack = {
    val gachaimo = new ItemStack(Material.GOLDEN_APPLE, amount)
    val meta = Bukkit.getItemFactory.getItemMeta(Material.GOLDEN_APPLE)
    meta.setDisplayName(StaticGachaPrizeFactory.getGachaRingoName)
    val lore = StaticGachaPrizeFactory.getGachaRingoLore
    meta.setLore(lore)
    gachaimo.setItemMeta(meta)
    gachaimo
  }

  def getElsa(amount: Int): ItemStack = {
    val elsa = new ItemStack(Material.DIAMOND_BOOTS, amount)
    val elsaMeta = Bukkit.getItemFactory.getItemMeta(Material.DIAMOND_BOOTS)
    elsaMeta.setDisplayName(s"$AQUA${ITALIC}エルサ")
    val lore = util.Arrays.asList("", s"${GREEN}装備中の移動速度$YELLOW(中)$GREEN", "", YELLOW + "金床" + RED + "不可", YELLOW + "修繕エンチャント" + AQUA + "可")
    elsaMeta.setLore(lore)
    elsaMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, false)
    elsaMeta.addEnchant(Enchantment.FROST_WALKER, 2, false)
    elsaMeta.addEnchant(Enchantment.DURABILITY, 4, false)
    elsaMeta.addEnchant(Enchantment.PROTECTION_FALL, 6, true)
    elsaMeta.addEnchant(Enchantment.MENDING, 1, false)
    elsa.setItemMeta(elsaMeta)
    elsa
  }

  def getVotingGift(amount: Int): ItemStack = {
    val gift = new ItemStack(Material.PAPER, amount)
    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.PAPER)
    itemMeta.setDisplayName(s"${AQUA}投票ギフト券")
    val lore = util.Arrays.asList("", s"${WHITE}公共施設鯖にある", WHITE + "デパートで買い物ができます")
    itemMeta.setLore(lore)
    gift.setItemMeta(itemMeta)
    gift
  }
}

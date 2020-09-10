package com.github.unchama.seichiassist.data

import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta
import java.util


/**
 * Created by karayuu on 2018/04/20
 */
object ItemData {
  def getSuperPickaxe(amount: Int): ItemStack = {
    val itemstack = new ItemStack(Material.DIAMOND_PICKAXE, amount)
    val itemmeta = Bukkit.getItemFactory.getItemMeta(Material.DIAMOND_PICKAXE)
    itemmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Thanks for Voting!")
    val lore = java.util.Arrays.asList("投票ありがとナス♡")
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
    skullmeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "ガチャ券")
    val lore = java.util.Arrays.asList(ChatColor.RESET + "" + ChatColor.GREEN + "右クリックで使えます", ChatColor.RESET + "" + ChatColor.DARK_GREEN + "所有者：" + name, ChatColor.RESET + "" + ChatColor.DARK_RED + "レベルアップ記念です")
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
    elsaMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.ITALIC + "エルサ")
    val lore = java.util.Arrays.asList("", ChatColor.GREEN + "装備中の移動速度" + ChatColor.YELLOW + "(中)" + ChatColor.GREEN, "", ChatColor.YELLOW + "金床" + ChatColor.RED + "不可", ChatColor.YELLOW + "修繕エンチャント" + ChatColor.AQUA + "可")
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
    itemMeta.setDisplayName(ChatColor.AQUA + "投票ギフト券")
    val lore = java.util.Arrays.asList("", ChatColor.WHITE + "公共施設鯖にある", ChatColor.WHITE + "デパートで買い物ができます")
    itemMeta.setLore(lore)
    gift.setItemMeta(itemMeta)
    gift
  }
}

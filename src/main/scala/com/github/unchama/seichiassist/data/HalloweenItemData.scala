package com.github.unchama.seichiassist.data

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import java.util._
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Color.fromRGB
import org.bukkit.enchantments.Enchantment
import org.bukkit.Material
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.inventory.meta.{ItemMeta, PotionMeta}
import org.bukkit.potion.{PotionEffect, PotionEffectType}

object HalloweenItemData {
  def getHalloweenPotion(): ItemStack = {
    val potion = new ItemStack(Material.POTION, 1)
    val potionMeta: PotionMeta = Bukkit.getItemFactory.getItemMeta(Material.POTION).asInstanceOf[PotionMeta]
    potionMeta.setDisplayName(s"${ChatColor.AQUA}${ChatColor.ITALIC}うんちゃまの汗")
    potionMeta.setColor(fromRGB(1, 93, 178))
    potionMeta.addEnchant(Enchantment.MENDING, 1, true)
    potionMeta.setLore(halloweenPotionLoreList())
    halloweenPotionItemFlags.foreach {
      potionMeta.addItemFlags(_)
    }
    halloweenPotionEffects.foreach {
      potionMeta.addCustomEffect(_, true)
    }
    potion.setItemMeta(potionMeta)
    potion
  }

  private val halloweenPotionItemFlags = Seq(
    ItemFlag.HIDE_ENCHANTS,
    ItemFlag.HIDE_POTION_EFFECTS
  )

  private val halloweenPotionEffects = Seq(
    new PotionEffect(PotionEffectType.REGENERATION, 200, 3),
    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 400, 2),
    new PotionEffect(PotionEffectType.NIGHT_VISION, 12000, 0),
    new PotionEffect(PotionEffectType.LUCK, 6000, 0)
  )

  private def halloweenPotionLoreList() = {
    val loreList = mutable.ListBuffer[String]()
    loreList += ""
    val year = Calendar.getInstance().get(Calendar.YEAR)
    loreList += s"${ChatColor.RESET}${ChatColor.GRAY}${year}ハロウィンイベント限定品"
    loreList += s"${ChatColor.RESET}${ChatColor.GRAY}敵に囲まれてピンチの時や"
    loreList += s"${ChatColor.RESET}${ChatColor.GRAY}MEBIUS育成中の時などにご利用ください"
    loreList.toList
  }.asJava
}

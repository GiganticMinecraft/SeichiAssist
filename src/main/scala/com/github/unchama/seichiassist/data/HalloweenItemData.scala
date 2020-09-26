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

  private val halloweenPotionItemFlags = Set(
    ItemFlag.HIDE_ENCHANTS,
    ItemFlag.HIDE_POTION_EFFECTS
  )

  private val halloweenPotionEffects = Set(
    new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 3),
    new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 20, 2),
    new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60 * 10, 0),
    new PotionEffect(PotionEffectType.LUCK, 20 * 60 * 5, 0)
  )

  private def halloweenPotionLoreList() = {
    val year = Calendar.getInstance().get(Calendar.YEAR)
    List(
      "",
      s"${ChatColor.RESET}${ChatColor.GRAY}${year}ハロウィンイベント限定品",
      s"${ChatColor.RESET}${ChatColor.GRAY}敵に囲まれてピンチの時や",
      s"${ChatColor.RESET}${ChatColor.GRAY}MEBIUS育成中の時などにご利用ください"
    )
  }.asJava
}

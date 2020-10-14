package com.github.unchama.seichiassist.data

import scala.collection.immutable.{List, Set}
import scala.jdk.CollectionConverters._
import scala.util.chaining._
import java.util._

import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Color.fromRGB
import org.bukkit.enchantments.Enchantment
import org.bukkit.Material
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.inventory.meta.{ItemMeta, PotionMeta}
import org.bukkit.potion.{PotionEffect, PotionEffectType}

object HalloweenItemData {

  /*
  HalloweenPotionここから
   */

  def getHalloweenPotion: ItemStack = {
    val potionMeta: PotionMeta = Bukkit.getItemFactory.getItemMeta(Material.POTION).asInstanceOf[PotionMeta]
      .tap(_.setDisplayName(s"${ChatColor.AQUA}${ChatColor.ITALIC}うんちゃまの汗"))
      .tap(_.setColor(fromRGB(1, 93, 178)))
      .tap(_.addEnchant(Enchantment.MENDING, 1, true))
      .tap(_.setLore(halloweenPotionLoreList()))
      .tap(meta =>
        halloweenPotionItemFlags.foreach(flg =>
          meta.addItemFlags(flg)
        )
      )
      .tap(meta =>
        halloweenPotionEffects.foreach (ef =>
          meta.addCustomEffect(ef, true)
        )
      )

    val potion = new ItemStack(Material.POTION, 1)
    potion.setItemMeta(potionMeta)

    val nbtItem = new NBTItem(potion)
    nbtItem.setByte(NBTTagConstants.typeIdTag, 1.toByte)
    nbtItem.getItem
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

  def isHalloweenPotion(itemStack: ItemStack): Boolean = {
    if (itemStack != null && itemStack.getType != Material.AIR) {
      new NBTItem(itemStack).getByte(NBTTagConstants.typeIdTag) == 1
    } else {
      false
    }
  }

  /*
  HalloweenPotionここまで
   */

  private object NBTTagConstants {
    val typeIdTag = "halloweenItemTypeId"
  }
}

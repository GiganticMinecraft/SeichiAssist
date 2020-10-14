package com.github.unchama.seichiassist.data

import java.util._

import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.Color.fromRGB
import org.bukkit.{Bukkit, Material}
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.potion.{PotionEffect, PotionEffectType}

import scala.collection.immutable.{List, Set}
import scala.jdk.CollectionConverters._
import scala.util.chaining._

object HalloweenItemData {

  /*
  HalloweenPotionここから
   */

  def getHalloweenPotion: ItemStack = {
    val potionMeta = Bukkit.getItemFactory.getItemMeta(Material.POTION).asInstanceOf[PotionMeta]
      .tap(_.setDisplayName(s"$AQUA${ITALIC}うんちゃまの汗"))
      .tap(_.setColor(fromRGB(1, 93, 178)))
      // 意味のないエンチャント。エンチャントが付与されている時の紫色のキラキラをつけるため。
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
      s"$RESET$GRAY${year}ハロウィンイベント限定品",
      "敵に囲まれてピンチの時や",
      "MEBIUS育成中の時などにご利用ください",
      "飲むと強くなりますし",
      "飲みすぎても死にません"
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

  /*
  HalloweenHoeここから
   */

  def getHalloweenHoe: ItemStack = {
    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.DIAMOND_HOE)
      .tap(_.setDisplayName(halloweenHoeName))
      // 意味のないエンチャント。エンチャントが付与されている時の紫色のキラキラをつけるため。
      .tap(_.addEnchant(Enchantment.ARROW_INFINITE, 1, true))
      .tap(_.setLore(halloweenHoeLoreList()))
      .tap(meta =>
        halloweenHoeItemFlags.foreach(flg =>
          meta.addItemFlags(flg)
        )
      )

    val hoe = new ItemStack(Material.DIAMOND_HOE, 1)
    hoe.setItemMeta(itemMeta)

    val nbtItem = new NBTItem(hoe)
    nbtItem.setByte(NBTTagConstants.typeIdTag, 2.toByte)
    nbtItem.getItem
  }

  private val halloweenHoeName =
    List(s"${RED}C", s"${GOLD}E", s"${YELLOW}N", s"${GREEN}T", s"${BLUE}E", s"${DARK_AQUA}O", s"${LIGHT_PURPLE}T", s"${RED}L")
      .foldLeft(""){ (name, str) => name + str.patch(2, s"$BOLD$ITALIC", 0) }

  private val halloweenHoeItemFlags = Set(
    ItemFlag.HIDE_ENCHANTS,
    ItemFlag.HIDE_UNBREAKABLE
  )

  private def halloweenHoeLoreList() = {
    val year = Calendar.getInstance().get(Calendar.YEAR)
    List(
      "",
      s"$RESET$GRAY${year}ハロウィンイベント限定品",
      "特殊なエンチャントが付与されています",
      "",
      // TODO: テクスチャコンペ終了時に記載する
      s"$RESET${WHITE}テクスチャ名：「」",
      "製作者："
    )
  }.asJava

  def isHalloweenHoe(itemStack: ItemStack): Boolean = {
    if (itemStack != null && itemStack.getType != Material.AIR) {
      new NBTItem(itemStack).getByte(NBTTagConstants.typeIdTag) == 2
    } else {
      false
    }
  }

  /*
  HalloweenHoeここまで
   */

  private object NBTTagConstants {
    val typeIdTag = "halloweenItemTypeId"
  }
}

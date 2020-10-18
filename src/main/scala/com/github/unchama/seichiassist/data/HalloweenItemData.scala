package com.github.unchama.seichiassist.data

import java.util.Calendar

import com.github.unchama.seichiassist.util.Util
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

  //region HalloweenPotion

  val halloweenPotion: ItemStack = {
    val itemFlags = Set(
      ItemFlag.HIDE_ENCHANTS,
      ItemFlag.HIDE_POTION_EFFECTS
    )
    val potionEffects = Set(
      new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 3),
      new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 20, 2),
      new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60 * 10, 0),
      new PotionEffect(PotionEffectType.LUCK, 20 * 60 * 5, 0)
    )
    val loreList = {
      val year = Calendar.getInstance().get(Calendar.YEAR)
      List(
        s"${year}ハロウィンイベント限定品",
        "",
        "敵に囲まれてピンチの時や",
        "MEBIUS育成中の時などにご利用ください",
        "飲むと強くなりますし",
        "飲みすぎても死にません"
      ).map(str => s"$RESET$GRAY$str")
    }.asJava

    val potionMeta = Bukkit.getItemFactory.getItemMeta(Material.POTION).asInstanceOf[PotionMeta]
      .tap(_.setDisplayName(s"$AQUA${ITALIC}うんちゃまの汗"))
      .tap(_.setColor(fromRGB(1, 93, 178)))
      // 意味のないエンチャント。エンチャントが付与されている時の紫色のキラキラをつけるため。
      .tap(_.addEnchant(Enchantment.MENDING, 1, true))
      .tap(_.setLore(loreList))
      .tap(meta =>
        itemFlags.foreach(flg =>
          meta.addItemFlags(flg)
        )
      )
      .tap(meta =>
        potionEffects.foreach (ef =>
          meta.addCustomEffect(ef, true)
        )
      )

    val potion = new ItemStack(Material.POTION, 1)
    potion.setItemMeta(potionMeta)

    val nbtItem = new NBTItem(potion)
    nbtItem.setByte(NBTTagConstants.typeIdTag, 1.toByte)
    nbtItem.getItem
  }

  def isHalloweenPotion(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getType != Material.AIR && {
      new NBTItem(itemStack)
        .getByte(NBTTagConstants.typeIdTag) == 1
    }

  //endregion

  //region HalloweenHoe

  val halloweenHoe: ItemStack = {
    val displayName = List(
      s"${RED}C", s"${GOLD}E", s"${YELLOW}N", s"${GREEN}T", s"${BLUE}E", s"${DARK_AQUA}O", s"${LIGHT_PURPLE}T", s"${RED}L")
      .foldLeft(""){(name, str) => name + str.patch(2, s"$BOLD$ITALIC", 0)}
    val enchantments = Set(
      (Enchantment.DURABILITY, 7),
      (Enchantment.DIG_SPEED, 7),
      (Enchantment.MENDING, 1)
    )
    val loreList = {
      val year = Calendar.getInstance().get(Calendar.YEAR)
      val enchDescription = enchantments.map( ench =>
        s"$RESET$GRAY${Util.getEnchantName(ench._1.getName, ench._2)}"
      ).toList
      val lore = List(
        "",
        s"$GRAY${year}ハロウィンイベント限定品",
        "",
        s"${YELLOW}特殊なエンチャントが付与されています",
        "",
        s"${WHITE}テクスチャ名：「バットクワン」",
        s"${WHITE}製作者：SpecialBoyWaka"
      ).map(str => s"$RESET$str")
      enchDescription ::: lore
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.DIAMOND_HOE)
      .tap(_.setDisplayName(displayName))
      .tap(_.setLore(loreList))
      .tap(_.addItemFlags(ItemFlag.HIDE_ENCHANTS))
      .tap(meta =>
        enchantments.foreach( ench =>
          meta.addEnchant(ench._1, ench._2, true)
        )
      )

    val hoe = new ItemStack(Material.DIAMOND_HOE, 1)
    hoe.setItemMeta(itemMeta)

    val nbtItem = new NBTItem(hoe)
    nbtItem.setByte(NBTTagConstants.typeIdTag, 2.toByte)
    nbtItem.getItem
  }

  def isHalloweenHoe(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getType != Material.AIR && {
      new NBTItem(itemStack)
        .getByte(NBTTagConstants.typeIdTag) == 2
    }

  //endregion

  private object NBTTagConstants {
    val typeIdTag = "halloweenItemTypeId"
  }
}

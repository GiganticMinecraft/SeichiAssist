package com.github.unchama.seasonalevents.halloween

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

    val potionMeta = Bukkit.getItemFactory.getItemMeta(Material.POTION).asInstanceOf[PotionMeta].tap {meta =>
      import meta._
      setDisplayName(s"$AQUA${ITALIC}うんちゃまの汗")
      setColor(fromRGB(1, 93, 178))
      addEnchant(Enchantment.MENDING, 1, true)
      setLore(loreList)
      itemFlags.foreach(flg => addItemFlags(flg))
      potionEffects.foreach (effect => addCustomEffect(effect, true))
    }

    val potion = new ItemStack(Material.POTION, 1)
    potion.setItemMeta(potionMeta)

    new NBTItem(potion)
      .tap(_.setByte(NBTTagConstants.typeIdTag, 1.toByte))
      .pipe(_.getItem)
  }

  def isHalloweenPotion(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getType != Material.AIR && {
      new NBTItem(itemStack)
        .getByte(NBTTagConstants.typeIdTag) == 1
    }

  //endregion

  //region HalloweenHoe

  val halloweenHoe: ItemStack = {
    val displayName = Seq(
      "C" -> RED,
      "E" -> GOLD,
      "N" -> YELLOW,
      "T" -> GREEN,
      "E" -> BLUE,
      "O" -> DARK_AQUA,
      "T" -> LIGHT_PURPLE,
      "L" -> RED
    ).map {case (c, color) => s"$color$BOLD$ITALIC$c"}
      .mkString
    val enchantments = Set(
      (Enchantment.DURABILITY, 7),
      (Enchantment.DIG_SPEED, 7),
      (Enchantment.MENDING, 1)
    )
    val loreList = {
      val year = Calendar.getInstance().get(Calendar.YEAR)
      val enchDescription = enchantments
        .map {case (ench, lvl) => s"$RESET$GRAY${Util.getEnchantName(ench.getName, lvl)}"}
        .toList
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

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.DIAMOND_HOE).tap { meta =>
      import meta._
      setDisplayName(displayName)
      setLore(loreList)
      addItemFlags(ItemFlag.HIDE_ENCHANTS)
      enchantments.foreach {case (ench, lvl) => addEnchant(ench, lvl, true)}
    }

    val hoe = new ItemStack(Material.DIAMOND_HOE, 1)
    hoe.setItemMeta(itemMeta)

    new NBTItem(hoe)
      .tap(_.setByte(NBTTagConstants.typeIdTag, 2.toByte))
      .pipe(_.getItem)
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

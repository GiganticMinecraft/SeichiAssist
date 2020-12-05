package com.github.unchama.seasonalevents.christmas

import java.time.LocalDate

import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor.{AQUA, GRAY, ITALIC, RESET}
import org.bukkit.Color.fromRGB
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.potion.{PotionEffect, PotionEffectType}
import org.bukkit.{Bukkit, Material}

import scala.collection.immutable.{List, Set}
import scala.jdk.CollectionConverters._
import scala.util.chaining._

object ChristmasItemData {

  //region ChristmasCake

  val ChristmasCake: ItemStack = {
    val itemFlags = Set(
      ItemFlag.HIDE_ENCHANTS
    )
    val loreList = {
      val year = LocalDate.now().getYear
      List(
        "",
        s"${year}クリスマスイベント限定品",
        "",
        "一口で食べられます",
        "食べると不運か幸運がランダムで付与されます"
      ).map(str => s"$RESET$GRAY$str")
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.CAKE).tap { meta =>
      import meta._
      setDisplayName(s"$AQUA${ITALIC}まいんちゃん特製クリスマスケーキ")
      addEnchant(Enchantment.MENDING, 1, true)
      setLore(loreList)
      itemFlags.foreach(flg => addItemFlags(flg))
    }

    val cake = new ItemStack(Material.CAKE, 1)
    cake.setItemMeta(itemMeta)

    new NBTItem(cake).tap { nbtItem =>
      import nbtItem._
      setByte(NBTTagConstants.typeIdTag, 1.toByte)
      setByte(NBTTagConstants.cakePieceTag, 7.toByte)
    }
      .pipe(_.getItem)
  }

  def isChristmasCake(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getType != Material.AIR && {
      new NBTItem(itemStack)
        .getByte(NBTTagConstants.typeIdTag) == 1
    }

  //endregion

  //region ChristmasTurkey

  val ChristmasTurkey: ItemStack = {
    val itemFlags = Set(
      ItemFlag.HIDE_ENCHANTS
    )
    val loreList = {
      val year = LocalDate.now().getYear
      List(
        "",
        s"${year}クリスマスイベント限定品",
        "",
        "食べると移動速度上昇か低下がランダムで付与されます"
      ).map(str => s"$RESET$GRAY$str")
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKED_CHICKEN).tap { meta =>
      import meta._
      setDisplayName(s"$AQUA${ITALIC}まいんちゃん特製ローストターキー")
      addEnchant(Enchantment.MENDING, 1, true)
      setLore(loreList)
      itemFlags.foreach(flg => addItemFlags(flg))
    }

    val cake = new ItemStack(Material.COOKED_CHICKEN, 1)
    cake.setItemMeta(itemMeta)

    new NBTItem(cake).tap { nbtItem =>
      import nbtItem._
      setByte(NBTTagConstants.typeIdTag, 2.toByte)
    }
      .pipe(_.getItem)
  }

  def isChristmasTurkey(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getType != Material.AIR && {
      new NBTItem(itemStack)
        .getByte(NBTTagConstants.typeIdTag) == 2
    }

  //endregion

  //region ChristmasPotion

  val christmasPotion: ItemStack = {
    val itemFlags = Set(
      ItemFlag.HIDE_ENCHANTS,
      ItemFlag.HIDE_POTION_EFFECTS
    )
    val potionEffects = Set(
      new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 30, 0),
      new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 0)
    )
    val loreList = {
      val year = LocalDate.now().getYear
      List(
        s"${year}クリスマスイベント限定品",
        "",
        "クリスマスを一人で過ごす鯖民たちの涙（血涙）を集めた瓶"
      ).map(str => s"$RESET$GRAY$str")
    }.asJava

    val potionMeta = Bukkit.getItemFactory.getItemMeta(Material.POTION).asInstanceOf[PotionMeta].tap { meta =>
      import meta._
      setDisplayName(s"$AQUA${ITALIC}みんなの涙")
      setColor(fromRGB(215, 0, 58))
      addEnchant(Enchantment.MENDING, 1, true)
      setLore(loreList)
      itemFlags.foreach(flg => addItemFlags(flg))
      potionEffects.foreach(effect => addCustomEffect(effect, true))
    }

    val potion = new ItemStack(Material.POTION, 1)
    potion.setItemMeta(potionMeta)

    new NBTItem(potion)
      .tap(_.setByte(NBTTagConstants.typeIdTag, 3.toByte))
      .pipe(_.getItem)
  }

  def isChristmasPotion(itemStack: ItemStack): Boolean =
    itemStack != null && itemStack.getType != Material.AIR && {
      new NBTItem(itemStack)
        .getByte(NBTTagConstants.typeIdTag) == 3
    }

  //endregion

  object NBTTagConstants {
    val typeIdTag = "christmasItemTypeId"
    val cakePieceTag = "christmasCakePiece"
  }

}
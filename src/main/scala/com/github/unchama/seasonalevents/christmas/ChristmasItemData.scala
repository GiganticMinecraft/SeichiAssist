package com.github.unchama.seasonalevents.christmas

import java.time.LocalDate

import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor.{AQUA, GRAY, ITALIC, RESET}
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.{Bukkit, Material}

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

  object NBTTagConstants {
    val typeIdTag = "christmasItemTypeId"
    val cakePieceTag = "christmasCakePiece"
  }

}
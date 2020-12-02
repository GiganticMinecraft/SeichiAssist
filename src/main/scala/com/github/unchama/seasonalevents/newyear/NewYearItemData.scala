package com.github.unchama.seasonalevents.newyear

import java.util.UUID

import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.seasonalevents.SkullData
import com.github.unchama.seasonalevents.newyear.NewYear.{DISTRIBUTED_SOBA_DATE, END_DATE}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.{Bukkit, Material}

import scala.jdk.CollectionConverters._
import scala.util.chaining._

object NewYearItemData {
  val newYearApple: ItemStack = {
    val loreList = List(
      "",
      s"${GRAY}お正月に向けて作られたりんご。",
      s"${GRAY}栄養豊富で、食べるとマナが10%回復する。",
      s"${GRAY}お正月パワーが含まれているため、",
      s"${GRAY}賞味期限を超えると効果がなくなる。",
      "",
      s"${DARK_GREEN}消費期限：$END_DATE",
      s"${AQUA}マナ回復（10%）$GRAY （期限内）"
    ).map(str => s"$RESET$str")
      .asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.GOLDEN_APPLE).tap { meta =>
      import meta._
      setDisplayName(s"$GOLD${BOLD}正月りんご")
      setLore(loreList)
      addEnchant(Enchantment.DIG_SPEED, 1, true)
      addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }

    val itemStack = new ItemStack(Material.GOLDEN_APPLE, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack).tap { item =>
      import item._
      setByte(NBTTagConstants.typeIdTag, 1.toByte)
      setObject(NBTTagConstants.expiryDateTag, END_DATE)
    }
      .pipe(_.getItem)
  }

  def isNewYearApple(item: ItemStack): Boolean = {
    item != null && item.getType != Material.AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 1
    }
  }

  val newYearBag: ItemStack = {
    val loreList = List(
      "新年あけましておめでとうございます",
      s"新年をお祝いして$RED${UNDERLINE}お年玉袋${RESET}をプレゼント！",
      s"$RED${UNDERLINE}アルカディア、エデン、ヴァルハラサーバー メインワールドの",
      s"$RED${UNDERLINE}スポーン地点にいる村人で様々なアイテムに交換可能です。"
    ).map(str => s"$RESET$str")
      .asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.PAPER).tap { meta =>
      import meta._
      setDisplayName(s"${AQUA}お年玉袋")
      setLore(loreList)
      addEnchant(Enchantment.DIG_SPEED, 1, true)
      addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }

    val itemStack = new ItemStack(Material.PAPER, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack).tap { item =>
      import item._
      setByte(NBTTagConstants.typeIdTag, 2.toByte)
      setObject(NBTTagConstants.expiryDateTag, END_DATE)
    }
      .pipe(_.getItem)
  }

  val sobaHead: ItemStack = new SkullItemStackBuilder(UUID.randomUUID(), SkullData.NewYearSoba.textureValue)
    .title(s"年越し蕎麦(${DISTRIBUTED_SOBA_DATE.getYear}年)")
    .lore(List(
      "",
      s"${YELLOW}大晦日記念アイテムだよ!"
    ))
    .build()

  object NBTTagConstants {
    val typeIdTag = "newYearItemTypeId"
    val expiryDateTag = "newYearAppleExpiryDate"
    val eventYearTag = "newYearEventYear"
  }

}
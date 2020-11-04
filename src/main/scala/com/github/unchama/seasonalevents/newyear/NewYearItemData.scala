package com.github.unchama.seasonalevents.newyear

import com.github.unchama.seasonalevents.newyear.NewYear.END_DATE
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
    ).asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.GOLDEN_APPLE)
      .tap(_.setDisplayName(s"$GOLD${BOLD}正月りんご"))
      .tap(_.setLore(loreList))
      .tap(_.addEnchant(Enchantment.DIG_SPEED, 20 * 50, true))
      .tap(_.addItemFlags(ItemFlag.HIDE_ENCHANTS))

    val itemStack = new ItemStack(Material.GOLDEN_APPLE, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap(_.setByte(NBTTagConstants.typeIdTag, 1.toByte))
      .tap(_.setObject(NBTTagConstants.expirationDateTag, END_DATE))
      .pipe(_.getItem)
  }

  def isNewYearApple(item: ItemStack): Boolean = {
    item != null && item.getType != Material.AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 1
    }
  }

  object NBTTagConstants {
    val typeIdTag = "newYearAppleTypeId"
    val expirationDateTag = "newYearAppleExpirationDate"
  }

}
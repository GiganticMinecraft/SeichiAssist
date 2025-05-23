package com.github.unchama.seichiassist.subsystems.seasonalevents.newyear

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerTextureValue}
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.subsystems.seasonalevents.newyear.NewYear.{
  END_DATE,
  EVENT_YEAR,
  NEW_YEAR_EVE
}
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
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
    ).map(str => s"$RESET$str").asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.GOLDEN_APPLE).tap { meta =>
      import meta._
      setDisplayName(s"$GOLD${BOLD}正月りんご")
      setLore(loreList)
      addEnchant(Enchantment.DIG_SPEED, 1, true)
      addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }

    val itemStack = new ItemStack(Material.GOLDEN_APPLE, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap { item =>
        import item._
        setByte(NBTTagConstants.typeIdTag, 1.toByte)
        setLong(NBTTagConstants.expiryDateTag, END_DATE.toEpochDay)
      }
      .pipe(_.getItem)
  }

  def isNewYearApple(item: ItemStack): Boolean = {
    item != null && item.getType == Material.GOLDEN_APPLE && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 1
    }
  }

  val newYearBag: ItemStack = {
    val loreList = List(
      "新年あけましておめでとうございます",
      s"新年をお祝いして$RED${UNDERLINE}お年玉袋${RESET}をプレゼント！",
      s"$RED${UNDERLINE}アルカディア、エデン、ヴァルハラサーバー メインワールドの",
      s"$RED${UNDERLINE}スポーン地点にいる村人で様々なアイテムに交換可能です。"
    ).map(str => s"$RESET$str").asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.PAPER).tap { meta =>
      import meta._
      setDisplayName(s"${AQUA}お年玉袋(${EVENT_YEAR}年)")
      setLore(loreList)
      addEnchant(Enchantment.DIG_SPEED, 1, true)
      addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }

    val itemStack = new ItemStack(Material.PAPER, 1)
    itemStack.setItemMeta(itemMeta)

    itemStack
  }

  private val soba = SkullOwnerTextureValue(
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY4MzRiNWIyNTQyNmRlNjM1MzhlYzgyY2E4ZmJlY2ZjYmIzZTY4MmQ4MDYzNjQzZDJlNjdhNzYyMWJkIn19fQ=="
  )

  def sobaHead(implicit playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]): ItemStack =
    new SkullItemStackBuilder(soba)
      .title(s"年越し蕎麦(${NEW_YEAR_EVE.from.getYear}年)")
      .lore(List("", s"${YELLOW}大晦日記念アイテムだよ!"))
      .build()

  object NBTTagConstants {
    val typeIdTag = "newYearItemTypeId"
    val expiryDateTag = "newYearAppleExpiryDate"
    val eventYearTag = "newYearEventYear"
  }

}

package com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary

import com.github.unchama.itemstackbuilder.{SkullItemStackBuilder, SkullOwnerTextureValue}
import com.github.unchama.seichiassist.subsystems.seasonalevents.anniversary.Anniversary.ANNIVERSARY_COUNT
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.Material._
import org.bukkit.inventory.ItemStack

import scala.jdk.CollectionConverters._
import scala.util.chaining._

object AnniversaryItemData {

  //region まいんちゃんの記念頭

  private val mineChan = SkullOwnerTextureValue("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDhmNTQ0OGI0ZDg4ZTQwYjE0YzgyOGM2ZjFiNTliMzg1NDVkZGE5MzNlNzNkZmYzZjY5NWU2ZmI0Mjc4MSJ9fX0=")
  val mineHead: ItemStack = new SkullItemStackBuilder(mineChan)
    .title("まいんちゃん")
    .lore(List(
      "",
      s"${YELLOW}ギガンティック☆整地鯖${ANNIVERSARY_COUNT}周年記念だよ！"
    ))
    .build()

  //endregion

  //region 「気になる木」の苗

  val strangeSapling: ItemStack = {
    val loreList = List(
      "植えるとすぐ成長する。",
      "先端のブロックがランダムで変化する。",
      "極稀にあの「林檎」も...？"
    ).map(lore => s"$RESET$GRAY$lore")
      .asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(SAPLING).tap { meta =>
      import meta._
      setDisplayName(s"$GOLD${BOLD}「気になる木」の苗")
      setLore(loreList)
    }

    val itemStack = new ItemStack(SAPLING, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack).tap { item =>
      import item._
      setByte(NBTTagConstants.typeIdTag, 1.toByte)
    }
      .pipe(_.getItem)
  }

  val storangeSaplingBlockSet = Set(
    ANVIL, BEACON, BONE_BLOCK, BOOKSHELF, BRICK, CAKE, CAULDRON, COAL_BLOCK, DIAMOND_BLOCK,
    DRAGON_EGG, FENCE, FLOWER_POT, GLOWSTONE, GOLD_BLOCK, GRASS, ICE, IRON_BLOCK, MELON_BLOCK,
    NETHER_BRICK, QUARTZ_BLOCK, SAND, SPONGE, WORKBENCH
  )

  val storangeSaplingSiinaRate = 0.0003

  def isStrangeSapling(item: ItemStack) =
    item != null && item.getType != AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 1
    }

  //endregion

  //region 修繕の書

  val mendingBook: ItemStack = {
    val loreList = List(
      "手に持って右クリックすると",
      "オフハンドにあるアイテムの耐久値を全回復する"
    ).map(lore => s"$RESET$GRAY$lore")
      .asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(WRITTEN_BOOK).tap { meta =>
      import meta._
      setDisplayName(s"$GOLD${BOLD}修繕の書")
      setLore(loreList)
    }

    val itemStack = new ItemStack(WRITTEN_BOOK, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack).tap { item =>
      import item._
      setByte(NBTTagConstants.typeIdTag, 2.toByte)
    }
      .pipe(_.getItem)
  }

  def isMendingBook(item: ItemStack) =
    item != null && item.getType != AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 2
    }

  //endregion

  private object NBTTagConstants {
    val typeIdTag = "anniversaryItemTypeId"
  }

}
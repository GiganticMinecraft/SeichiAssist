package com.github.unchama.seichiassist.util

import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

import scala.jdk.CollectionConverters._

object ItemUtil {
  def isGachaTicket(itemStack: ItemStack): Boolean = {
    val containsRightClickMessage: String => Boolean = _.contains(s"${GREEN}右クリックで使えます")

    if (itemStack.getType != Material.SKULL_ITEM) return false

    val skullMeta = itemStack.getItemMeta.asInstanceOf[SkullMeta]

    if (!(skullMeta.hasOwner && skullMeta.getOwner == "unchama")) return false

    skullMeta.hasLore && skullMeta.getLore.asScala.exists(containsRightClickMessage)
  }

  // TODO: Codec
  def itemStackContainsOwnerName(itemstack: ItemStack, name: String): Boolean = {
    val meta = itemstack.getItemMeta

    val lore =
      if (meta.hasLore)
        meta.getLore.asScala.toList
      else
        Nil

    lore.exists(line =>
      line.contains("所有者：") && line.drop(line.indexOf("所有者：") + 4).toLowerCase == name.toLowerCase()
    )
  }

  // TODO: Codec
  def isMineHeadItem(itemstack: ItemStack): Boolean = {
    itemstack.getType == Material.CARROT_STICK &&
      loreIndexOf(itemstack.getItemMeta.getLore.asScala.toList, "頭を狩り取る形をしている...").nonEmpty
  }

  def isLimitedTitanItem(itemstack: ItemStack): Boolean = {
    itemstack.getType == Material.DIAMOND_AXE &&
      isContainedInLore(itemstack, "特別なタイタンをあなたに♡")
  }

  /**
   * 指定された`String`が指定された[[ItemStack]]のloreに含まれているかどうか
   *
   * @param itemStack 確認する`ItemStack`
   * @param sentence  探す文字列
   * @return 含まれていれば`true`、含まれていなければ`false`。ただし、`ItemStack`に`ItemMeta`と`Lore`のいずれかがなければfalse
   */
  def isContainedInLore(itemStack: ItemStack, sentence: String): Boolean =
    itemStack.hasItemMeta &&
      itemStack.getItemMeta.hasLore &&
      loreIndexOf(itemStack.getItemMeta.getLore.asScala.toList, sentence).nonEmpty

  /**
   * loreを捜査して、要素の中に`find`が含まれているかを調べる。
   *
   * @param lore 探される対象
   * @param find 探す文字列
   * @return 見つかった場合は`Some(index)`、見つからなかった場合は[[None]]
   */
  def loreIndexOf(lore: List[String], find: String): Option[Int] = {
    lore.zipWithIndex
      .find(_._1.contains(find))
      .map(_._2)
  }
}

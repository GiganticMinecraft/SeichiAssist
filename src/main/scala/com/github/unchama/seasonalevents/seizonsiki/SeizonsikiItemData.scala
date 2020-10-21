package com.github.unchama.seasonalevents.seizonsiki

import java.util

import com.github.unchama.seasonalevents.seizonsiki.Seizonsiki.FINISHDISP

import org.bukkit.{Bukkit, Material}
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

import scala.jdk.CollectionConverters._
import scala.util.chaining._

object SeizonsikiItemData {
  // アイテムがゾンごかどうかの判定
  // TODO NBT化
  def isZongoConsumed(item: ItemStack): Boolean = {
    // Lore取得
    if (!item.hasItemMeta || !item.getItemMeta.hasLore) return false
    val itemLore = item.getItemMeta.getLore
    val prizeLore = getZongoLore
    // 比較
    itemLore.containsAll(prizeLore)
  }

  def getZongoItemStack: ItemStack = {
    val itemMeta: ItemMeta = Bukkit.getItemFactory.getItemMeta(Material.GOLDEN_APPLE)
      .tap(_.setDisplayName(s"$GOLD${BOLD}ゾんご"))
      .tap(_.setLore(getZongoLore))

    val itemStack = new ItemStack(Material.GOLDEN_APPLE, 1)
    itemStack.setItemMeta(itemMeta)
    itemStack
  }

  private def getZongoLore: util.List[String] = List(
    "",
    s"$RESET${GRAY}成ゾン式で暴走していたチャラゾンビから没収した。",
    s"$RESET${GRAY}ゾンビたちが栽培しているりんご。",
    s"$RESET${GRAY}良質な腐葉土で1つずつ大切に育てられた。",
    s"$RESET${GRAY}栄養豊富で、食べるとマナが10%回復する。",
    s"$RESET${GRAY}腐りやすいため賞味期限を超えると効果が無くなる。",
    "",
    s"$RESET${DARK_GREEN}賞味期限：$FINISHDISP",
    s"$RESET${AQUA}マナ回復（10％）$GRAY （期限内）"
  ).asJava
}
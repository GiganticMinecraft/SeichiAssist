package com.github.unchama.seasonalevents.seizonsiki

import com.github.unchama.seasonalevents.seizonsiki.Seizonsiki.FINISHDISP
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack
import org.bukkit.{Bukkit, Material}

import scala.jdk.CollectionConverters._
import scala.util.chaining._

object SeizonsikiItemData {
  // TODO NBT化
  val seizonsikiZongo: ItemStack = {
    val loreList = List(
      "",
      s"${GRAY}成ゾン式で暴走していたチャラゾンビから没収した。",
      s"${GRAY}ゾンビたちが栽培しているりんご。",
      s"${GRAY}良質な腐葉土で1つずつ大切に育てられた。",
      s"${GRAY}栄養豊富で、食べるとマナが10%回復する。",
      s"${GRAY}腐りやすいため賞味期限を超えると効果が無くなる。",
      "",
      s"${DARK_GREEN}賞味期限：$FINISHDISP",
      s"${AQUA}マナ回復（10％）$GRAY （期限内）"
    ).map(str => s"$RESET$str")
      .asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.GOLDEN_APPLE)
      .tap(_.setDisplayName(s"$GOLD${BOLD}ゾんご"))
      .tap(_.setLore(loreList))

    val itemStack = new ItemStack(Material.GOLDEN_APPLE, 1)
    itemStack.setItemMeta(itemMeta)
    itemStack
  }

  // アイテムがゾンごかどうかの判定
  def isZongoConsumed(item: ItemStack): Boolean = {
    // Lore取得
//    if (!item.hasItemMeta || !item.getItemMeta.hasLore) return false
//    val itemLore = item.getItemMeta.getLore
//    val prizeLore = getZongoLore
//    // 比較
//    itemLore.containsAll(prizeLore)
    false
  }
}
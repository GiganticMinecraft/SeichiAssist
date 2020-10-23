package com.github.unchama.seasonalevents.seizonsiki

import java.text.SimpleDateFormat
import java.util.Date

import com.github.unchama.seasonalevents.seizonsiki.Seizonsiki.{FINISH, FINISHDISP}
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack
import org.bukkit.{Bukkit, Material}

import scala.jdk.CollectionConverters._
import scala.util.chaining._

object SeizonsikiItemData {
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

    // FIXME finishDateがObjectにいったら
    val n: Date = new SimpleDateFormat("yyyy-MM-dd").parse(FINISH)
    new NBTItem(itemStack)
      .tap(_.setByte(NBTTagConstants.typeIdTag, 1.toByte))
      .tap(_.setObject(NBTTagConstants.expirationDateTag, n))
      .pipe(_.getItem)
  }

  def isZongo(item: ItemStack): Boolean = {
    item != null && item.getType != Material.AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) != 0
    }
  }

  def isValidZongo(item: ItemStack): Boolean = {
    val now = new Date()
    // TODO 時刻は比較しない
    new NBTItem(item).getObject(NBTTagConstants.expirationDateTag, classOf[Date]).after(now)
  }

  private object NBTTagConstants {
    val typeIdTag = "seizonsikiZongoTypeId"
    val expirationDateTag = "seizonsikiZongoExpirationDate"
  }
}
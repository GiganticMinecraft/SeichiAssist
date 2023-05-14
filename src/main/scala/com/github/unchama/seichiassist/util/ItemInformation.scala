package com.github.unchama.seichiassist.util

import org.bukkit.ChatColor.GREEN
import org.bukkit.block.{Block, Skull}
import org.bukkit.{Material, SkullType}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

import java.util.stream.IntStream

object ItemInformation {

  import scala.jdk.CollectionConverters._
  import scala.util.chaining._

  def isGachaTicket(itemStack: ItemStack): Boolean = {
    val containsRightClickMessage: String => Boolean = _.contains(s"${GREEN}右クリックで使えます")

    if (itemStack.getType != Material.PLAYER_HEAD) return false

    val skullMeta = itemStack.getItemMeta.asInstanceOf[SkullMeta]

    if (!(skullMeta.hasOwner && skullMeta.getOwningPlayer.getName == "unchama")) return false

    skullMeta.hasLore && skullMeta.getLore.asScala.exists(containsRightClickMessage)
  }

  def isMineHeadItem(itemstack: ItemStack): Boolean = {
    itemstack.getType == Material.CARROT_ON_A_STICK &&
    loreIndexOf(itemstack.getItemMeta.getLore.asScala.toList, "頭を狩り取る形をしている...") >= 0
  }

  def getSkullDataFromBlock(block: Block): Option[ItemStack] = {
    if (block.getType != Material.PLAYER_HEAD) return None

    val skull = block.getState.asInstanceOf[Skull]
    val itemStack = new ItemStack(Material.PLAYER_HEAD)

    // SkullTypeがプレイヤー以外の場合，SkullTypeだけ設定して終わり
    if (skull.getSkullType != SkullType.PLAYER) {
      val durability = skull.getSkullType match {
        case SkullType.CREEPER  => SkullType.CREEPER.ordinal.toShort
        case SkullType.DRAGON   => SkullType.DRAGON.ordinal.toShort
        case SkullType.SKELETON => SkullType.SKELETON.ordinal.toShort
        case SkullType.WITHER   => SkullType.WITHER.ordinal.toShort
        case SkullType.ZOMBIE   => SkullType.ZOMBIE.ordinal.toShort
        case _                  => itemStack.getDurability
      }
      return Some(itemStack.tap(_.setDurability(durability)))
    }
    // プレイヤーの頭の場合，ドロップアイテムからItemStackを取得．データ値をPLAYERにして返す
    Some(block.getDrops.asScala.head.tap(_.setDurability(SkullType.PLAYER.ordinal.toShort)))
  }

  /**
   * loreを捜査して、要素の中に`find`が含まれているかを調べる。
   *
   * @param lore
   *   探される対象
   * @param find
   *   探す文字列
   * @return
   *   見つかった場合はその添字、見つからなかった場合は-1
   */
  // TODO 引数がListとStringのみならloreIndexOfというメソッド名はおかしいような？(ListがItemStackのloreとは限らないため)
  def loreIndexOf(lore: List[String], find: String): Int = {
    IntStream.range(0, lore.size).filter { i => lore(i).contains(find) }.findFirst().orElse(-1)
  }
}

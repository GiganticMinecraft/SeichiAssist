package com.github.unchama.seichiassist.util

import com.github.unchama.itemstackbuilder.SkullOwnerUuid
import com.github.unchama.seichiassist.SkullOwners
import org.bukkit.ChatColor.GREEN
import org.bukkit.block.Block
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

import java.util.stream.IntStream

object ItemInformation {

  import scala.jdk.CollectionConverters._

  def isGachaTicket(itemStack: ItemStack): Boolean = {
    val containsRightClickMessage: String => Boolean = _.contains(s"${GREEN}右クリックで使えます")

    if (itemStack.getType != Material.PLAYER_HEAD) return false

    val skullMeta = itemStack.getItemMeta.asInstanceOf[SkullMeta]

    /*
      Note: skullMeta.getOwner == "unchama"という条件は、後方互換性を保つためのコードである。
            1.12.2のバージョンでskullMeta.getOwnerで頭のオーナーを取得できていたが、
            1.18.2ではsetOwner、getOwnerともに使用できない。
            そのため、1.18.2からはPlayerProfileにUUIDを書き込み、UUIDを利用した判定を行うことになった。

            1.18.2の環境で、1.12.2から持ってきたガチャ券(1.12.2の環境でItemStack化されたもの)からgetOwnerすることが
            できなければ該当のコードを削除して良い。
     */
    if (
      !(skullMeta.hasOwner && (SkullOwnerUuid(
        skullMeta.getOwningPlayer.getPlayerProfile.getUniqueId
      ) == SkullOwners.unchama || skullMeta.getOwner == "unchama"))
    ) return false

    skullMeta.hasLore && skullMeta.getLore.asScala.exists(containsRightClickMessage)
  }

  def isMineHeadItem(itemstack: ItemStack): Boolean = {
    itemstack.getType == Material.CARROT_ON_A_STICK &&
    loreIndexOf(itemstack.getItemMeta.getLore.asScala.toList, "頭を狩り取る形をしている...") >= 0
  }

  def getSkullDataFromBlock(block: Block): Option[ItemStack] = {
    val headMaterials = Set(
      Material.PLAYER_WALL_HEAD,
      Material.PLAYER_HEAD,
      Material.DRAGON_HEAD,
      Material.DRAGON_WALL_HEAD,
      Material.ZOMBIE_HEAD,
      Material.ZOMBIE_WALL_HEAD,
      Material.CREEPER_HEAD,
      Material.CREEPER_WALL_HEAD,
      Material.SKELETON_SKULL,
      Material.SKELETON_WALL_SKULL,
      Material.WITHER_SKELETON_SKULL,
      Material.WITHER_SKELETON_WALL_SKULL
    )

    Option.when(headMaterials.contains(block.getType))(block.getDrops.asScala.head)
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

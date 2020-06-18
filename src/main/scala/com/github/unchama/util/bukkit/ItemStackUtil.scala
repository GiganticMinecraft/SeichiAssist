package com.github.unchama.util.bukkit

import org.bukkit.ChatColor.{DARK_GREEN, RESET}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

object ItemStackUtil {
  /**
   * `stacks` に含まれるアイテムスタックをできるだけマージしたような新たな `Seq` を返す
   */
  def amalgamate(stacks: Seq[ItemStack]): Seq[ItemStack] = {
    val originals = stacks.map(_.clone())

    val result = scala.collection.mutable.ArrayBuffer.empty[ItemStack]

    originals.foreach { original =>
      result.foreach { r =>
        if (r.isSimilar(original)) {
          val mergeAmount = original.getAmount.min(r.getMaxStackSize - r.getAmount)

          r.setAmount(r.getAmount + mergeAmount)
          original.setAmount(original.getAmount - mergeAmount)
        }
      }
      if (original.getAmount != 0) {
        result.addOne(original)
      }
    }

    result.toSeq
  }

  /**
   * メタが `f` によって変更されたような新たな `ItemStack` を作成する
   */
  def modifyMeta(f: ItemMeta => Unit)(stack: ItemStack): ItemStack = {
    val itemMeta = stack.getItemMeta
    val newItem = stack.clone()
    f(itemMeta)
    newItem.setItemMeta(itemMeta)
    newItem
  }

  /**
   * `owner` のloreが追加されたような新たな `ItemStack` を作成する
   */
  def appendOwnerInformation(owner: Player)(itemStack: ItemStack): ItemStack = {
    import scala.jdk.CollectionConverters._

    modifyMeta { m => import m._
      setLore {
        val originalLore = if (itemStack.getItemMeta.hasLore) getLore.asScala else Nil
        val appended = originalLore ++ List(s"$RESET${DARK_GREEN}所有者：${owner.getName}")

        appended.asJava
      }
    }(itemStack)
  }
}

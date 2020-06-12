package com.github.unchama.seichiassist.util

import org.bukkit.inventory.ItemStack

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
}

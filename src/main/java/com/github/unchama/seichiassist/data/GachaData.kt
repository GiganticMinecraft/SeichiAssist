package com.github.unchama.seichiassist.data

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack
import java.util.*

class GachaData(itemStack: ItemStack, var probability: Double) {
  //アイテムデータ格納
  var itemStack: ItemStack = itemStack.clone()

  @Deprecated("", ReplaceWith("use itemStack.amount"))
  val itemStackAmount: Int
    get() = this.itemStack.amount

  fun compare(m: ItemStack, name: String): Boolean {
    val mlore: List<String> = m.itemMeta.lore
    val lore: List<String> = this.itemStack.itemMeta.lore

    if (mlore.containsAll(lore) && this.itemStack.itemMeta.displayName == m.itemMeta.displayName) {
      val index = Util.loreIndexOf(mlore, "所有者")

      return if (index >= 0) {
        //保有者であれば交換
        //保有者でなければ交換できない
        mlore[index].toLowerCase().contains(name)
      } else {
        //所有者の記載がなければ交換できる。
        true
      }
    }
    return false
  }

  fun addname(name: String) {
    val meta = this.itemStack.itemMeta
    val lore: MutableList<String>
    if (meta.hasLore()) {
      lore = meta.lore
    } else {
      lore = ArrayList()
    }
    lore.add(ChatColor.RESET.toString() + "" + ChatColor.DARK_GREEN + "所有者：" + name)
    meta.lore = lore
    this.itemStack.itemMeta = meta
  }

  fun copy(): GachaData = GachaData(this.itemStack.clone(), probability)

  companion object {
    // TODO ここにあるべきではない
    fun runGacha(): GachaData {
      var sum = 1.0
      val rand = Math.random()

      for (gachadata in SeichiAssist.gachadatalist) {
        sum -= gachadata.probability
        if (sum <= rand) {
          return gachadata.copy()
        }
      }
      return GachaData(StaticGachaPrizeFactory.getGachaRingo(), 1.0)
    }
  }
}

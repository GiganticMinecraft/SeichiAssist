package com.github.unchama.seichiassist.data

import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack

class GachaPrize(itemStack: ItemStack, var probability: Double) {
  //アイテムデータ格納
  var itemStack: ItemStack = itemStack.clone()

  @Deprecated("", ReplaceWith("use itemStack.amount"))
  val itemStackAmount: Int
    get() = this.itemStack.amount

  @Deprecated("ここをなんのデータクラスだと思っているんだ")
  def compare(m: ItemStack, name: String): Boolean = {
    val mlore: List[String] = m.itemMeta.lore
    val lore: List[String] = this.itemStack.itemMeta.lore

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

  @Deprecated("ここをなんのデータクラスだと思っているんだ")
  def appendOwnerLore(name: String) {
    val meta = this.itemStack.itemMeta
    val lore = if (meta.hasLore()) meta.lore else ArrayList()
    lore.add(s"${RESET}${DARK_GREEN}所有者：$name")
    this.itemStack.itemMeta.lore = lore
  }

  def copy(): GachaPrize = GachaPrize(this.itemStack.clone(), probability)
}

object GachaPrize {
  // TODO ここにあるべきではない
  def runGacha(): GachaPrize = {
    var sum = 1.0
    val rand = Math.random()

    for (gachadata in SeichiAssist.gachadatalist) {
      sum -= gachadata.probability
      if (sum <= rand) {
        return gachadata.copy()
      }
    }
    return GachaPrize(StaticGachaPrizeFactory.gachaRingo(), 1.0)
  }
}

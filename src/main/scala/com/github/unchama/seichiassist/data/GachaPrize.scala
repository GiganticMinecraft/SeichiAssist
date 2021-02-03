package com.github.unchama.seichiassist.data

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.util.{ItemUtil, StaticGachaPrizeFactory, Util}
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack

// TODO イミュータブル化
class GachaPrize(_itemStack: ItemStack, var probability: Double) {
  //アイテムデータ格納
  val itemStack: ItemStack = _itemStack.clone()

  @Deprecated
  val itemStackAmount: Int = this.itemStack.getAmount

  import scala.jdk.CollectionConverters._

  /**
   * @deprecated ここをなんのデータクラスだと思っているんだ
   */
  @Deprecated()
  def canExchange(m: ItemStack, name: String): Boolean = {
    val mlore = m.getItemMeta.getLore.asScala.toList
    val lore = this.itemStack.getItemMeta.getLore.asScala.toList

    //　TODO: UUID
    if (lore.forall(mlore.contains) && this.itemStack.getItemMeta.getDisplayName == m.getItemMeta.getDisplayName) {
      ItemUtil.loreIndexOf(mlore, "所有者").forall(mlore(_).toLowerCase().contains(name))
    } else false
  }

  /**
   * @deprecated ここをなんのデータクラスだと思っているんだ
   */
  @Deprecated()
  def appendOwnerLore(name: String): Unit = {
    val meta = itemStack.getItemMeta
    val lore = if (meta.hasLore) meta.getLore.asScala else Nil
    itemStack.getItemMeta.setLore(lore.:+(s"$RESET${DARK_GREEN}所有者：$name").asJava)
  }

  def copy(): GachaPrize = new GachaPrize(this.itemStack.clone(), probability)
}

object GachaPrize {
  // TODO ここにあるべきではない
  def runGacha(): GachaPrize = {
    var sum = 1.0
    val rand = Math.random()

    for {gachadata <- SeichiAssist.gachadatalist} {
      sum -= gachadata.probability
      if (sum <= rand) {
        return gachadata.copy()
      }
    }

    new GachaPrize(StaticGachaPrizeFactory.getGachaRingo, 1.0)
  }
}

package com.github.unchama.seichiassist.data

import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.block.Banner
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta

class MineStackGachaData(val objName: String,
                         _itemStack: ItemStack,
                         val probability: Double,
                         val level: Int) extends Cloneable {
  val itemStack: ItemStack = _itemStack.clone()

  import scala.jdk.CollectionConverters._

  /**
   * @deprecated use itemStack.amount
   */
  @Deprecated()
  val amount: Int = itemStack.getAmount

  /**
   * @deprecated ここをなんのデータクラスだと思っているんだ
   */
  @Deprecated()
  def itemStackEquals(another: ItemStack): Boolean = {
    val crt = itemStack.getItemMeta
    val ant = another.getItemMeta
    val lore = crt.getLore
    val anotherLore = ant.getLore

    if (anotherLore.containsAll(lore) && (crt.getDisplayName.contains(another.getItemMeta.getDisplayName) || ant.getDisplayName.contains(this.itemStack.getItemMeta.getDisplayName))) {
      //この時点で名前と内容が一致
      //盾、バナー用の模様判定
      val otherType = another.getType
      if ((otherType == Material.SHIELD || otherType == Material.BANNER) && this.itemStack.getType == otherType) {
        val bs0 = ant.asInstanceOf[BlockStateMeta]
        val b0 = bs0.getBlockState.asInstanceOf[Banner]
        val p0 = b0.getPatterns

        val bs1 = crt.asInstanceOf[BlockStateMeta]
        val b1 = bs1.getBlockState.asInstanceOf[Banner]
        val p1 = b1.getPatterns

        return p0.containsAll(p1)
      }
      return true
    }
    false
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

  def copy(): MineStackGachaData = new MineStackGachaData(objName, itemStack.clone(), probability, level)
}

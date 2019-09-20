package com.github.unchama.seichiassist.data

import org.bukkit.inventory.ItemStack

class MineStackGachaData(val objName: String,
                         itemStack: ItemStack,
                         val probability: Double,
                         val level: Int) : Cloneable {
  @Deprecated("", ReplaceWith("use itemStack.amount"))
  val amount
    get() = itemStack.amount

  val itemStack: ItemStack = itemStack.clone()

  @Deprecated("ここをなんのデータクラスだと思っているんだ")
  def itemStackEquals(another: ItemStack): Boolean {
    val crt = itemStack.itemMeta
    val ant = another.itemMeta
    val lore = crt.lore
    val anotherLore = ant.lore

    if (anotherLore.containsAll(lore) && (crt.displayName.contains(another.itemMeta.displayName) || ant.displayName.contains(this.itemStack.itemMeta.displayName))) {
      //この時点で名前と内容が一致
      //盾、バナー用の模様判定
      val otherType = another.type
      if ((otherType === Material.SHIELD || otherType === Material.BANNER) && this.itemStack.type === otherType) {
        val bs0 = ant as BlockStateMeta
        val b0 = bs0.blockState as Banner
        val p0 = b0.patterns

        val bs1 = crt as BlockStateMeta
        val b1 = bs1.blockState as Banner
        val p1 = b1.patterns

        return p0.containsAll(p1)
      }
      return true
    }
    return false
  }

  @Deprecated("ここをなんのデータクラスだと思っているんだ")
  def appendOwnerLore(name: String) {
    val meta = this.itemStack.itemMeta
    val lore = if (meta.hasLore()) meta.lore else ArrayList()
    lore.add(s"${ChatColor.RESET}${ChatColor.DARK_GREEN}所有者：$name")
    this.itemStack.itemMeta.lore = lore
  }

  def copy() = MineStackGachaData(objName, itemStack.clone(), probability, level)
}

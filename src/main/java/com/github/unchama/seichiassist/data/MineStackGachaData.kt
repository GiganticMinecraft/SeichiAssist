package com.github.unchama.seichiassist.data

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.block.Banner
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import java.util.*

class MineStackGachaData(val objName: String,
                         itemStack: ItemStack,
                         val probability: Double,
                         val level: Int) : Cloneable {
  @Deprecated("", ReplaceWith("use itemStack.amount"))
  val amount
    get() = itemStack.amount

  val itemStack: ItemStack = itemStack.clone()

  @Deprecated("ここをなんのデータクラスだと思っているんだ")
  fun itemStackEquals(another: ItemStack): Boolean {
    val lore = this.itemStack.itemMeta.lore
    val anotherLore = another.itemMeta.lore

    if (anotherLore.containsAll(lore) && (this.itemStack.itemMeta.displayName.contains(another.itemMeta.displayName) || another.itemMeta.displayName.contains(this.itemStack.itemMeta.displayName))) {
      //この時点で名前と内容が一致
      //盾、バナー用の模様判定
      if ((another.type == Material.SHIELD || another.type == Material.BANNER) && this.itemStack.type == another.type) {
        val bs0 = another.itemMeta as BlockStateMeta
        val b0 = bs0.blockState as Banner
        val p0 = b0.patterns

        val bs1 = this.itemStack.itemMeta as BlockStateMeta
        val b1 = bs1.blockState as Banner
        val p1 = b1.patterns

        return p0.containsAll(p1)
      }
      return true
    }
    return false
  }

  @Deprecated("ここをなんのデータクラスだと思っているんだ")
  fun appendOwnerLore(name: String) {
    val meta = this.itemStack.itemMeta
    val lore = if (meta.hasLore()) meta.lore else ArrayList()
    lore.add("${ChatColor.RESET}${ChatColor.DARK_GREEN}所有者：$name")
    this.itemStack.itemMeta.lore = lore
  }

  fun copy() = MineStackGachaData(objName, itemStack.clone(), probability, level)
}

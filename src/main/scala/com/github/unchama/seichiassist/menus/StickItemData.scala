package com.github.unchama.seichiassist.menus

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

import scala.util.chaining.scalaUtilChainingOps

object StickItemData {

  val stick: ItemStack = new ItemStack(Material.STICK, 1).tap { itemStack =>
    import itemStack._
    val meta = getItemMeta
    meta.setDisplayName("棒メニューが開ける棒")
    setItemMeta(meta)
  }

}

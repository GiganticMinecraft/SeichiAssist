package com.github.unchama.seichiassist.subsystems.gachaprize.bukkit

import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{
  CanBeSignedAsGachaPrize,
  GachaPrize
}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.ChatColor._

object BukkitItemStackCanBeSignedAsGachaPrize extends CanBeSignedAsGachaPrize[ItemStack] {
  override def signWith(ownerName: String): GachaPrize[ItemStack] => ItemStack = { gachaPrize =>
    appendOwnerInformation(ownerName)(gachaPrize.itemStack.clone())
  }

  /**
   * メタが `f` によって変更されたような新たな `ItemStack` を作成する
   */
  private def modifyMeta(f: ItemMeta => Unit)(stack: ItemStack): ItemStack = {
    val itemMeta = stack.getItemMeta
    val newItem = stack.clone()
    f(itemMeta)
    newItem.setItemMeta(itemMeta)
    newItem
  }

  /**
   * `owner` のloreが追加されたような新たな `ItemStack` を作成する
   */
  private def appendOwnerInformation(ownerName: String)(itemStack: ItemStack): ItemStack = {
    import scala.jdk.CollectionConverters._

    modifyMeta { m =>
      import m._
      setLore {
        val originalLore = if (itemStack.getItemMeta.hasLore) getLore.asScala else Nil
        val appended = originalLore :+ s"$RESET${DARK_GREEN}所有者：$ownerName"

        appended.asJava
      }
    }(itemStack)
  }
}

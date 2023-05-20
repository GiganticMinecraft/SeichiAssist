package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import com.github.unchama.itemmigration.bukkit.util.MigrationHelper
import com.github.unchama.itemmigration.domain.{ItemMigration, ItemMigrationVersionNumber}
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack

/**
 * 4周年記念アイテムの名前がAniv.というtypoを含んでいたため修正するマイグレーション。
 */
object V1_2_0_FixTypoOf4thAnniversaryGT {

  private val gt4thName = s"$WHITE$BOLD${ITALIC}4thAniv."

  def is4thGiganticItem(itemStack: ItemStack): Boolean = {
    if (itemStack == null || !itemStack.hasItemMeta || !itemStack.getItemMeta.hasDisplayName)
      return false
    val name = itemStack.getItemMeta.getDisplayName
    name.contains(gt4thName)
  }

  import eu.timepit.refined.auto._

  def migrationFunction(itemStack: ItemStack): ItemStack = {
    if (!is4thGiganticItem(itemStack)) return itemStack

    import scala.util.chaining._

    val clone = itemStack.clone()
    val meta = clone.getItemMeta
    meta.setDisplayName(meta.getDisplayName.replace("Aniv", "Anniv"))
    clone.tap(_.setItemMeta(meta))
  }

  def migration: ItemMigration = ItemMigration(
    ItemMigrationVersionNumber(1, 2, 0),
    MigrationHelper.delegateConversionForContainers(migrationFunction)
  )
}

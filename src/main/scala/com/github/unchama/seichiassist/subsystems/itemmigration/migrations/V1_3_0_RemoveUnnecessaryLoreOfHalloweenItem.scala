package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import com.github.unchama.itemmigration.bukkit.util.MigrationHelper
import com.github.unchama.itemmigration.domain.{ItemMigration, ItemMigrationVersionNumber}
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack

import scala.jdk.CollectionConverters._

object V1_3_0_RemoveUnnecessaryLoreOfHalloweenItem {

  private val halloweenClearPrizeLore = s"${GRAY}2020ハロウィン討伐イベントクリア賞"
  private val halloweenSpecialPrizeLore = s"${GRAY}2020ハロウィン討伐イベント特別賞"

  def isHalloweenPrize(itemStack: ItemStack): Boolean = {
    if (itemStack == null || !itemStack.hasItemMeta || !itemStack.getItemMeta.hasLore) return false
    val lore = itemStack.getItemMeta.getLore.asScala
    lore.contains(halloweenClearPrizeLore) || lore.contains(halloweenSpecialPrizeLore)
  }

  import eu.timepit.refined.auto._

  def migrationFunction(itemStack: ItemStack): ItemStack = {
    if (!isHalloweenPrize(itemStack)) return itemStack

    import scala.util.chaining._

    val clone = itemStack.clone()
    val meta = clone.getItemMeta.tap {meta =>
      import meta._
      setLore {
        if (isUnbreakable) getLore.asScala.filter(_ != s"$RESET${DARK_RED}耐久無限").asJava
        else getLore
      }
    }
    clone.tap(_.setItemMeta(meta))
  }

  def migration: ItemMigration = ItemMigration(
    ItemMigrationVersionNumber(1, 3, 0),
    MigrationHelper.delegateConversionForContainers(migrationFunction)
  )
}

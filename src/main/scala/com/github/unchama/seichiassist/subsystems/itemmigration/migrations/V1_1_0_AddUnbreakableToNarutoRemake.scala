package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import com.github.unchama.itemmigration.domain.{ItemMigration, ItemMigrationVersionNumber}
import com.github.unchama.itemmigration.util.MigrationHelper
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack

import scala.jdk.CollectionConverters._

object V1_1_0_AddUnbreakableToNarutoRemake {

  private val narutoRemake1Lore = s"${GRAY}2020ハロウィン討伐イベントクリア賞"
  private val narutoRemake2Lore = s"${GRAY}2020ハロウィン討伐イベント特別賞"

  def isNarutoRemake(itemStack: ItemStack): Boolean = {
    if (itemStack == null || !itemStack.hasItemMeta || !itemStack.getItemMeta.hasLore) return false
    val lore = itemStack.getItemMeta.getLore.asScala
    lore.contains(narutoRemake1Lore) || lore.contains(narutoRemake2Lore)
  }

  import eu.timepit.refined.auto._

  def migrationFunction(itemStack: ItemStack): ItemStack = {
    if (!isNarutoRemake(itemStack)) return itemStack

    import scala.util.chaining._

    itemStack.clone().tap { clone =>
      import clone._
      setDurability(238.toShort)
      setItemMeta {
        getItemMeta.tap { itemMeta =>
          import itemMeta._
          setUnbreakable(true)
          setLore(getLore.asScala.append(s"$RESET${DARK_RED}耐久無限").asJava)
        }
      }
    }
  }

  def migration: ItemMigration = ItemMigration(
    ItemMigrationVersionNumber(1, 1, 0),
    MigrationHelper.delegateConversionForContainers(migrationFunction)
  )
}
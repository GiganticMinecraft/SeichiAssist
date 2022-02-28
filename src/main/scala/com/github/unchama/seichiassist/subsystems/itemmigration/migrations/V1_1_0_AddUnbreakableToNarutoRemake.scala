package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import com.github.unchama.itemmigration.bukkit.util.MigrationHelper
import com.github.unchama.itemmigration.domain.{ItemMigration, ItemMigrationVersionNumber}
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack

import scala.jdk.CollectionConverters._

/*
 * NARUTO REMAKE I/IIの耐久無限化を目的としたマイグレーション。
 *
 * https://github.com/GiganticMinecraft/SeichiAssist/issues/788
 * https://red.minecraftserver.jp/issues/8713
 *
 * を参照されたい。
 */
object V1_1_0_AddUnbreakableToNarutoRemake {

  private val narutoRemake1Lore = s"${GRAY}2020ハロウィン討伐イベントクリア賞"
  private val narutoRemake2Lore = s"${GRAY}2020ハロウィン討伐イベント特別賞"

  def isNarutoRemake(itemStack: ItemStack): Boolean = {
    if (itemStack == null || !itemStack.hasItemMeta || !itemStack.getItemMeta.hasLore)
      return false
    val lore = itemStack.getItemMeta.getLore.asScala
    lore.contains(narutoRemake1Lore) || lore.contains(narutoRemake2Lore)
  }

  import eu.timepit.refined.auto._

  def migrationFunction(itemStack: ItemStack): ItemStack = {
    if (!isNarutoRemake(itemStack)) return itemStack

    import scala.util.chaining._

    itemStack.clone().tap { clone =>
      import clone._
      setDurability(0.toShort)
      setItemMeta {
        getItemMeta.tap { itemMeta =>
          import itemMeta._

          // 冪等性のため、不可壊がすでについているケースを除外する
          if (!isUnbreakable) {
            setUnbreakable(true)
            setLore(getLore.asScala.append(s"$RESET${DARK_RED}耐久無限").asJava)
          }
        }
      }
    }
  }

  def migration: ItemMigration = ItemMigration(
    ItemMigrationVersionNumber(1, 1, 0),
    MigrationHelper.delegateConversionForContainers(migrationFunction)
  )
}

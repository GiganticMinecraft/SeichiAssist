package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import com.github.unchama.itemmigration.domain.{ItemMigration, ItemMigrationVersionNumber}
import com.github.unchama.itemmigration.util.MigrationHelper
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.inventory.ItemStack

import scala.jdk.CollectionConverters._

object V1_1_0_MigrateNarutoRemakeToNewCodec {

  object OldNarutoRemakeItemStackCodec {
    private val narutoRemake1Lore = List(s"${GRAY}2020ハロウィン討伐イベントクリア賞")
    private val narutoRemake2Lore = List(s"${GRAY}2020ハロウィン討伐イベント特別賞")

    def decodeOldNarutoRemakeProperty(itemStack: ItemStack): Option[OldNarutoRemakeRawProperty] =
      if (isNarutoRemake1(itemStack)) Some(OldNarutoRemakeRawProperty(1))
      else if (isNarutoRemake2(itemStack)) Some(OldNarutoRemakeRawProperty(2))
      else None

    def isNarutoRemake1(itemStack: ItemStack): Boolean = {
      if (itemStack == null || !itemStack.hasItemMeta || !itemStack.getItemMeta.hasLore) return false
      val lore = itemStack.getItemMeta.getLore.asScala
      narutoRemake1Lore.forall(lore.contains)
    }

    def isNarutoRemake2(itemStack: ItemStack): Boolean = {
      if (itemStack == null || !itemStack.hasItemMeta || !itemStack.getItemMeta.hasLore) return false
      val lore = itemStack.getItemMeta.getLore.asScala
      narutoRemake2Lore.forall(lore.contains)
    }

    case class OldNarutoRemakeRawProperty(itemType: Int)

  }

  import eu.timepit.refined.auto._

  def migrationFunction(itemStack: ItemStack): ItemStack = {
    val property =
      OldNarutoRemakeItemStackCodec
        .decodeOldNarutoRemakeProperty(itemStack)
        .getOrElse(return itemStack)

    import scala.util.chaining._

    val clone = itemStack.clone().tap { clone =>
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

    new NBTItem(clone).tap { nbtItem =>

      nbtItem.setByte("narutoRemakeTypeId", property.itemType.toByte)
    }.pipe(_.getItem)
  }

  def migration: ItemMigration = ItemMigration(
    ItemMigrationVersionNumber(1, 1, 0),
    MigrationHelper.delegateConversionForContainers(migrationFunction)
  )
}
package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import io.github.iltotore.iron.autoRefine

import com.github.unchama.itemmigration.bukkit.util.MigrationHelper
import com.github.unchama.itemmigration.domain.{ItemMigration, ItemMigrationVersionNumber}
import de.tr7zw.nbtapi.NBTItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

import scala.jdk.CollectionConverters._

/**
 * ギガンティック・ギフト券の空白のLoreを1.18形式に変換する。
 */
object V1_5_0_MigrateGiganticGiftTicketLore {

  private val giftTicketName =
    """{"extra":[{"bold":true,"italic":true,"underlined":false,"strikethrough":false,"obfuscated":false,"color":"green","text":"ギガンティック・ギフト券"}],"text":""}"""

  private val descriptionLore = Vector(
    """{"extra":[{"bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"color":"gray","text":"公共施設鯖にある"}],"text":""}""",
    """{"extra":[{"bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"color":"gray","text":"デパートで買い物ができます"}],"text":""}"""
  )

  private val oldBlankLore = """{"text":" "}"""
  private val newBlankLore = """{"extra":[{"text":" "}],"text":""}"""
  private val knownBlankLore = Set(oldBlankLore, newBlankLore)

  private[migrations] def migratedLore(
    name: String,
    lore: Vector[String]
  ): Option[Vector[String]] = {
    val isGiftTicket =
      name == giftTicketName &&
        lore.size == 4 &&
        lore.slice(1, 3) == descriptionLore &&
        knownBlankLore.contains(lore.head) &&
        knownBlankLore.contains(lore.last)

    if (isGiftTicket && (lore.head == oldBlankLore || lore.last == oldBlankLore)) {
      Some(lore.updated(0, newBlankLore).updated(3, newBlankLore))
    } else None
  }

  def migrationFunction(itemStack: ItemStack): ItemStack =
    Option(itemStack)
      .filter(item => item.getType != Material.AIR && item.getAmount > 0)
      .flatMap { item =>
        val nbtItem = new NBTItem(item)

        for {
          display <- Option(nbtItem.getCompound("display"))
          lore = display.getStringList("Lore")
          originalLore = lore.asScala.toVector
          updatedLore <- migratedLore(display.getString("Name"), originalLore)
        } yield {
          Vector(0, 3).foreach { index =>
            if (originalLore(index) != updatedLore(index)) {
              nbtItem
                .getCompound("display")
                .getStringList("Lore")
                .set(index, updatedLore(index))
            }
          }
          nbtItem.getItem
        }
      }
      .getOrElse(itemStack)

  def migration: ItemMigration = ItemMigration(
    ItemMigrationVersionNumber(1, 5, 0),
    MigrationHelper.delegateConversionForContainers(migrationFunction)
  )
}

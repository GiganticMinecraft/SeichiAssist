package com.github.unchama.seichiassist.subsystems.itemmigration.migrations

import com.github.unchama.itemmigration.bukkit.util.MigrationHelper
import com.github.unchama.itemmigration.domain.{ItemMigration, ItemMigrationVersionNumber}
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack

import scala.jdk.CollectionConverters._

/**
 * 通常ガチャや限定ガチャで排出される各ARTEMISは、「矢を発射すると花火が確率で上がる」という機能を持っていたが、
 * 当該機能は削除されたため、アイテム説明文からその旨を削除する
 */
object V1_5_0_RemoveUnnecessaryLoreOfArtemis {

  private val removedLore = s"${ChatColor.AQUA}打つと花火が上がります"

  def isArtemis(itemStack: ItemStack): Boolean = {
    if (itemStack == null || !itemStack.hasItemMeta || !itemStack.getItemMeta.hasLore)
      return false

    itemStack.getItemMeta.getLore.asScala.contains(removedLore)
  }

  import eu.timepit.refined.auto._

  def migrationFunction(itemStack: ItemStack): ItemStack = {
    if (!isArtemis(itemStack)) return itemStack

    import scala.util.chaining._

    val clone = itemStack.clone()
    val meta = clone.getItemMeta.tap { meta =>
      import meta._
      setLore {
        if (isUnbreakable) getLore.asScala.filter(_ != removedLore).asJava
        else getLore
      }
    }
    clone.tap(_.setItemMeta(meta))
  }

  def migration: ItemMigration = ItemMigration(
    ItemMigrationVersionNumber(1, 5, 0),
    MigrationHelper.delegateConversionForContainers(migrationFunction)
  )
}

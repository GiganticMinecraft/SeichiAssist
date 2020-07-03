package com.github.unchama.itemmigration

import com.github.unchama.itemmigration.ItemMigration.{ItemConversion, VersionNumber}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative
import org.bukkit.inventory.ItemStack

case class ItemMigration(version: VersionNumber, conversion: ItemConversion)

object ItemMigration {
  type VersionNumber = IndexedSeq[Int Refined NonNegative]

  type MigrationSequence = IndexedSeq[ItemMigration]

  type ItemConversion = ItemStack => ItemStack

  /**
   * 先頭から適用されるべきマイグレーションの列を単一の関数へと変換する
   */
  def toSingleFunction(sequence: MigrationSequence): ItemConversion =
    sequence.map(_.conversion).reduce((c1, c2) => c1.andThen(c2))
}

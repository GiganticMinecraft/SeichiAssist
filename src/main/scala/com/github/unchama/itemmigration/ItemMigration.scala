package com.github.unchama.itemmigration

import com.github.unchama.itemmigration.ItemMigration.{ItemStackConversion, VersionNumber}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.NonNegative
import org.bukkit.inventory.ItemStack

case class ItemMigration(version: VersionNumber, conversion: ItemStackConversion)

object ItemMigration {
  type VersionComponent = Int Refined NonNegative

  type VersionNumber = IndexedSeq[VersionComponent]

  type MigrationSequence = IndexedSeq[ItemMigration]

  type ItemStackConversion = ItemStack => ItemStack

  /**
   * 先頭から適用されるべきマイグレーションの列を単一の関数へと変換する
   */
  def toSingleFunction(sequence: MigrationSequence): ItemStackConversion =
    sequence.map(_.conversion).reduce((c1, c2) => c1.andThen(c2))
}

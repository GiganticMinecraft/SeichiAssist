package com.github.unchama.itemmigration

import com.github.unchama.itemmigration.ItemMigration.VersionNumber
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive
import org.bukkit.inventory.ItemStack

case class ItemMigration(version: VersionNumber, conversion: ItemStack => ItemStack)

object ItemMigration {
  type VersionNumber = IndexedSeq[Int Refined Positive]

  type MigrationSequence = IndexedSeq[ItemMigration]

  /**
   * 先頭から適用されるべきマイグレーションの列を単一の関数へと変換する
   */
  def toSingleFunction(sequence: MigrationSequence): ItemStack => ItemStack =
    sequence.map(_.conversion).reduce((c1, c2) => c1.andThen(c2))
}

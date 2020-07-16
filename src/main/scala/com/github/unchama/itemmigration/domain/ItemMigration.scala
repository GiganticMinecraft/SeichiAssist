package com.github.unchama.itemmigration.domain

case class ItemMigration(version: VersionNumber, conversion: ItemStackConversion)

object ItemMigration {

  /**
   * 先頭から適用されるべきマイグレーションの列を単一の関数へと変換する
   */
  def toSingleFunction(sequence: MigrationSequence): ItemStackConversion =
    sequence.map(_.conversion).reduce((c1, c2) => c1.andThen(c2))

}

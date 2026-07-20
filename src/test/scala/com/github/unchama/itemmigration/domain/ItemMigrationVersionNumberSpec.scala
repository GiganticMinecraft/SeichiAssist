package com.github.unchama.itemmigration.domain

import org.scalatest.wordspec.AnyWordSpec

/**
 * [[ItemMigrationVersionNumber]] と [[ItemMigrations]] のバージョン順序の回帰テスト。
 *
 * `ItemMigrationVersionNumber(1, 0, 0)` のようなリテラル構築は refined.auto._ の
 * コンパイル時検証に依存しており、Scala 3移行でinlineファクトリへ置き換える方針の
 * ため、文字列変換・パース・ソート順の現行挙動をここで固定する。
 */
class ItemMigrationVersionNumberSpec extends AnyWordSpec {

  "ItemMigrationVersionNumber" should {
    "versionStringでドット区切りの文字列になる" in {
      assert(ItemMigrationVersionNumber(1, 0, 0).versionString == "1.0.0")
      assert(ItemMigrationVersionNumber(0).versionString == "0")
      assert(ItemMigrationVersionNumber(1, 10, 3).versionString == "1.10.3")
    }

    "fromStringで非負整数のドット区切りをパースできる" in {
      assert(
        ItemMigrationVersionNumber
          .fromString("1.0.0")
          .contains(ItemMigrationVersionNumber(1, 0, 0))
      )
      assert(ItemMigrationVersionNumber.fromString("0").contains(ItemMigrationVersionNumber(0)))
    }

    "fromStringとversionStringは往復可能である" in {
      assert(
        ItemMigrationVersionNumber.fromString("1.4.0").map(_.versionString).contains("1.4.0")
      )
    }

    "fromStringは不正な入力に対してNoneを返す" in {
      assert(ItemMigrationVersionNumber.fromString("1.-1.0").isEmpty)
      assert(ItemMigrationVersionNumber.fromString("a.b.c").isEmpty)
      assert(ItemMigrationVersionNumber.fromString("").isEmpty)
      assert(ItemMigrationVersionNumber.fromString("1..0").isEmpty)
    }
  }

  "ItemMigrations.sorted" should {
    "バージョン番号を数値として辞書式にソートする" in {
      def migrationOf(version: ItemMigrationVersionNumber): ItemMigration =
        ItemMigration(version, identity)

      val v1_0_0 = ItemMigrationVersionNumber(1, 0, 0)
      val v1_2_0 = ItemMigrationVersionNumber(1, 2, 0)
      val v1_10_0 = ItemMigrationVersionNumber(1, 10, 0)
      val v2_0_0 = ItemMigrationVersionNumber(2, 0, 0)

      val sorted =
        ItemMigrations(IndexedSeq(v2_0_0, v1_10_0, v1_0_0, v1_2_0).map(migrationOf)).sorted

      // 文字列ソートでは "1.10.0" < "1.2.0" になるが、数値ソートでは 1.2.0 < 1.10.0 である
      assert(sorted.versions == List(v1_0_0, v1_2_0, v1_10_0, v2_0_0))
    }
  }
}

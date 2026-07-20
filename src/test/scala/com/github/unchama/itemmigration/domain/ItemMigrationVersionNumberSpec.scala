package com.github.unchama.itemmigration.domain

import io.github.iltotore.iron.autoRefine

import org.scalatest.wordspec.AnyWordSpec

/**
 * [[ItemMigrationVersionNumber]] と [[ItemMigrations]] のバージョン順序の回帰テスト。
 *
 * `ItemMigrationVersionNumber(1, 0, 0)` のようなリテラル構築のコンパイル時検証は
 * Ironが担う。文字列変換・パース・ソート順の現行挙動をここで固定する。
 */
class ItemMigrationVersionNumberSpec extends AnyWordSpec {

  "ItemMigrationVersionNumber" should {
    "versionStringでドット区切りの文字列になる" in {
      assert(ItemMigrationVersionNumber(1, 0, 0).versionString == "1.0.0")
      assert(ItemMigrationVersionNumber(0).versionString == "0")
      assert(ItemMigrationVersionNumber(1, 10, 3).versionString == "1.10.3")
    }

    "成分数は3に限らず、任意の個数のリテラルから構築できる" in {
      // Ironのリテラル自動検証と可変長引数により、Scala 2時代のrefined.auto._と
      // 同じ受け入れ範囲（任意個数）が復元されている
      assert(ItemMigrationVersionNumber(1, 0).versionString == "1.0")
      assert(ItemMigrationVersionNumber(1, 2, 3, 4).versionString == "1.2.3.4")
      assert(
        ItemMigrationVersionNumber.fromString("1.0").contains(ItemMigrationVersionNumber(1, 0))
      )
    }

    "負のリテラル・非リテラル・成分なしの構築はコンパイルエラーになる" in {
      assertDoesNotCompile("ItemMigrationVersionNumber(-1, 0)")
      assertDoesNotCompile("val runtimeValue = 1; ItemMigrationVersionNumber(runtimeValue)")
      assertDoesNotCompile("ItemMigrationVersionNumber()")
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

    "リテラル構築とfromString構築の値は、等価性とハッシュ値の両方が一致する" in {
      // 適用済みマイグレーションのスキップ判定（ItemMigrations.yetToBeApplied）は、
      // DBからfromStringで読み戻した値と、マイグレーション定義のリテラル構築値との
      // Set.contains（hashCode + equals）に依存している。この一致が崩れると
      // 適用済みのマイグレーションが再適用されてしまうため、両構築経路の
      // 完全な互換性をここで固定する。
      val literal = ItemMigrationVersionNumber(1, 0, 0)
      val parsed = ItemMigrationVersionNumber.fromString("1.0.0").get

      assert(literal == parsed)
      assert(literal.hashCode == parsed.hashCode)
      assert(Set[ItemMigrationVersionNumber](literal).contains(parsed))
      assert(Set[ItemMigrationVersionNumber](parsed).contains(literal))
    }

    "fromStringで読み戻したバージョンはyetToBeAppliedで適用済みとして扱われる" in {
      def migrationOf(version: ItemMigrationVersionNumber): ItemMigration =
        ItemMigration(version, identity)

      val definedMigrations = ItemMigrations(
        IndexedSeq(
          migrationOf(ItemMigrationVersionNumber(1, 0, 0)),
          migrationOf(ItemMigrationVersionNumber(1, 1, 0))
        )
      )
      // DBから読み戻した状態を再現する
      val appliedVersions: Set[ItemMigrationVersionNumber] =
        Set(ItemMigrationVersionNumber.fromString("1.0.0").get)

      assert(
        definedMigrations.yetToBeApplied(appliedVersions).versions ==
          List(ItemMigrationVersionNumber(1, 1, 0))
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

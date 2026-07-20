package com.github.unchama.seichiassist.menus.paging

import eu.timepit.refined.auto._
import eu.timepit.refined.types.numeric.PosInt
import org.scalatest.wordspec.AnyWordSpec

/**
 * [[PageCounter]] の回帰テスト。
 *
 * `PosInt(...)` リテラルは `RefinedTypeOps.apply` マクロによるコンパイル時検証に
 * 依存しており、Scala 3移行ではinlineファクトリへ置き換える方針のため、
 * ページ数計算の現行挙動をここで固定する。
 */
class PageCounterSpec extends AnyWordSpec {

  "PageCounter.totalPage" should {
    "アイテム数0でも1ページを返す" in {
      assert(PageCounter.totalPage(0, PosInt(26)) == PosInt(1))
    }

    "1ページに収まる場合は1ページを返す" in {
      assert(PageCounter.totalPage(1, PosInt(26)) == PosInt(1))
      assert(PageCounter.totalPage(26, PosInt(26)) == PosInt(1))
    }

    "1ページから溢れた分は切り上げる" in {
      // NicknameShopMenu は26件/ページ、NicknameCombinationMenu は27件/ページで利用している
      assert(PageCounter.totalPage(27, PosInt(26)) == PosInt(2))
      assert(PageCounter.totalPage(52, PosInt(26)) == PosInt(2))
      assert(PageCounter.totalPage(53, PosInt(26)) == PosInt(3))
      assert(PageCounter.totalPage(27, PosInt(27)) == PosInt(1))
      assert(PageCounter.totalPage(28, PosInt(27)) == PosInt(2))
    }

    "1件/ページではアイテム数がそのままページ数になる" in {
      assert(PageCounter.totalPage(5, PosInt(1)) == PosInt(5))
    }
  }
}

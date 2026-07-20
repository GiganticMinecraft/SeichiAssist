package com.github.unchama.seichiassist.menus.paging

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.GreaterEqual
import eu.timepit.refined.refineV
import eu.timepit.refined.types.numeric.PosInt
import org.scalatest.wordspec.AnyWordSpec

/**
 * [[PageCounter]] の回帰テスト。
 *
 * Scala 2時代に`PosInt(...)`マクロが担っていた`itemsPerPage`リテラルのコンパイル時検証は、
 * Scala 3移行でinlineオーバーロードに置き換えられた。ページ数計算の挙動が
 * 移行前と変わっていないことをここで固定する。
 */
class PageCounterSpec extends AnyWordSpec {

  private def items(count: Int): Int Refined GreaterEqual[0] =
    refineV[GreaterEqual[0]].unsafeFrom(count)

  "PageCounter.totalPage" should {
    "アイテム数0でも1ページを返す" in {
      assert(PageCounter.totalPage(items(0), 26) == PosInt.unsafeFrom(1))
    }

    "1ページに収まる場合は1ページを返す" in {
      assert(PageCounter.totalPage(items(1), 26) == PosInt.unsafeFrom(1))
      assert(PageCounter.totalPage(items(26), 26) == PosInt.unsafeFrom(1))
    }

    "1ページから溢れた分は切り上げる" in {
      // NicknameShopMenu は26件/ページ、NicknameCombinationMenu は27件/ページで利用している
      assert(PageCounter.totalPage(items(27), 26) == PosInt.unsafeFrom(2))
      assert(PageCounter.totalPage(items(52), 26) == PosInt.unsafeFrom(2))
      assert(PageCounter.totalPage(items(53), 26) == PosInt.unsafeFrom(3))
      assert(PageCounter.totalPage(items(27), 27) == PosInt.unsafeFrom(1))
      assert(PageCounter.totalPage(items(28), 27) == PosInt.unsafeFrom(2))
    }

    "1件/ページではアイテム数がそのままページ数になる" in {
      assert(PageCounter.totalPage(items(5), 1) == PosInt.unsafeFrom(5))
    }
  }
}

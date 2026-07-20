package com.github.unchama.seichiassist.menus.paging

import io.github.iltotore.iron.autoRefine

import io.github.iltotore.iron.constraint.numeric.GreaterEqual
import io.github.iltotore.iron.{:|, refineUnsafe}
import org.scalatest.wordspec.AnyWordSpec

/**
 * [[PageCounter]] の回帰テスト。
 *
 * `itemsPerPage` リテラルのコンパイル時検証はIronが担う。
 * ページ数計算の挙動が移行前と変わっていないことをここで固定する。
 */
class PageCounterSpec extends AnyWordSpec {

  private def items(count: Int): Int :| GreaterEqual[0] =
    count.refineUnsafe[GreaterEqual[0]]

  "PageCounter.totalPage" should {
    "アイテム数0でも1ページを返す" in {
      assert(PageCounter.totalPage(items(0), 26) == 1)
    }

    "1ページに収まる場合は1ページを返す" in {
      assert(PageCounter.totalPage(items(1), 26) == 1)
      assert(PageCounter.totalPage(items(26), 26) == 1)
    }

    "1ページから溢れた分は切り上げる" in {
      // NicknameShopMenu は26件/ページ、NicknameCombinationMenu は27件/ページで利用している
      assert(PageCounter.totalPage(items(27), 26) == 2)
      assert(PageCounter.totalPage(items(52), 26) == 2)
      assert(PageCounter.totalPage(items(53), 26) == 3)
      assert(PageCounter.totalPage(items(27), 27) == 1)
      assert(PageCounter.totalPage(items(28), 27) == 2)
    }

    "1件/ページではアイテム数がそのままページ数になる" in {
      assert(PageCounter.totalPage(items(5), 1) == 5)
    }
  }
}

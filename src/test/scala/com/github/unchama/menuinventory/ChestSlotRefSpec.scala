package com.github.unchama.menuinventory

import io.github.iltotore.iron.autoRefine

import org.scalatest.wordspec.AnyWordSpec

/**
 * [[ChestSlotRef]] の回帰テスト。
 *
 * `ChestSlotRef(行, 列)` のリテラル呼び出しはリポジトリ内に約300箇所存在する。
 * リテラルのコンパイル時検証はIronが担う。スロットID計算の現行挙動をここで固定する。
 */
class ChestSlotRefSpec extends AnyWordSpec {

  "ChestSlotRef.apply" should {
    "行インデックス×9 + 列インデックスのスロットIDを計算する" in {
      assert(ChestSlotRef(0, 0) == 0)
      assert(ChestSlotRef(0, 4) == 4)
      assert(ChestSlotRef(0, 8) == 8)
      assert(ChestSlotRef(1, 0) == 9)
      assert(ChestSlotRef(1, 8) == 17)
      assert(ChestSlotRef(3, 4) == 31)
      assert(ChestSlotRef(5, 0) == 45)
      assert(ChestSlotRef(5, 8) == 53)
    }

    "6行を超える行インデックスも計算できる（行数の上限は型では制約されない）" in {
      assert(ChestSlotRef(6, 0) == 54)
      assert(ChestSlotRef(10, 8) == 98)
    }
  }
}

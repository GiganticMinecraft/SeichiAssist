package com.github.unchama.seichiassist.subsystems.mana.domain

import com.github.unchama.generic.CachedFunction
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{
  SeichiLevel,
  SeichiLevelTable
}

object ManaAmountCap {

  import cats.implicits._

  /**
   * 整地レベルと、その整地レベルでのマナ上限の対応を与える関数。
   */
  val at: SeichiLevel => ManaAmount = {

    /**
     * 整地レベル `level` を引数に取って、 `level` からその次のレベルに上がった際の マナ上限の増加値を返す関数。
     *
     *   - レベル9未満は増加なし
     *   - レベル9からのレベルアップで初めて100追加され、
     *   - レベル10以降のレベルアップでは、
     *     - n := 通り過ぎた10の倍数のレベルの数
     *     - a := (n - 1) min 10
     *     - x := (2のa乗 - 1) * 2 としたときに 10 + x ずつ追加される。
     *
     * 例えば、 レベル10からレベル19までは10ずつ増加し、 レベル20からレベル29までは12ずつ、 レベル30からレベル39までは16ずつ、
     * レベル40からレベル49までは24ずつ…といった具合に、 レベル110までのレベル10ごとに増加幅が2冪で増える。
     */
    val increaseFrom: SeichiLevel => ManaAmount = CachedFunction { seichiLevel =>
      if (seichiLevel < SeichiLevel.ofPositive(9)) {
        ManaAmount(0)
      } else if (seichiLevel == SeichiLevel.ofPositive(9)) {
        ManaAmount(100)
      } else {
        val n = seichiLevel.level / 10
        val a = (n - 1) min 10
        val x = (scala.math.pow(2, a) - 1) * 2
        ManaAmount(10 + x)
      }
    }

    val levelsToConsider = SeichiLevelTable.table.levelRange.toVector

    // ManaAmount(0) からの累積和を取ったものをテーブルとする
    val table = levelsToConsider.map(increaseFrom).scanLeft(ManaAmount(0))(_.add(_))

    level => table(level.level - 1)
  }

}

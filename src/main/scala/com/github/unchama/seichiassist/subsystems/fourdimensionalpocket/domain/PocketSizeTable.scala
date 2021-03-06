package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel

/**
 * 整地レベルから、その整地レベルにて利用可能な四次元ポケットのサイズを計算するテーブル。
 */
object PocketSizeTable extends (SeichiLevel => PocketSize) {

  import cats.implicits._

  override def apply(level: SeichiLevel): PocketSize = {
    if (level < SeichiLevel.ofPositive(46)) PocketSize(3)
    else if (level < SeichiLevel.ofPositive(56)) PocketSize(4)
    else if (level < SeichiLevel.ofPositive(66)) PocketSize(5)
    else PocketSize(6)
  }

  val default: PocketSize = apply(SeichiLevel.ofPositive(1))

}

package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

/**
 * 整地スターレベル。
 */
case class SeichiStarLevel private(level: Int) extends AnyVal

object SeichiStarLevel {

  def ofNonNegative(n: Int): SeichiStarLevel = {
    require(n >= 0, "整地スターレベルは非負である必要があります。")
    SeichiStarLevel(n)
  }

}

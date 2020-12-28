package com.github.unchama.buildassist.application

import com.github.unchama.buildassist.domain.explevel.BuildExpAmount

trait BuildExpMultiplier {

  /**
   * 整地ワールドでの建築量倍率
   */
  val whenInSeichiWorld: BigDecimal

  /**
   * スキルを使って並べた時の建築量倍率
   */
  val withBuildSkills: BigDecimal

}

trait Configuration {

  val multipliers: BuildExpMultiplier

  /**
   * ブロック設置カウントの1分上限
   */
  val oneMinuteBuildExpLimit: BuildExpAmount

}

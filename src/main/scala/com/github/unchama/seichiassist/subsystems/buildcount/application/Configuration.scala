package com.github.unchama.seichiassist.subsystems.buildcount.application

import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount

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

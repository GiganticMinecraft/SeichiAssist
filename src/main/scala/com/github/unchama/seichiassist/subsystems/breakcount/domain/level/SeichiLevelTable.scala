package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import com.github.unchama.seichiassist.domain.explevel.FiniteExpLevelTable

object SeichiLevelTable {

  import SeichiExpAmount.ofNonNegative

  //@formatter:off
  private val tableUpToLevel51: FiniteExpLevelTable[SeichiLevel, SeichiExpAmount] =
    new FiniteExpLevelTable[SeichiLevel, SeichiExpAmount](
      Vector(
        0,       15,      49,      106,       198,       //5
        333,     705,     1_265,   2_105,     3_347,     //10
        4_589,   5_831,   7_073,   8_315,     9_557,     //15
        11_047,  12_835,  14_980,  17_554,    20_642,    //20
        24_347,  28_793,  34_128,  40_530,    48_212,    //25
        57_430,  68_491,  81_764,  97_691,    116_803,   //30
        135_915, 155_027, 174_139, 193_251,   212_363,   //35
        235_297, 262_817, 295_841, 335_469,   383_022,   //40
        434_379, 489_844, 549_746, 614_440,   684_309,   //45
        759_767, 841_261, 929_274, 1_024_328, 1_126_986, //50
        1_250_000
      ).map(ofNonNegative(_))
    )

  private val linearIncreases: List[(SeichiLevel, SeichiExpAmount)] = List(
    SeichiLevel.ofPositive(60)  -> SeichiExpAmount.ofNonNegative(125_000),
    SeichiLevel.ofPositive(70)  -> SeichiExpAmount.ofNonNegative(175_000),
    SeichiLevel.ofPositive(80)  -> SeichiExpAmount.ofNonNegative(220_000),
    SeichiLevel.ofPositive(90)  -> SeichiExpAmount.ofNonNegative(280_000),
    SeichiLevel.ofPositive(99)  -> SeichiExpAmount.ofNonNegative(360_000),
    SeichiLevel.ofPositive(100) -> SeichiExpAmount.ofNonNegative(800_000),
    SeichiLevel.ofPositive(110) -> SeichiExpAmount.ofNonNegative(450_000),
    SeichiLevel.ofPositive(120) -> SeichiExpAmount.ofNonNegative(490_000),
    SeichiLevel.ofPositive(130) -> SeichiExpAmount.ofNonNegative(540_000),
    SeichiLevel.ofPositive(140) -> SeichiExpAmount.ofNonNegative(590_000),
    SeichiLevel.ofPositive(150) -> SeichiExpAmount.ofNonNegative(660_000),
    SeichiLevel.ofPositive(160) -> SeichiExpAmount.ofNonNegative(740_000),
    SeichiLevel.ofPositive(170) -> SeichiExpAmount.ofNonNegative(820_000),
    SeichiLevel.ofPositive(180) -> SeichiExpAmount.ofNonNegative(920_000),
    SeichiLevel.ofPositive(190) -> SeichiExpAmount.ofNonNegative(1_000_000),
    SeichiLevel.ofPositive(199) -> SeichiExpAmount.ofNonNegative(1_150_000),
    SeichiLevel.ofPositive(200) -> SeichiExpAmount.ofNonNegative(1_500_000)
  )
  //@formatter:on

  /**
   * 整地量をレベルに関連付ける経験値テーブル
   */
  val table: FiniteExpLevelTable[SeichiLevel, SeichiExpAmount] = {
    linearIncreases.foldLeft(tableUpToLevel51) {
      case (table, (level, amount)) =>
        table.extendToLevel(level).withLinearIncreaseOf(amount)
    }
  }
}

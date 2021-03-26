package com.github.unchama.seichiassist.subsystems.mana.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel

case class LevelCappedManaAmount private(manaAmount: ManaAmount, level: SeichiLevel) {

  import cats.implicits._

  private val cap = ManaAmountCap.at(level)

  assert(
    manaAmount <= cap,
    "LevelCappedManaAmountはマナのキャップ制約を満たす必要があります"
  )

  def add(amount: ManaAmount): LevelCappedManaAmount = {
    LevelCappedManaAmount.capping(manaAmount.add(amount), level)
  }

  def tryUse(amount: ManaAmount): Option[LevelCappedManaAmount] = {
    manaAmount.tryUse(amount).map(LevelCappedManaAmount(_, level))
  }

  def withHigherLevel(newLevel: SeichiLevel): Unit = {
    require(newLevel >= level, "レベルは現在のレベル以上である必要があります")
    LevelCappedManaAmount(manaAmount, newLevel)
  }

  /**
   * マナを最大値にまで引き上げた [[LevelCappedManaAmount]]
   */
  lazy val fillToCap: LevelCappedManaAmount = LevelCappedManaAmount(cap, level)

  /**
   * マナ最大値に対する `manaAmount` の割合を示す0以上1未満の数値
   */
  lazy val ratioToCap: Double = cap.value / manaAmount.value
}

object LevelCappedManaAmount {

  import cats.implicits._

  def capping(manaAmount: ManaAmount, level: SeichiLevel): LevelCappedManaAmount = {
    LevelCappedManaAmount(manaAmount min ManaAmountCap.at(level), level)
  }
}

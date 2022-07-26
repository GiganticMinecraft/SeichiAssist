package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

import scala.util.Random

object FairyRecoveryManaAmount {

  def manaAmountAt(levelCappedManaAmount: Double): FairyRecoveryMana = {
    require(levelCappedManaAmount >= 0.0, "levelCappedManaAmountは非負の値で指定してください。")
    FairyRecoveryMana(
      (levelCappedManaAmount / 10 - levelCappedManaAmount / 30 + new Random()
        .nextInt((levelCappedManaAmount / 20).toInt) / 2.9).toInt + 200
    )
  }

}

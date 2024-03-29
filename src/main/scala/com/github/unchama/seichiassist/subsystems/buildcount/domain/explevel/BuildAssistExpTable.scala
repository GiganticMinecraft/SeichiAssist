package com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel

import com.github.unchama.seichiassist.domain.explevel.FiniteExpLevelTable

private object Constant {
  // 経験値テーブルの生の値
  val internalTable: Vector[BuildExpAmount] =
    Vector(0, 50, 100, 200, 300, 450, 600, 900, 1200, 1600, // 10
      2000, 2500, 3000, 3600, 4300, 5100, 6000, 7000, 8200, 9400, // 20
      10800, 12200, 13800, 15400, 17200, 19000, 21000, 23000, 25250, 27500, // 30
      30000, 32500, 35500, 38500, 42000, 45500, 49500, 54000, 59000, 64000, // 40
      70000, 76000, 83000, 90000, 98000, 106000, 115000, 124000, 133000, 143000, // 50
      153000, 163000, 174000, 185000, 196000, 208000, 220000, 232000, 245000, 258000, // 60
      271000, 285000, 299000, 313000, 328000, 343000, 358000, 374000, 390000, 406000, // 70
      423000, 440000, 457000, 475000, 493000, 511000, 530000, 549000, 568000, 588000, // 80
      608000, 628000, 648000, 668000, 688000, 708000, 728000, 748000, 768000, 788000, // 90
      808000, 828000, 848000, 868000, 888000, 908000, 928000, 948000, 968000, 1000000 // 100
    ).map(BuildExpAmount.ofNonNegative)
}

object BuildAssistExpTable
    extends FiniteExpLevelTable[BuildLevel, BuildExpAmount](Constant.internalTable)

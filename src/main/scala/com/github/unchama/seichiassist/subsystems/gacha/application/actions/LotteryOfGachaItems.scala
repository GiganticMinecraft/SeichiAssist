package com.github.unchama.seichiassist.subsystems.gacha.application.actions

import com.github.unchama.seichiassist.subsystems.gacha.domain.GlobalGachaPrizeList
import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize.GachaPrize

/**
 * ガチャアイテムの抽選を行う作用
 */
trait LotteryOfGachaItems[F[_], ItemStack] {

  def runLottery(
    amount: Int,
    gachaPrizesListRepository: GlobalGachaPrizeList[F, ItemStack]
  ): F[Vector[GachaPrize[ItemStack]]]

}

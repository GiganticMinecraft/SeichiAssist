package com.github.unchama.seichiassist.subsystems.gacha.application.actions

import cats.effect.concurrent.Ref
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrize

/**
 * ガチャアイテムの抽選を行う作用
 */
trait LotteryOfGachaItems[F[_], ItemStack] {

  def runLottery(
    amount: Int,
    gachaPrizesListRepository: Ref[F, Vector[GachaPrize[ItemStack]]]
  ): F[Vector[GachaPrize[ItemStack]]]

}

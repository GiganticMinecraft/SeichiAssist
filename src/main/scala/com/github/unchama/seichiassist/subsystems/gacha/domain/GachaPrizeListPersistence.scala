package com.github.unchama.seichiassist.subsystems.gacha.domain

import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaevent.GachaEventName

trait GachaPrizeListPersistence[F[_], ItemStack] {

  /**
   * ガチャアイテムとして登録されているアイテムリストをGachaPrizeのVectorとして返します。
   */
  def list: F[Vector[GachaPrize[ItemStack]]]

  /**
   * ガチャリストを更新します。
   */
  def set(gachaPrizesList: Vector[GachaPrize[ItemStack]]): F[Unit]

  /**
   * 常に排出されるガチャ景品を取得します。
   */
  def alwaysDischargeGachaPrizes: F[Vector[GachaPrize[ItemStack]]]

  /**
   * 指定されたイベント名のガチャ景品を取得します。
   */
  def getOnlyGachaEventDischargeGachaPrizes(
    gachaEventName: GachaEventName
  ): F[Vector[GachaPrize[ItemStack]]]

}

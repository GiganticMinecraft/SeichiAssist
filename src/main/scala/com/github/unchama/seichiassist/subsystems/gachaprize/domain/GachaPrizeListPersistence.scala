package com.github.unchama.seichiassist.subsystems.gachaprize.domain

import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.GachaEvent

trait GachaPrizeListPersistence[F[_], ItemStack] {

  /**
   * @return ガチャアイテムとして登録されているアイテムの一覧を返す作用
   */
  def list: F[Vector[GachaPrize[ItemStack]]]

  /**
   * @return `gachaPrize`と同様のgachaPrizeIdが存在すれば`gachaPrize`に更新し、
   *         存在しなければ`gachaPrize`を追加します。
   */
  def upsertGachaPrize(gachaPrize: GachaPrize[ItemStack]): F[Unit]

  /**
   * @return ガチャアイテムを削除する作用
   */
  def removeGachaPrize(gachaPrizeId: GachaPrizeId): F[Unit]

  /**
   * @return 通常排出のガチャ景品をイベント景品として複製する作用
   */
  def duplicateDefaultGachaPrizes(gachaEvent: GachaEvent): F[Unit]

}

package com.github.unchama.seichiassist.subsystems.gachaprize.domain

import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.GachaEvent

trait GachaPrizeListPersistence[F[_], ItemStack] {

  /**
   * @return ガチャアイテムとして登録されているアイテムの一覧を返す作用
   */
  def list: F[Vector[GachaPrizeTableEntry[ItemStack]]]

  /**
   * @return `gachaPrize`と同様のgachaPrizeIdが存在すれば`gachaPrize`に更新し、
   *         存在しなければ`gachaPrize`を追加する作用
   */
  def upsertGachaPrize(gachaPrize: GachaPrizeTableEntry[ItemStack]): F[Unit]

  /**
   * @return ガチャアイテムを削除する作用
   */
  def removeGachaPrize(gachaPrizeId: GachaPrizeId): F[Unit]

  /**
   * @return イベント開催中ではない時に排出されるガチャ景品を、
   *         `gachaEvent`の景品としてidとガチャイベント以外を同一の内容で追加する作用
   */
  def duplicateDefaultGachaPrizes(gachaEvent: GachaEvent): F[Unit]

}

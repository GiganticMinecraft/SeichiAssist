package com.github.unchama.seichiassist.subsystems.gacha.domain

trait GachaPrizeListPersistence[F[_], ItemStack] {

  /**
   * ガチャアイテムとして登録されているアイテムリストをGachaPrizeのVectorとして返します。
   */
  def list(
    implicit gachaPrizeEncoder: GachaPrizeEncoder[ItemStack]
  ): F[Vector[GachaPrize[ItemStack]]]

  /**
   * ガチャリストを更新します。
   */
  def set(gachaPrizesList: Vector[GachaPrize[ItemStack]])(
    implicit gachaPrizeEncoder: GachaPrizeEncoder[ItemStack]
  ): F[Unit]

}

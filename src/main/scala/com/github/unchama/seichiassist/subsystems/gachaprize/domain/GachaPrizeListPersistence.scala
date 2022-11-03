package com.github.unchama.seichiassist.subsystems.gachaprize.domain

import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.GachaPrize

trait GachaPrizeListPersistence[F[_], ItemStack] {

  /**
   * ガチャアイテムとして登録されているアイテムリストをGachaPrizeのVectorとして返します。
   */
  def list: F[Vector[GachaPrize[ItemStack]]]

  /**
   * ガチャリストを更新します。
   */
  def set(gachaPrizesList: Vector[GachaPrize[ItemStack]]): F[Unit]

}

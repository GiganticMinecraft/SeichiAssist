package com.github.unchama.seichiassist.subsystems.gachaprize.domain

import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.{
  GachaPrize,
  GachaPrizeId
}

trait GachaPrizeListPersistence[F[_], ItemStack] {

  /**
   * @return ガチャアイテムとして登録されているアイテムの一覧を返す作用
   */
  def list: F[Vector[GachaPrize[ItemStack]]]

  /**
   * @return ガチャリストを更新する作用
   */
  def set(gachaPrizesList: Vector[GachaPrize[ItemStack]]): F[Unit]

  /**
   * @return mineStackGachaObjectを追加する作用
   */
  def addMineStackGachaObject(id: GachaPrizeId, objectName: String): F[Unit]

  /**
   * @return mineStackGachaObjectを削除する作用
   */
  def deleteMineStackGachaObject(id: GachaPrizeId): F[Unit]

}

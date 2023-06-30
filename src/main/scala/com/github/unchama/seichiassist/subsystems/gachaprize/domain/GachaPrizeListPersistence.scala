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
   * @return ガチャアイテムを追加する作用
   */
  def addGachaPrize(gachaPrize: GachaPrize[ItemStack]): F[Unit]

  /**
   * @return ガチャアイテムを削除する作用
   */
  def removeGachaPrize(gachaPrizeId: GachaPrizeId): F[Unit]

  /**
   * @return 複数のガチャアイテムを追加する作用
   */
  def addGachaPrizes(gachaPrizes: Vector[GachaPrize[ItemStack]]): F[Unit]

  /**
   * @return ガチャアイテムを削除する作用
   */
  def removeGachaPrize(gachaPrizeId: GachaPrizeId): F[Unit]


  /**
   * @return mineStackGachaObjectを追加する作用
   */
  def addMineStackGachaObject(id: GachaPrizeId, objectName: String): F[Unit]

  /**
   * @return mineStackGachaObjectを削除する作用
   */
  def deleteMineStackGachaObject(id: GachaPrizeId): F[Unit]

}

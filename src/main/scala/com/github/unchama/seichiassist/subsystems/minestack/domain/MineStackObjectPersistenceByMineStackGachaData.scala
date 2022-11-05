package com.github.unchama.seichiassist.subsystems.minestack.domain

trait MineStackObjectPersistenceByMineStackGachaData[F[_], ItemStack] {

  /**
   * @return mineStackGachaObjectを追加する
   */
  def addMineStackGachaObject(id: MineStackGachaObjectId, objectName: String): F[Unit]

  /**
   * @return mineStackGachaObjectを削除する
   */
  def deleteMineStackGachaObject(id: MineStackGachaObjectId): F[Unit]

}

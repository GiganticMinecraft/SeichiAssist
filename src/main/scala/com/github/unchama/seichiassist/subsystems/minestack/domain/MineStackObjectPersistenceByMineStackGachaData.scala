package com.github.unchama.seichiassist.subsystems.minestack.domain

import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject

trait MineStackObjectPersistenceByMineStackGachaData[F[_], ItemStack] {

  /**
   * @return MineStackGachaDataから[[MineStackObject]]のListを取得する
   */
  def load: F[List[MineStackObject[ItemStack]]]

  /**
   * @return mineStackGachaObjectを追加する
   */
  def addMineStackGachaObject(objectName: String): F[Unit]

  /**
   * @return mineStackGachaObjectを削除する
   */
  def deleteMineStackGachaObject(id: MineStackGachaObjectId): F[Unit]

}

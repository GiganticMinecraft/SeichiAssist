package com.github.unchama.seichiassist.subsystems.minestack.domain.persistence

import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackGachaObject

trait MineStackGachaObjectPersistence[F[_], ItemStack] {

  /**
   * @return mineStackGachaObjectをすべて取得する作用
   */
  def getAllMineStackGachaObjects: F[Vector[MineStackGachaObject[ItemStack]]]

}

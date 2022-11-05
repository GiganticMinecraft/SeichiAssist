package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

import com.github.unchama.generic.RefDict
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackGachaObject

import java.util.UUID

trait MineStackObjectPersistence[F[_], ItemStack]
    extends RefDict[F, UUID, List[MineStackObjectWithAmount[ItemStack]]] {

  /**
   * @return mineStackGachaObjectをすべて取得する作用
   */
  def getAllMineStackGachaObjects: F[Vector[MineStackGachaObject[ItemStack]]]

}

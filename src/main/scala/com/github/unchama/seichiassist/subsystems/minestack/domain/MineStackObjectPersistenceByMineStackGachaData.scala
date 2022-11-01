package com.github.unchama.seichiassist.subsystems.minestack.domain

import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject

trait MineStackObjectPersistenceByMineStackGachaData[F[_], ItemStack] {

  /**
   * @return MineStackGachaDataから[[MineStackObject]]のListを取得する
   */
  def load: F[List[MineStackObject[ItemStack]]]
}

package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

trait MineStackObjectList[F[_], ItemStack <: Cloneable] {

  /**
   * @return [[MineStackObject]]をすべて返します
   */
  def allMineStackObjects: F[Vector[MineStackObject[ItemStack]]]

}

package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

trait MineStackObjectList[F[_], ItemStack <: Cloneable] {

  /**
   * @return [[MineStackObject]]をすべて返します
   */
  def allMineStackObjects: F[Vector[MineStackObject[ItemStack]]]

  /**
   * @return [[ItemStack]]から[[MineStackObject]]を取得します
   */
  def findByItemStack(itemStack: ItemStack): F[Option[MineStackObject[ItemStack]]]

  /**
   * @return `name`から[[MineStackObject]]を取得します
   */
  def findByName(name: String): F[Option[MineStackObject[ItemStack]]]

}

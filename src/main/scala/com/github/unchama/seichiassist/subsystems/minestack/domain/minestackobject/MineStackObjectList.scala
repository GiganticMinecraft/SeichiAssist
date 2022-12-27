package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

trait MineStackObjectList[F[_], ItemStack, Player] {

  /**
   * @return [[MineStackObject]]をすべて返す作用
   */
  def allMineStackObjects: F[Vector[MineStackObject[ItemStack]]]

  /**
   * @return [[ItemStack]]から[[MineStackObject]]を取得しようとする作用
   */
  def findByItemStack(
    itemStack: ItemStack,
    player: Player
  ): F[Option[MineStackObject[ItemStack]]]

  /**
   * @return `name`から[[MineStackObject]]を取得する作用
   */
  def findByName(name: String): F[Option[MineStackObject[ItemStack]]]

  /**
   * @return `category`を指定してすべての[[MineStackObjectGroup]]を取得する作用
   */
  def getAllObjectGroupsInCategory(
    category: MineStackObjectCategory
  ): F[List[MineStackObjectGroup[ItemStack]]]

}

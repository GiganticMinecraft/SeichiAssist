package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

trait MineStackObjectList[F[_], ItemStack, Player] {

  /**
   * @return [[MineStackObject]]をすべて返します
   */
  def allMineStackObjects: F[Vector[MineStackObject[ItemStack]]]

  /**
   * @return [[ItemStack]]から[[MineStackObject]]を取得します
   */
  def findByItemStack(
    itemStack: ItemStack,
    player: Player
  ): F[Option[MineStackObject[ItemStack]]]

  /**
   * @return `name`から[[MineStackObject]]を取得します
   */
  def findByName(name: String): F[Option[MineStackObject[ItemStack]]]

  /**
   * `category`を指定してすべての[[MineStackObjectGroup]]を取得します。
   */
  def getAllObjectGroupsInCategory(
    category: MineStackObjectCategory
  ): F[List[MineStackObjectGroup[ItemStack]]]

}

package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

import cats.Functor

trait MineStackObjectList[F[_], ItemStack, Player] {

  /**
   * @return [[MineStackObject]]をすべて返す作用
   */
  def allMineStackObjects: F[Vector[MineStackObject[ItemStack]]]

  /**
   * @param itemStack 記名することのできるアイテムは、既に記名されていることを想定している
   * @return [[ItemStack]]から[[MineStackObject]]を取得しようとする作用
   */
  def findBySignedItemStacks(
    itemStack: Vector[ItemStack],
    player: Player
  ): F[Vector[Option[MineStackObject[ItemStack]]]]

  protected implicit val F: Functor[F]

  import cats.implicits._

  /**
   * @return `name`から[[MineStackObject]]を取得する作用
   */
  final def findByName(name: String): F[Option[MineStackObject[ItemStack]]] = for {
    result <- allMineStackObjects.map(_.find(_.mineStackObjectName == name))
  } yield result

  /**
   * @return `category`を指定してすべての[[MineStackObjectGroup]]を取得する作用
   */
  def getAllObjectGroupsInCategory(
    category: MineStackObjectCategory
  ): F[List[MineStackObjectGroup[ItemStack]]]

}

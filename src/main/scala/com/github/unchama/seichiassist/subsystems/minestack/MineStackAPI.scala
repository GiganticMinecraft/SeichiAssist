package com.github.unchama.seichiassist.subsystems.minestack

import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject

trait MineStackWriteAPI[F[_], Player, ItemStack] {

  /**
   * @return [[Player]]の[[MineStackObject]]を指定された量だけ増加させる作用
   */
  def addStackedAmountOf(
    player: Player,
    mineStackObject: MineStackObject[ItemStack],
    amount: Int
  ): F[Unit]

  /**
   * @return [[Player]]の[[MineStackObject]]を指定された量だけ減少させ、実際に減少させた量を返す
   */
  def trySubtractStackedAmountOf(
    player: Player,
    mineStackObject: MineStackObject[ItemStack],
    amount: Int
  ): F[Int]

  /**
   * @return [[Player]]が持っている[[MineStackObject]]の量を取得します
   */
  def getStackedAmountOf(
    player: Player,
    mineStackObject: MineStackObject[ItemStack]
  ): F[Long]

}

object MineStackWriteAPI {

  def apply[F[_], Player, ItemStack](
    implicit ev: MineStackWriteAPI[F, Player, ItemStack]
  ): MineStackWriteAPI[F, Player, ItemStack] = ev

}

trait MineStackAPI[F[_], Player, ItemStack] extends MineStackWriteAPI[F, Player, ItemStack]

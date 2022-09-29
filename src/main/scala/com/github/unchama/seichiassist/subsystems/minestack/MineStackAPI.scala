package com.github.unchama.seichiassist.subsystems.minestack

import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject

trait MineStackWriteAPI[F[_], Player] {

  /**
   * @return [[Player]]の[[MineStackObject]]を指定された量だけ増加させる作用
   */
  def addStackedAmountOf(player: Player, mineStackObject: MineStackObject, amount: Int): F[Unit]

  /**
   * @return [[Player]]の[[MineStackObject]]を指定された量だけ減少させ、実際に減少させた量を返す
   */
  def subtractStackedAmountOf(
    player: Player,
    mineStackObject: MineStackObject,
    amount: Int
  ): F[Int]

}

object MineStackWriteAPI {

  def apply[F[_], Player](
    implicit ev: MineStackWriteAPI[F, Player]
  ): MineStackWriteAPI[F, Player] = ev

}

trait MineStackAPI[F[_], Player] extends MineStackWriteAPI[F, Player]

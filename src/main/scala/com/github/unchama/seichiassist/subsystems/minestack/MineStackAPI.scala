package com.github.unchama.seichiassist.subsystems.minestack

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.minestack.domain.TryIntoMineStack
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectList
}

trait MineStackWriteAPI[F[_], Player, ItemStack] {

  /**
   * @return `player`の`mineStackObject`を`amount`だけ増加させる作用
   */
  def addStackedAmountOf(
    player: Player,
    mineStackObject: MineStackObject[ItemStack],
    amount: Int
  ): F[Unit]

  /**
   * `player`の`mineStackObject`を`amount`だけ減少させます。
   * @return 実際に減少させた量を返す作用
   */
  def subtractStackedAmountOf(
    player: Player,
    mineStackObject: MineStackObject[ItemStack],
    amount: Long
  ): F[Long]

  /**
   * @return `player`のMineStackUsageHistoryに`mineStackObject`を追加する作用
   */
  def addUsageHistory(mineStackObject: MineStackObject[ItemStack]): Kleisli[F, Player, Unit]

  /**
   * @return AutoMineStackをステータスをトグルする作用
   */
  def toggleAutoMineStack: Kleisli[F, Player, Unit]

  /**
   * @return プレイヤーのMineStackリポジトリに格納を試みる関数
   */
  def tryIntoMineStack: TryIntoMineStack[F, Player, ItemStack]

}

object MineStackWriteAPI {

  def apply[F[_], Player, ItemStack](
    implicit ev: MineStackWriteAPI[F, Player, ItemStack]
  ): MineStackWriteAPI[F, Player, ItemStack] = ev

}

trait MineStackReadAPI[F[_], Player, ItemStack] {

  /**
   * @return `player`が持っている`mineStackObject`の量を取得する作用
   */
  def getStackedAmountOf(player: Player, mineStackObject: MineStackObject[ItemStack]): F[Long]

  /**
   * @return `player`のMineStackの使用履歴を取得する作用
   */
  def getUsageHistory(player: Player): F[Vector[MineStackObject[ItemStack]]]

  /**
   * @return 現在、自動回収されるかどうかを返す作用
   */
  def autoMineStack(player: Player): F[Boolean]

  /**
   * @return [[MineStackObject]]に対する操作を提供するtrait
   */
  def mineStackObjectList: MineStackObjectList[F, ItemStack, Player]

}

object MineStackReadAPI {

  def apply[F[_], Player, ItemStack](
    implicit ev: MineStackReadAPI[F, Player, ItemStack]
  ): MineStackReadAPI[F, Player, ItemStack] = ev

}

trait MineStackAPI[F[_], Player, ItemStack]
    extends MineStackWriteAPI[F, Player, ItemStack]
    with MineStackReadAPI[F, Player, ItemStack]

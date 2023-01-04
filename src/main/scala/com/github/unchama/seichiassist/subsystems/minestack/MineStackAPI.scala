package com.github.unchama.seichiassist.subsystems.minestack

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackRepository
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectList
}

trait MineStackWriteAPI[F[_], Player, ItemStack] {

  /**
   * @return `player`のMineStackUsageHistoryに`mineStackObject`を追加する作用
   */
  def addUsageHistory(mineStackObject: MineStackObject[ItemStack]): Kleisli[F, Player, Unit]

  /**
   * @param isItemCollectedAutomatically 自動収集を行うかどうか
   * @return 自動収集のステータスを`isItemCollectedAutomatically`に更新する作用
   */
  def setAutoMineStack(isItemCollectedAutomatically: Boolean): Kleisli[F, Player, Unit]

  /**
   * @return 自動収集のステータスをトグルする作用
   */
  def toggleAutoMineStack: Kleisli[F, Player, Unit]

}

object MineStackWriteAPI {

  def apply[F[_], Player, ItemStack](
    implicit ev: MineStackWriteAPI[F, Player, ItemStack]
  ): MineStackWriteAPI[F, Player, ItemStack] = ev

}

trait MineStackReadAPI[F[_], Player, ItemStack] {

  /**
   * @return `player`のMineStackの使用履歴を取得する作用
   */
  def getUsageHistory(player: Player): F[Vector[MineStackObject[ItemStack]]]

  /**
   * @return 現在、自動回収されるかどうかを返す作用
   */
  def autoMineStack(player: Player): F[Boolean]

  /**
   * @return [[MineStackObject]]に対する操作を提供するクラス
   */
  def mineStackObjectList: MineStackObjectList[F, ItemStack, Player]

  /**
   * @return MineStackのアイテム数に関する操作を提供するクラス
   */
  def mineStackRepository: MineStackRepository[F, Player, ItemStack]

}

object MineStackReadAPI {

  def apply[F[_], Player, ItemStack](
    implicit ev: MineStackReadAPI[F, Player, ItemStack]
  ): MineStackReadAPI[F, Player, ItemStack] = ev

}

trait MineStackAPI[F[_], Player, ItemStack]
    extends MineStackWriteAPI[F, Player, ItemStack]
    with MineStackReadAPI[F, Player, ItemStack]

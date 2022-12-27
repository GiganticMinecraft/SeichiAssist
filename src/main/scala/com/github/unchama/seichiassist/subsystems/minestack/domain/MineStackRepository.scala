package com.github.unchama.seichiassist.subsystems.minestack.domain

import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject

/**
 * [[Player]]のMineStackリポジトリに対して行う操作を持つ抽象。
 */
trait MineStackRepository[F[_], Player, ItemStack] {

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
   * [[Player]]のMineStackリポジトリに[[ItemStack]]を格納することを試みます。
   * @return 格納できたら`true`、格納に失敗すれば`false`
   */
  def tryIntoMineStack(player: Player, itemStack: ItemStack, amount: Int): F[Boolean]

}

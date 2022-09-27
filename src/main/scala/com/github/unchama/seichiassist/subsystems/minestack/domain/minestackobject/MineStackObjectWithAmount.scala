package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

case class MineStackObjectWithAmount(mineStackObject: MineStackObject, amount: Long) {
  require(amount >= 0)

  /**
   * @return `amount`を指定分だけ増加させる作用
   */
  def increase(value: Long): MineStackObjectWithAmount = {
    this.copy(amount = amount + value)
  }

  /**
   * @return `amount`を指定分だけ減少させる作用
   */
  def decrease(value: Long): MineStackObjectWithAmount = {
    this.copy(amount = amount - value)
  }

}

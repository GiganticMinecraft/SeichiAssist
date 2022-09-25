package com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject

case class MineStackObjectWithAmount(mineStackObject: MineStackObject, amount: Long) {
  require(amount >= 0)
}

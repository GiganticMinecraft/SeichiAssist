package com.github.unchama.seichiassist.subsystems.minestack.domain

case class MineStackGachaObjectId(value: Int) {
  require(value >= 0)
}

package com.github.unchama.seichiassist.subsystems.gacha.domain

abstract class ConsumeAmount(value: Int)

object ConsumeAmount {
  case object oneThousand extends ConsumeAmount(1000)
  case object fiveThousands extends ConsumeAmount(5000)
  case object tenThousands extends ConsumeAmount(10000)
}

package com.github.unchama.seichiassist.subsystems.vote.domain

case class EffectPoint(value: Int) extends AnyVal {
  require(value >= 0)
}

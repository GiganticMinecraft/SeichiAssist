package com.github.unchama.seichiassist.subsystems.vote.domain

case class EffectPoint(value: Int) {
  require(value >= 0)
}

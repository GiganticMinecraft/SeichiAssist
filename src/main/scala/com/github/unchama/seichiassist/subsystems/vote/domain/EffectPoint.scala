package com.github.unchama.seichiassist.subsystems.vote.domain

case class EffectPoint(value: Int) {
  require(value >= 0, "EffectPointは非負である必要があります。")
}

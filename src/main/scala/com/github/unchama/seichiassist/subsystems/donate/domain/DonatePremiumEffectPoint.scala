package com.github.unchama.seichiassist.subsystems.donate.domain

case class DonatePremiumEffectPoint(value: Int) {
  require(value >= 0, "DonatePremiumEffectPointは非負の値で指定してください。")
}

package com.github.unchama.seichiassist.subsystems.mana.domain

case class ManaMultiplier(value: Double) {
  require(value >= 0, "マナ利用倍率は0以上である必要があります。")
}

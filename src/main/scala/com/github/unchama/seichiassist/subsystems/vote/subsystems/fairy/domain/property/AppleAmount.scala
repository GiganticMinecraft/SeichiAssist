package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

case class AppleAmount(amount: Int) {
  require(amount >= 0, "amountは非負の値で指定してください。")
}

package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain

case class FairyRecoveryMana(recoveryMana: Int) {
  require(recoveryMana >= 0)
}

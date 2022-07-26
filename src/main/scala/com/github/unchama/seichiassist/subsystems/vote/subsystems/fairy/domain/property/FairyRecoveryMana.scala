package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

case class FairyRecoveryMana(recoveryMana: Int) {
  require(recoveryMana >= 0)
}

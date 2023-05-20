package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property

case class FairyMessage(message: String) {
  require(message.nonEmpty)
}

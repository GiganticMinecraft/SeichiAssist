package com.github.unchama.seichiassist.subsystems.vote.domain

case class PlayerName(name: String) {
  require(name.forall(_.isLower))
}

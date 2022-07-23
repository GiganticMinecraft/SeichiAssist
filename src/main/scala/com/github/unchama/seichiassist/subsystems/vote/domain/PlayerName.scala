package com.github.unchama.seichiassist.subsystems.vote.domain

case class PlayerName(name: String) extends AnyVal {
  require(name.forall(_.isLower))
}

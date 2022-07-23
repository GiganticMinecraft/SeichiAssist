package com.github.unchama.seichiassist.subsystems.vote.domain

case class VoteBenefit(value: Int) extends AnyVal {
  require(value >= 0)
}

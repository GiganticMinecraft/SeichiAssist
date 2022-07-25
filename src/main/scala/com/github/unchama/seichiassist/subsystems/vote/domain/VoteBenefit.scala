package com.github.unchama.seichiassist.subsystems.vote.domain

case class VoteBenefit(value: Int) {
  require(value >= 0)
}

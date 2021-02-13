package com.github.unchama.seichiassist

sealed trait VotingFairyStrategy {
  def internalId: Int

  def votingPointCost: Int
}

object VotingFairyStrategy {
  case object Much extends VotingFairyStrategy {
    override def internalId: Int = 1

    override def votingPointCost: Int = 8
  }
  case object More extends VotingFairyStrategy
  case object Less extends VotingFairyStrategy
  case object None extends VotingFairyStrategy

  def apply(internal: Int): VotingFairyStrategy = internal match {
    case 1 => Much
    case 2 => More
    case 3 => Less
    case 4 => None
  }
}

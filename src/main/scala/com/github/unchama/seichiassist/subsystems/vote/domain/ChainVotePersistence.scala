package com.github.unchama.seichiassist.subsystems.vote.domain

import java.util.UUID

trait ChainVotePersistence[F[_]] {

  def updateChainVote(playerName: PlayerName): F[Unit]

  def getChainVoteDays(uuid: UUID): F[ChainVoteDayNumber]

}

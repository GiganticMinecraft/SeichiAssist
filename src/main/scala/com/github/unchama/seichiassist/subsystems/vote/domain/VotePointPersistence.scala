package com.github.unchama.seichiassist.subsystems.vote.domain

import java.util.UUID

trait VotePointPersistence[F[_]] {

  def increment(playerName: PlayerName): F[Unit]

  def votePoint(uuid: UUID): F[VotePoint]

}

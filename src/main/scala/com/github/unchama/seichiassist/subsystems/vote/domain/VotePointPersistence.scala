package com.github.unchama.seichiassist.subsystems.vote.domain

import java.util.UUID

trait VotePointPersistence[F[_]] {

  def increment(uuid: UUID): F[Unit]

  def votePoint(uuid: UUID): F[VotePoint]

}

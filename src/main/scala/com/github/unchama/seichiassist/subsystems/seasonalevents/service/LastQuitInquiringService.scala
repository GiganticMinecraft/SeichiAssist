package com.github.unchama.seichiassist.subsystems.seasonalevents.service

import java.time.LocalDateTime

import cats.FlatMap
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.LastQuitPersistenceRepository

class LastQuitInquiringService[
  F[_]: FlatMap
](implicit persistenceRepository: LastQuitPersistenceRepository[F, String]) {

  import cats.implicits._
  import persistenceRepository._

  def loadLastQuitDateTime(playerName: String): F[Option[LocalDateTime]] = {
    for {
      result <- loadPlayerLastQuit(playerName)
    } yield result
  }
}
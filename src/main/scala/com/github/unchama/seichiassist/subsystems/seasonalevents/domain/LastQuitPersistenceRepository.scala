package com.github.unchama.seichiassist.subsystems.seasonalevents.domain

import java.time.LocalDateTime
import java.util.UUID

trait LastQuitPersistenceRepository[F[_], Key] {
  def loadPlayerLastQuit(key: Key): F[Option[LocalDateTime]]
}
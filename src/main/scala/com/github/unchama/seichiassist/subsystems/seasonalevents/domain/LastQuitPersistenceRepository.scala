package com.github.unchama.seichiassist.subsystems.seasonalevents.domain

import java.time.LocalDateTime

trait LastQuitPersistenceRepository[F[_], Key] {
  def loadPlayerLastQuit(key: Key): F[Option[LocalDateTime]]
}

package com.github.unchama.seichiassist.subsystems.lastquit

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.lastquit.domain.{
  LastQuitDateTime,
  LastQuitPersistence
}
import com.github.unchama.seichiassist.subsystems.lastquit.infrastructure.JdbcLastQuitPersistence

import java.util.UUID

trait System[F[_]] {

  val api: LastQuitAPI[F]

}

object System {

  def wired[F[_]: Sync]: System[F] = {
    val persistence: LastQuitPersistence[F] = new JdbcLastQuitPersistence[F]
    new System[F] {
      override val api: LastQuitAPI[F] = new LastQuitAPI[F] {
        override def lastQuitDateTime(uuid: UUID): F[LastQuitDateTime] =
          persistence.lastQuitDateTime(uuid)

        override def updateLastLastQuitDateTimeNow(uuid: UUID): F[Unit] =
          persistence.updateLastQuitNow(uuid)
      }
    }

  }

}

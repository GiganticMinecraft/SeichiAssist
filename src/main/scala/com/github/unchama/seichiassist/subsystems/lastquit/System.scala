package com.github.unchama.seichiassist.subsystems.lastquit

import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.lastquit.bukkit.commands.LastQuitCommand
import com.github.unchama.seichiassist.subsystems.lastquit.bukkit.listeners.LastQuitUpdater
import com.github.unchama.seichiassist.subsystems.lastquit.domain.{
  LastQuitDateTime,
  LastQuitPersistence
}
import com.github.unchama.seichiassist.subsystems.lastquit.infrastructure.JdbcLastQuitPersistence
import org.bukkit.command.TabExecutor
import org.bukkit.event.Listener

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {

  val api: LastQuitAPI[F]

}

object System {

  def wired[F[_]: ConcurrentEffect]: System[F] = {
    val persistence: LastQuitPersistence[F] = new JdbcLastQuitPersistence[F]
    new System[F] {
      override implicit val api: LastQuitAPI[F] = new LastQuitAPI[F] {
        override def get(uuid: UUID): F[Option[LastQuitDateTime]] =
          persistence.lastQuitDateTime(uuid)

        override def updateLastLastQuitDateTimeNow(uuid: UUID): F[Unit] =
          persistence.updateLastQuitNow(uuid)
      }

      override val commands: Map[String, TabExecutor] = Map(
        "lastquit" -> new LastQuitCommand().executor
      )

      override val listeners: Seq[Listener] = Seq(new LastQuitUpdater[F])

    }

  }

}

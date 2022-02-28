package com.github.unchama.seichiassist.subsystems.bookedachivement

import cats.effect.ConcurrentEffect
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.command.AchievementCommand
import com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.listener.GrantBookedAchievementListener
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.BookedAchievementPersistenceRepository
import com.github.unchama.seichiassist.subsystems.bookedachivement.infrastructure.JdbcBookedAchievementPersistenceRepository
import com.github.unchama.seichiassist.subsystems.bookedachivement.service.AchievementBookingService
import org.bukkit.command.TabExecutor
import org.bukkit.event.Listener

import java.util.UUID

object System {
  def wired[F[_]: ConcurrentEffect: NonServerThreadContextShift, G[_]](
    implicit effectEnvironment: EffectEnvironment
  ): Subsystem[G] = {

    implicit val repository: BookedAchievementPersistenceRepository[F, UUID] =
      new JdbcBookedAchievementPersistenceRepository[F]

    implicit val service: AchievementBookingService[F] =
      new AchievementBookingService[F]

    new Subsystem[G] {
      override val listeners: Seq[Listener] = Seq(new GrantBookedAchievementListener())
      override val commands: Map[String, TabExecutor] = Map(
        "achievement" -> AchievementCommand.executor
      )
    }
  }
}

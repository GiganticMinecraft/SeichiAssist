package com.github.unchama.seichiassist.subsystems.bookedachivement

import java.util.UUID

import cats.effect.ConcurrentEffect
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.command.AchievementCommand
import com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.listener.GrantBookedAchievementListener
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.BookedAchievementPersistenceRepository
import com.github.unchama.seichiassist.subsystems.bookedachivement.infrastructure.JdbcBookedAchievementPersistenceRepository
import com.github.unchama.seichiassist.subsystems.bookedachivement.service.AchievementBookingService

object System {
  def wired[
    F[_] : ConcurrentEffect
  ](implicit effectEnvironment: EffectEnvironment): Subsystem = {

    implicit val repository: BookedAchievementPersistenceRepository[F, UUID] =
      new JdbcBookedAchievementPersistenceRepository[F]

    implicit val service: AchievementBookingService[F] =
      new AchievementBookingService[F]

    val listener = Seq(
      new GrantBookedAchievementListener()
    )

    Subsystem(listener, Map(
      "achievement" -> AchievementCommand.executor
    ))
  }
}

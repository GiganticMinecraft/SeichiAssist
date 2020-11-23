package com.github.unchama.seichiassist.subsystems.bookedachivement

import java.util.UUID

import cats.effect.ConcurrentEffect
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.command.AchievementCommand
import com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.listener.GrantBookedAchievementListener
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.BookedAchievementPersistenceRepository
import com.github.unchama.seichiassist.subsystems.bookedachivement.infrastructure.JdbcBookedAchievementPersistenceRepository
import com.github.unchama.seichiassist.subsystems.bookedachivement.service.AchievementBookingService

object System {
  def wired[
    AsyncContext[_] : ConcurrentEffect : NonServerThreadContextShift
  ](implicit effectEnvironment: EffectEnvironment): Subsystem = {

    implicit val repository: BookedAchievementPersistenceRepository[AsyncContext, UUID] =
      new JdbcBookedAchievementPersistenceRepository[AsyncContext]

    implicit val service: AchievementBookingService[AsyncContext] =
      new AchievementBookingService[AsyncContext]

    val listener = Seq(
      new GrantBookedAchievementListener()
    )

    Subsystem(listener, Map(
      "achievement" -> AchievementCommand.executor
    ))
  }
}

package com.github.unchama.seichiassist.subsystems.bookedachivement

import java.util.UUID

import cats.effect.SyncEffect
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.commands.AchievementCommand
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.bookedachivement.bukkit.listener.GrantBookedAchievementListener
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.BookedAchievementPersistenceRepository
import com.github.unchama.seichiassist.subsystems.bookedachivement.infrastructure.JdbcBookedAchievementPersistenceRepository
import com.github.unchama.seichiassist.subsystems.bookedachivement.service.AchievementBookingService

object System {
  def wired[
    SyncContext[_] : SyncEffect
  ](implicit effectEnvironment: EffectEnvironment): Subsystem = {

    implicit val repository: BookedAchievementPersistenceRepository[SyncContext, UUID] =
      new JdbcBookedAchievementPersistenceRepository[SyncContext]

    implicit val service: AchievementBookingService[SyncContext] =
      new AchievementBookingService[SyncContext]

    val listener = Seq(
      new GrantBookedAchievementListener()
    )

    Subsystem(listener, Map(
      "achievement" -> AchievementCommand.executor
    ))
  }
}

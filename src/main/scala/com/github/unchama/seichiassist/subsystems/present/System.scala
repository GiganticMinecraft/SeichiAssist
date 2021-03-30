package com.github.unchama.seichiassist.subsystems.present

import cats.effect.ConcurrentEffect
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.domain.actions.UuidToLastSeenName
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.present.bukkit.command.PresentCommand
import com.github.unchama.seichiassist.subsystems.present.infrastructure.JdbcBackedPresentPersistence
import org.bukkit.command.TabExecutor

object System {
  def wired[
    ConcurrentContext[_] : ConcurrentEffect : NonServerThreadContextShift
  ](implicit environment: EffectEnvironment, uuidToLastSeenName: UuidToLastSeenName[ConcurrentContext]): Subsystem[ConcurrentContext] = {
    implicit val repo: JdbcBackedPresentPersistence[ConcurrentContext] =
      new JdbcBackedPresentPersistence[ConcurrentContext]

    new Subsystem[ConcurrentContext] {
      override val commands: Map[String, TabExecutor] = Map(
        "present" -> PresentCommand.executor
      )
    }
  }
}

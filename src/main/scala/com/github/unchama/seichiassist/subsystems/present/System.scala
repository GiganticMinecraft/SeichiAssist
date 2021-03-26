package com.github.unchama.seichiassist.subsystems.present

import cats.effect.ConcurrentEffect
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.present.bukkit.command.PresentCommand
import com.github.unchama.seichiassist.subsystems.present.infrastructure.JdbcBackedPresentPersistence
import org.bukkit.command.TabExecutor

object System {
  def wired[
    AsyncContext[_] : ConcurrentEffect : NonServerThreadContextShift
  ](implicit environment: EffectEnvironment): Subsystem[AsyncContext] = {
    implicit val repo: JdbcBackedPresentPersistence[AsyncContext] =
      new JdbcBackedPresentPersistence[AsyncContext]

    new Subsystem[AsyncContext] {
      override val commands: Map[String, TabExecutor] = Map(
        "present" -> PresentCommand.executor
      )
    }
  }
}

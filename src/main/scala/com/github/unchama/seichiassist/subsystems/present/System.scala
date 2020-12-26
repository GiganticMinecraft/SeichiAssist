package com.github.unchama.seichiassist.subsystems.present

import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.present.bukkit.command.PresentCommand
import com.github.unchama.seichiassist.subsystems.present.infrastructure.JdbcBackedPresentRepository

object System {
  def wired[
    AsyncContext[_] : ConcurrentEffect : NonServerThreadContextShift,
    F[_]
  ](implicit environment: EffectEnvironment): Subsystem[F] = {
    implicit val repo: JdbcBackedPresentRepository[AsyncContext] =
      new JdbcBackedPresentRepository[AsyncContext]
    Subsystem(Seq(), Nil, Map("present" -> PresentCommand.executor))
  }
}

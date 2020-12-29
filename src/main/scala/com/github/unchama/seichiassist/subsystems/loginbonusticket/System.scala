package com.github.unchama.seichiassist.subsystems.loginbonusticket

import java.util.UUID

import cats.effect.ConcurrentEffect
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.loginbonusticket.bukkit.listeners.LoginBonusGifter
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.LastQuitPersistenceRepository
import com.github.unchama.seichiassist.subsystems.seasonalevents.infrastructure.JdbcLastQuitPersistenceRepository

object System {
  def wired[F[_] : ConcurrentEffect : NonServerThreadContextShift, G[_]](implicit effectEnvironment: EffectEnvironment): Subsystem[G] = {
    implicit val repository: LastQuitPersistenceRepository[F, UUID] =
      new JdbcLastQuitPersistenceRepository[F]

    val listeners = Seq(new LoginBonusGifter())

    Subsystem(listeners, Nil, Map())
  }
}
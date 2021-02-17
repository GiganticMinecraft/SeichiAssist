package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.process

import cats.effect.{ConcurrentEffect, Timer}
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.domain.playercount.GetConnectedPlayerCount
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectApi
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.Configuration
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{FastDiggingAmplifier, FastDiggingEffect, FastDiggingEffectCause}

object PlayerCountEffectSynchronization {

  import cats.implicits._

  import scala.concurrent.duration._

  def using[
    F[_] : ConcurrentEffect : Timer : GetConnectedPlayerCount,
    Player: HasUuid
  ](implicit
    configuration: Configuration,
    api: FastDiggingEffectApi[F, Player]): fs2.Stream[F, Unit] =
    api
      .effectClock
      .evalMap { case (player, _) =>
        GetConnectedPlayerCount[F].now.map(count => (player, count))
      }
      .evalMap { case (player, count) =>
        api.addEffect(
          FastDiggingEffect(
            FastDiggingAmplifier(configuration.amplifierPerPlayerConnection * count),
            FastDiggingEffectCause.FromConnectionNumber(count)
          ),
          1.second
        ).run(player)
      }

}

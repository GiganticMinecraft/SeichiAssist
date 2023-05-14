package com.github.unchama.seichiassist.subsystems.dragonnighttime

import cats.effect.{Concurrent, Timer}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application._
import com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances._
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectWriteApi
import com.github.unchama.seichiassist.subsystems.mana.ManaApi

object System {
  def backgroundProcess[F[_]: Concurrent: Timer, G[_]: ContextCoercion[*[_], F], Player](
    implicit fastDiggingEffectApi: FastDiggingEffectWriteApi[F, Player],
    manaApi: ManaApi[F, G, Player]
  ): F[Nothing] = {

    implicit val _notifiable: Notifiable[F] = SyncNotifiable[F]

    DragonNightTimeRoutine[F, G, Player]
  }
}

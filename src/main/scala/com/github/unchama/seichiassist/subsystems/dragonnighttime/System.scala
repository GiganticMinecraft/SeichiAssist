package com.github.unchama.seichiassist.subsystems.dragonnighttime

import cats.effect.{Sync, Timer}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application._
import com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances._
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectWriteApi
import org.bukkit.entity.Player

object System {
  def backgroundProcess[F[_] : Sync : Timer](fastDiggingEffectApi: FastDiggingEffectWriteApi[F, Player])
                                            (implicit ctx: RepeatingTaskContext): F[Nothing] = {
    implicit val _addableWithContext: AddableWithContext[F] = SyncAddableWithContext[F](fastDiggingEffectApi)
    implicit val _notifiable: Notifiable[F] = SyncNotifiable[F]

    DragonNightTimeRoutine()
  }
}

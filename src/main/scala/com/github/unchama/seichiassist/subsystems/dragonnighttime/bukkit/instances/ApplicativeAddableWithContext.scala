package com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances

import cats.Applicative
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application.AddableWithContext
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectWriteApi
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.{FastDiggingAmplifier, FastDiggingEffect, FastDiggingEffectCause}
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object ApplicativeAddableWithContext {

  import cats.implicits._

  import scala.concurrent.duration._
  import scala.jdk.CollectionConverters._

  private val effectToAdd = FastDiggingEffect(
    FastDiggingAmplifier(10.0),
    FastDiggingEffectCause.FromDragonNightTime
  )

  def apply[F[_] : Applicative](api: FastDiggingEffectWriteApi[F, Player]): AddableWithContext[F] = new AddableWithContext[F] {
    override val addEffect: F[Unit] =
      Bukkit.getOnlinePlayers
        .asScala
        .toList
        .traverse { player => api.addEffect(effectToAdd, 1.hour).run(player) }
        .as(())
  }
}

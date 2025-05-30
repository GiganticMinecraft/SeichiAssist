package com.github.unchama.seichiassist.subsystems.dragonnighttime

import cats.effect.{Concurrent, Timer}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.{GetConnectedPlayers, OnMinecraftServerThread}
import com.github.unchama.seichiassist.subsystems.dragonnighttime.application._
import com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.instances._
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingEffectWriteApi
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import org.bukkit.event.Listener
import com.github.unchama.seichiassist.subsystems.dragonnighttime.bukkit.JoinListener
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import cats.effect.Effect
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import org.bukkit.entity.Player

object System {
  def backgroundProcess[F[_]: Concurrent: Timer: OnMinecraftServerThread: GetConnectedPlayers[*[
    _
  ], org.bukkit.entity.Player], G[_]: ContextCoercion[*[_], F], Player](
    implicit fastDiggingEffectApi: FastDiggingEffectWriteApi[F, Player],
    manaApi: ManaApi[F, G, Player]
  ): F[Nothing] = {
    implicit val broadcastImpl: CanBroadcast[F] = SyncCanBroadcastOnBukkit[F]

    DragonNightTimeRoutine[F, G, Player]
  }

  def wired[F[_]: Timer: Effect](
    implicit fastDiggingEffectApi: FastDiggingEffectWriteApi[F, Player],
    effectEnvironment: EffectEnvironment
  ): Subsystem[F] = {
    new Subsystem[F] {
      override val listeners: Seq[Listener] = Seq(new JoinListener[F])
    }
  }
}

package com.github.unchama.seichiassist.subsystems.managedfly.bukkit.listeners

import com.github.unchama.runSync

import com.github.unchama.toIO

import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.ioRuntime

import cats.data.Kleisli
import cats.effect.SyncIO
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.subsystems.managedfly.application.{
  ActiveSessionReference,
  PlayerFlyStatusManipulation
}
import org.bukkit.entity.Player
import org.bukkit.event.player.{PlayerChangedWorldEvent, PlayerRespawnEvent}
import org.bukkit.event.{EventHandler, Listener}

class BukkitPlayerStatusChangeListener[F[_]: [f[_]] =>> ContextCoercion[f, cats.effect.IO], G[
  _
]: [g[_]] =>> ContextCoercion[g, SyncIO]](
  implicit
  sessionReferenceRepository: KeyedDataRepository[Player, ActiveSessionReference[F, G]],
  playerFlyStatusManipulation: PlayerFlyStatusManipulation[[a] =>> Kleisli[F, Player, a]]
) extends Listener {

  @EventHandler
  def onWorldChange(event: PlayerChangedWorldEvent): Unit = {
    val player = event.getPlayer

    val program = for {
      currentStatus <- sessionReferenceRepository(player)
        .getLatestFlyStatus
        .runSync[SyncIO]
        .toIO
      _ <- playerFlyStatusManipulation.synchronizeFlyStatus(currentStatus)(player).toIO
    } yield ()

    program.unsafeRunAndForget()
  }

  @EventHandler
  def onPlayerRespawn(event: PlayerRespawnEvent): Unit = {
    val player = event.getPlayer

    val program = for {
      currentStatus <- sessionReferenceRepository(player)
        .getLatestFlyStatus
        .runSync[SyncIO]
        .toIO
      _ <- playerFlyStatusManipulation.synchronizeFlyStatus(currentStatus)(player).toIO
    } yield ()

    program.unsafeRunAndForget()
  }

}

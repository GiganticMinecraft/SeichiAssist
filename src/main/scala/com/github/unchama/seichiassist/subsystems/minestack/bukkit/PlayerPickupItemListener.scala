package com.github.unchama.seichiassist.subsystems.minestack.bukkit

import com.github.unchama.toIO

import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.ioRuntime

import cats.effect.{Async, Sync}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.ApplicativeExtra.whenAOrElse
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.minestack.domain.{
  AutoCollectPreference,
  MineStackRepository
}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.{GameMode, Sound}
import cats.effect.Ref

class PlayerPickupItemListener[F[_]: Async: [f[_]] =>> ContextCoercion[f, cats.effect.IO]](
  implicit autoCollectPreferenceRepository: PlayerDataRepository[Ref[F, AutoCollectPreference]],
  mineStackRepository: MineStackRepository[F, Player, ItemStack]
) extends Listener {

  import cats.implicits._

  @EventHandler
  def onPickupMineStackItem(event: EntityPickupItemEvent): Unit = {
    event.getEntity match {
      case player: Player =>
        if (player.getGameMode != GameMode.SURVIVAL) return

        val item = event.getItem
        val itemStack = item.getItemStack

        val program = for {
          currentAutoMineStackState <-
            autoCollectPreferenceRepository(player).get
          intoSucceedItemStacksAndFailedItemStacks <- whenAOrElse(
            currentAutoMineStackState.isEnabled
          )(
            mineStackRepository.tryIntoMineStack(player, Vector(itemStack)),
            (Vector(itemStack), Vector.empty)
          )
          _ <- Sync[F]
            .delay {
              event.setCancelled(true)
              player.playSound(player.getLocation, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
              item.remove()
              if (SeichiAssist.DEBUG) {
                player.sendMessage(RED.toString + "pick:" + itemStack.toString)
              }
            }
            .whenA(intoSucceedItemStacksAndFailedItemStacks._2.nonEmpty)
        } yield ()

        program.toIO.unsafeRunAndForget()
      case _ => ()
    }
  }

}

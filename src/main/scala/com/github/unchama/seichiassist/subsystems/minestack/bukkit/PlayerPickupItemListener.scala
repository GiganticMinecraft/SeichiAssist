package com.github.unchama.seichiassist.subsystems.minestack.bukkit

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.minestack.domain.{MineStackSettings, TryIntoMineStack}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.{GameMode, Sound}

class PlayerPickupItemListener[F[_]: ConcurrentEffect](
  implicit mineStackSettingRepository: PlayerDataRepository[MineStackSettings[F]],
  tryIntoMineStack: TryIntoMineStack[F, Player, ItemStack]
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
          currentAutoMineStackState <- mineStackSettingRepository(player).currentState
          isSucceedTryIntoMineStack <- {
            if (currentAutoMineStackState)
              tryIntoMineStack(player, itemStack, itemStack.getAmount)
            else
              Sync[F].pure(false)
          }
          _ <- Sync[F].delay {
            event.setCancelled(true)
            player.playSound(player.getLocation, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
            item.remove()
            if (SeichiAssist.DEBUG) {
              player.sendMessage(RED.toString + "pick:" + itemStack.toString)
              player.sendMessage(RED.toString + "pickDurability:" + itemStack.getDurability)
            }
          }.whenA(isSucceedTryIntoMineStack)
        } yield ()

        program.toIO.unsafeRunAsyncAndForget()
    }
  }

}

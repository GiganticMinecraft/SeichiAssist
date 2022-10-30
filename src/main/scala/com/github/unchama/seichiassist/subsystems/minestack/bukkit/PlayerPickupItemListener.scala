package com.github.unchama.seichiassist.subsystems.minestack.bukkit

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackSettings
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectList
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.{GameMode, Sound}

class PlayerPickupItemListener[F[_]: ConcurrentEffect](
  implicit mineStackSettingRepository: PlayerDataRepository[MineStackSettings[F]],
  mineStackObjectList: MineStackObjectList[F, ItemStack],
  mineStackAPI: MineStackAPI[F, Player, ItemStack]
) extends Listener {

  import cats.implicits._

  @EventHandler
  def onPickupMineStackItem(event: EntityPickupItemEvent): Unit = {
    event.getEntity match {
      case player: Player =>
        if (player.getGameMode != GameMode.SURVIVAL) return

        val item = event.getItem
        val itemStack = item.getItemStack

        val tryIntoMineStackProgram = for {
          foundMineStackObject <- mineStackObjectList.findByItemStack(itemStack)
          _ <- {
            mineStackAPI.addStackedAmountOf(
              player,
              foundMineStackObject.get,
              itemStack.getAmount
            ) >> Sync[F].delay {
              event.setCancelled(true)
              player.playSound(player.getLocation, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
              item.remove()
              if (SeichiAssist.DEBUG) {
                player.sendMessage(RED.toString + "pick:" + itemStack.toString)
                player.sendMessage(RED.toString + "pickDurability:" + itemStack.getDurability)
              }
            }
          }.whenA(foundMineStackObject.nonEmpty)
        } yield ()

        val program = for {
          currentAutoMineStackState <- mineStackSettingRepository(player).currentState
          _ <- tryIntoMineStackProgram.whenA(currentAutoMineStackState)
        } yield ()

        program.toIO.unsafeRunAsyncAndForget()
    }
  }

}

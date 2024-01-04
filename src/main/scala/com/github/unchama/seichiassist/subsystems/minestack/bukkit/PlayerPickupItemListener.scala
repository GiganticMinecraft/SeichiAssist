package com.github.unchama.seichiassist.subsystems.minestack.bukkit

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.ApplicativeExtra.whenAOrElse
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.minestack.domain.{
  MineStackRepository,
  MineStackSettings
}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.{GameMode, Sound}
import com.github.unchama.util.bukkit.ItemStackUtil

import scala.jdk.CollectionConverters._

class PlayerPickupItemListener[F[_]: ConcurrentEffect, G[_]: ContextCoercion[*[_], F]](
  implicit mineStackSettingRepository: PlayerDataRepository[MineStackSettings[G, Player]],
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
          currentAutoMineStackState <- ContextCoercion(
            mineStackSettingRepository(player).isAutoCollectionTurnedOn
          )
          intoSucceedItemStacksAndFailedItemStacks <- whenAOrElse(currentAutoMineStackState)(
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

        program.toIO.unsafeRunAsyncAndForget()
      case _ => ()
    }
  }

  @EventHandler
  def onDropItem(event: BlockDropItemEvent): Unit = {
    val itemStacks =
      ItemStackUtil.amalgamate(event.getItems.asScala.map(_.getItemStack).toSeq).toVector
    val player = event.getPlayer

    val program = for {
      currentAutoMineStackState <- ContextCoercion(
        mineStackSettingRepository(player).isAutoCollectionTurnedOn
      )
      intoSucceedItemStacksAndFailedItemStacks <- whenAOrElse(currentAutoMineStackState)(
        mineStackRepository.tryIntoMineStack(player, itemStacks),
        (itemStacks, Vector.empty)
      )
      _ <- Sync[F].delay {
        event.setCancelled(true)
        intoSucceedItemStacksAndFailedItemStacks._1.foreach { itemStack =>
          player.getWorld.dropItemNaturally(player.getLocation, itemStack)
        }
      }
    } yield ()

    program.toIO.unsafeRunAsyncAndForget()
  }

}

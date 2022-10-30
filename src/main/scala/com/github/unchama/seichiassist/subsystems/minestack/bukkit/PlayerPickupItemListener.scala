package com.github.unchama.seichiassist.subsystems.minestack.bukkit

import cats.effect.Sync
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackSettings
import com.github.unchama.seichiassist.util.BreakUtil
import org.bukkit.ChatColor.RED
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{GameMode, Sound}

class PlayerPickupItemListener[F[_]: Sync](
  implicit mineStackSettingRepository: PlayerDataRepository[MineStackSettings[F]]
) extends Listener {

  import cats.implicits._

  @EventHandler
  def onPickupMineStackItem(event: EntityPickupItemEvent): Unit = {
    event.getEntity match {
      case player: Player =>
        if (player.getGameMode != GameMode.SURVIVAL) return

        for {
          currentAutoMineStackState <- mineStackSettingRepository(player).currentState

        } yield ()

        val item = event.getItem
        val itemstack = item.getItemStack

        if (SeichiAssist.DEBUG) {
          player.sendMessage(RED.toString + "pick:" + itemstack.toString)
          player.sendMessage(RED.toString + "pickDurability:" + itemstack.getDurability)
        }

        if (BreakUtil.tryAddItemIntoMineStack(player, itemstack)) {
          event.setCancelled(true)
          player.playSound(player.getLocation, Sound.ENTITY_ITEM_PICKUP, 1f, 1f)
          item.remove()
        }
    }
  }

}

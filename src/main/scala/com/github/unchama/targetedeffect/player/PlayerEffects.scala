package com.github.unchama.targetedeffect.player

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.concurrent.{BukkitSyncExecutionContext, Execution}
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

object PlayerEffects {
  val closeInventoryEffect: TargetedEffect[Player] = targetedeffect.delay(_.closeInventory())

  def openInventoryEffect(inventory: => Inventory)
                         (implicit context: BukkitSyncExecutionContext): TargetedEffect[Player] =
    Kleisli { player =>
      Execution.synchronously(IO {
        player.openInventory(inventory)
      })
    }

}

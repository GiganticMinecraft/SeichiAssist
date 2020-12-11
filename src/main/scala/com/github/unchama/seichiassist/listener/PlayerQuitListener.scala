package com.github.unchama.seichiassist.listener

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.task.PlayerDataSaveTask
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class PlayerQuitListener extends Listener {
  private val playerMap = SeichiAssist.playermap

  //プレイヤーがquitした時に実行
  @EventHandler(priority = EventPriority.LOWEST)
  def onplayerQuitEvent(event: PlayerQuitEvent): Unit = {
    val player = event.getPlayer

    SeichiAssist.instance.expBarSynchronization.desynchronizeFor(player)

    val playerData = playerMap(player.getUniqueId)

    playerData.updateOnQuit()

    IO {
      PlayerDataSaveTask.savePlayerData(playerData)
    }
      .start(PluginExecutionContexts.asyncShift)
      .unsafeRunAsync {
        case Left(error) => error.printStackTrace()
        case Right(_) =>
      }

    //不要なplayerdataを削除
    playerMap.remove(player.getUniqueId)
  }
}

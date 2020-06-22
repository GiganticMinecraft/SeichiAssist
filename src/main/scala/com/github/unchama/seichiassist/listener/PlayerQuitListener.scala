package com.github.unchama.seichiassist.listener

import cats.effect.IO
import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.database.DatabaseGateway
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
      val uuid = player.getUniqueId
      val pd = BuildAssist.playermap.get(uuid)
      if (pd.nonEmpty) {
        val data = pd.get
        if (data.flyflag) {
          val min = data.flytime
          SeichiAssist.databaseGateway.executeUpdate(s"INSERT INTO flying VALUES ('$uuid', $min)")
        }
      }
    }.unsafeRunAsync {
      case Left(error) => error.printStackTrace()
      case Right(_) =>
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

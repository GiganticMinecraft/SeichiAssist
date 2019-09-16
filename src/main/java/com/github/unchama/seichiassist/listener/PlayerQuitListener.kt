package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.task.savePlayerData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener : Listener {
  private val playerMap = SeichiAssist.playermap

  //プレイヤーがquitした時に実行
  @EventHandler(priority = EventPriority.LOWEST)
  fun onplayerQuitEvent(event: PlayerQuitEvent) {
    val player = event.player
    val uuid = player.uniqueId
    val playerData = playerMap[uuid]!!

    SeichiAssist.instance.expBarSynchronization.desynchronizeFor(player)

    playerData.updateOnQuit()
    playerData.activeskilldata.RemoveAllTask()

    GlobalScope.launch {
      savePlayerData(playerData)
    }

    //不要なplayerdataを削除
    playerMap.remove(uuid)
  }
}

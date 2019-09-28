package com.github.unchama.seichiassist.listener

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.task.PlayerDataSaving
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class PlayerQuitListener  extends  Listener {
  private val playerMap = SeichiAssist.playermap

  //プレイヤーがquitした時に実行
  @EventHandler(priority = EventPriority.LOWEST)
  def onplayerQuitEvent(event: PlayerQuitEvent) {
    val player = event.getPlayer
    val uuid = player.getUniqueId
    SeichiAssist.instance.expBarSynchronization.desynchronizeFor(player)

    val playerData = playerMap(uuid).ifNull { return }

    playerData.updateOnQuit()

    IO {
      PlayerDataSaving.savePlayerData(playerData)
    }.unsafeRunAsync { case Left(error) => error.printStackTrace() }

    //不要なplayerdataを削除
    playerMap.remove(uuid)
  }
}

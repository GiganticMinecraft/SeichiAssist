package com.github.unchama.buildassist.listener

import com.github.unchama.buildassist.TemporaryMutableBuildAssistPlayerData
import org.bukkit.event.player.{AsyncPlayerPreLoginEvent, PlayerQuitEvent}
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import java.util.UUID
import scala.collection.mutable

class TemporaryDataInitializer(
  dataMap: mutable.Map[UUID, TemporaryMutableBuildAssistPlayerData]
) extends Listener {

  @EventHandler(priority = EventPriority.LOWEST)
  def onPreLogin(event: AsyncPlayerPreLoginEvent): Unit = {
    val uuid = event.getUniqueId
    val newTemporaryData = new TemporaryMutableBuildAssistPlayerData()

    dataMap.addOne(uuid, newTemporaryData)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  def onQuit(event: PlayerQuitEvent): Unit = {
    val uuid = event.getPlayer.getUniqueId

    dataMap.remove(uuid)
  }

}

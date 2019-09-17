package com.github.unchama.seichiassist.bungee

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.task.savePlayerData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.*
import java.util.*

class BungeeReceiver(private val plugin: SeichiAssist) : PluginMessageListener {

  @Synchronized
  override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
    // ストリームの準備
    val stream = ByteArrayInputStream(message)
    val `in` = DataInputStream(stream)
    try {
      when (`in`.readUTF()) {
        "GetLocation" -> getLocation(`in`.readUTF(), `in`.readUTF(), `in`.readUTF())
        "UnloadPlayerData" -> savePlayerDataOnUpstreamRequest(`in`.readUTF())
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  private fun writtenMessage(vararg messages: String): ByteArray {
    val b = ByteArrayOutputStream()
    val out = DataOutputStream(b)

    try {
      messages.forEach { out.writeUTF(it) }
    } catch (e: IOException) {
      e.printStackTrace()
    }

    return b.toByteArray()
  }

  private fun savePlayerDataOnUpstreamRequest(playerName: String) {
    val player = Bukkit.getServer().getPlayer(playerName)

    try {
      val playerData = SeichiAssist.playermap[player.uniqueId]!!

      playerData.updateOnQuit()
      SeichiAssist.playermap.remove(player.uniqueId)

      GlobalScope.launch {
        savePlayerData(playerData)

        val message = writtenMessage("PlayerDataUnloaded", player.name)
        player.sendPluginMessage(plugin, "SeichiAssistBungee", message)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      player.kickPlayer("${player.name}のプレーヤーデータが正常にアンロードされませんでした。再接続した後サーバーを移動してください。")
      val message = writtenMessage("FailedToUnloadPlayerData", player.name)
      player.sendPluginMessage(plugin, "SeichiAssistBungee", message)
    }
  }

  private fun getLocation(servername: String, uuid: String, wanter: String) {
    val player = Bukkit.getServer().getPlayer(UUID.fromString(uuid))
    val playerData = SeichiAssist.playermap[UUID.fromString(uuid)]!!

    val message = writtenMessage(
        "GetLocation",
        wanter,
        "${player.name}: 整地Lv${playerData.level} (総整地量: ${String.format("%,d", playerData.totalbreaknum)})",
        "Server: $servername, World: ${player.world.name} (${player.location.blockX}, ${player.location.blockY}, ${player.location.blockZ})"
    )

    player.sendPluginMessage(plugin, "SeichiAssistBungee", message)
  }
}

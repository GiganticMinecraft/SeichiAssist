package com.github.unchama.seichiassist.bungee

import com.github.unchama.seichiassist.SeichiAssist

class BungeeReceiver(private val plugin: SeichiAssist) : PluginMessageListener {

  @Synchronized
  override def onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
    // ストリームの準備
    val stream = ByteArrayInputStream(message)
    val `in` = DataInputStream(stream)
    try {
      when (`in`.readUTF()) {
        "GetLocation" => getLocation(`in`.readUTF(), `in`.readUTF(), `in`.readUTF())
        "UnloadPlayerData" => savePlayerDataOnUpstreamRequest(`in`.readUTF())
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  private def writtenMessage(vararg messages: String): ByteArray {
    val b = ByteArrayOutputStream()
    val out = DataOutputStream(b)

    try {
      messages.forEach { out.writeUTF(it) }
    } catch (e: IOException) {
      e.printStackTrace()
    }

    return b.toByteArray()
  }

  private def savePlayerDataOnUpstreamRequest(playerName: String) {
    println("unloading data for $playerName by upstream request.")

    val player: Player? = Bukkit.getServer().getPlayer(playerName)

    try {
      val playerData = SeichiAssist.playermap[player!!.uniqueId]!!

      playerData.updateOnQuit()

      GlobalScope.launch {
        savePlayerData(playerData)
        SeichiAssist.playermap.remove(player.uniqueId)

        val message = writtenMessage("PlayerDataUnloaded", playerName)
        player.sendPluginMessage(plugin, "SeichiAssistBungee", message)
        println("successfully unloaded data for $playerName by upstream request.")
      }
    } catch (e: Exception) {
      e.printStackTrace()
      val message = writtenMessage("FailedToUnloadPlayerData", playerName)
      Bukkit.getOnlinePlayers().first().sendPluginMessage(plugin, "SeichiAssistBungee", message)

      player?.kickPlayer("${playerName}のプレーヤーデータが正常にアンロードされませんでした。再接続した後サーバーを移動してください。")
    }
  }

  private def getLocation(servername: String, uuid: String, wanter: String) {
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

package com.github.unchama.seichiassist.bungee

import com.github.unchama.seichiassist.SeichiAssist
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
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

  private fun getLocation(servername: String, uuid: String, wanter: String) {
    // 受信UUIDからプレイヤーを特定
    val player = Bukkit.getServer().getPlayer(UUID.fromString(uuid))
    // プレイヤーデータを取得
    val playerData = SeichiAssist.playermap[UUID.fromString(uuid)]!!

    val b = ByteArrayOutputStream()
    val out = DataOutputStream(b)
    try {
      // 返却データの生成
      out.writeUTF("GetLocation")
      out.writeUTF(wanter)
      out.writeUTF("${player.name}: 整地Lv${playerData.level} (総整地量: ${String.format("%,d", playerData.totalbreaknum)})")
      out.writeUTF("Server: $servername, World: ${player.world.name} (${player.location.blockX}, ${player.location.blockY}, ${player.location.blockZ})")
    } catch (e: IOException) {
      e.printStackTrace()
    }

    player.sendPluginMessage(plugin, "SeichiAssistBungee", b.toByteArray())
  }
}

package com.github.unchama.seichiassist.bungee

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, DataInputStream, DataOutputStream}
import java.util.UUID

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.task.PlayerDataSaveTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener

import scala.jdk.CollectionConverters._

class BungeeReceiver(private val plugin: SeichiAssist) extends PluginMessageListener {

  override def onPluginMessageReceived(channel: String, player: Player, message: Array[Byte]): Unit = synchronized {
    // ストリームの準備
    val stream = new ByteArrayInputStream(message)
    val in = new DataInputStream(stream)
    try {
      in.readUTF() match {
        case "GetLocation" => getLocation(in.readUTF(), in.readUTF(), in.readUTF())
        case "UnloadPlayerData" => savePlayerDataOnUpstreamRequest(in.readUTF())
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  private def savePlayerDataOnUpstreamRequest(playerName: String): Unit = {
    println(s"unloading data for $playerName by upstream request.")

    val player: Player = Bukkit.getServer.getPlayer(playerName)

    try {
      /**
       * 存在しないプレーヤーのデータアンロードが要求されたら
       * NPEをcatchさせたいためnullableに対するフィールドアクセスは意図的.
       */
      val uuid = player.getUniqueId
      val playerData = SeichiAssist.playermap(uuid)

      playerData.updateOnQuit()

      IO {
        PlayerDataSaveTask.savePlayerData(playerData)
        SeichiAssist.playermap.remove(uuid)

        val message = writtenMessage("PlayerDataUnloaded", playerName)
        player.sendPluginMessage(plugin, "SeichiAssistBungee", message)
        println(s"successfully unloaded data for $playerName by upstream request.")
      }.unsafeRunAsync {
        case Left(error) => error.printStackTrace()
        case Right(_) =>
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        val message = writtenMessage("FailedToUnloadPlayerData", playerName)
        Bukkit.getOnlinePlayers.asScala.head.sendPluginMessage(plugin, "SeichiAssistBungee", message)

        if (player != null) {
          player.kickPlayer(s"${playerName}のプレーヤーデータが正常にアンロードされませんでした。再接続した後サーバーを移動してください。")
        }
    }
  }

  private def getLocation(servername: String, uuid: String, wanter: String): Unit = {
    val player = Bukkit.getServer.getPlayer(UUID.fromString(uuid))
    val playerData = SeichiAssist.playermap(UUID.fromString(uuid))

    val message = writtenMessage(
      "GetLocation",
      wanter,
      s"${player.getName}: 整地Lv${playerData.level} (総整地量: ${String.format("%,d", playerData.totalbreaknum)})",
      s"Server: $servername, World: ${player.getWorld.getName} ",
      s"(${player.getLocation.getBlockX}, ${player.getLocation.getBlockY}, ${player.getLocation.getBlockZ})"
    )

    player.sendPluginMessage(plugin, "SeichiAssistBungee", message)
  }

  private def writtenMessage(messages: String*): Array[Byte] = {
    val b = new ByteArrayOutputStream()
    val out = new DataOutputStream(b)

    try {
      messages.foreach(out.writeUTF)
    } catch {
      case e: Exception => e.printStackTrace()
    }

    b.toByteArray
  }
}

package com.github.unchama.seichiassist.bungee

import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.messaging.PluginMessageListener

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, DataInputStream, DataOutputStream}
import java.util.UUID

class BungeeReceiver(private val plugin: SeichiAssist) extends PluginMessageListener {

  override def onPluginMessageReceived(
    channel: String,
    player: Player,
    message: Array[Byte]
  ): Unit = synchronized {
    // ストリームの準備
    val stream = new ByteArrayInputStream(message)
    val in = new DataInputStream(stream)
    try {
      in.readUTF() match {
        case "GetLocation" => getLocation(in.readUTF(), in.readUTF(), in.readUTF())
      }
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }

  private def getLocation(servername: String, uuid: String, wanter: String): Unit = {
    val player = Bukkit.getServer.getPlayer(UUID.fromString(uuid))

    val seichiAmountData =
      plugin.breakCountSystem.api.seichiAmountDataRepository(player).read.unsafeRunSync()
    val level = seichiAmountData.levelCorrespondingToExp.level
    val totalBreakAmount = seichiAmountData.expAmount.amount

    val message = writtenMessage(
      "GetLocation",
      wanter,
      s"${player.getName}: 整地Lv$level (総整地量: ${String.format("%,d", totalBreakAmount)})",
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

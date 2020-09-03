package com.github.unchama.targetedeffect.player

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.concurrent.{Execution, MinecraftServerThreadIOShift}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

object PlayerEffects {
  val closeInventoryEffect: TargetedEffect[Player] = TargetedEffect.delay(_.closeInventory())

  def openInventoryEffect(inventory: => Inventory)
                         (implicit ctx: MinecraftServerThreadIOShift): TargetedEffect[Player] =
    Kleisli { player =>
      // インベントリを開く操作はサーバースレッドでなければならない(Spigot 1.12.2)
      Execution.onServerMainThread(IO {
        player.openInventory(inventory)
      })
    }

  def connectToServerEffect(serverIdentifier: String)
                           (implicit ctx: MinecraftServerThreadIOShift): TargetedEffect[Player] =
    Kleisli { player =>
      // BungeeCordのサーバ移動はサーバスレッドでなければならない(Spigot 1.12.2)
      Execution.onServerMainThread(IO {

        import com.google.common.io.ByteStreams

        val byteArrayDataOutput = ByteStreams.newDataOutput()
        import byteArrayDataOutput._
        writeUTF("Connect")
        writeUTF(serverIdentifier)
        player.sendPluginMessage(SeichiAssist.instance, "BungeeCord", byteArrayDataOutput.toByteArray)
      })
    }

}

package com.github.unchama.targetedeffect.player

import cats.data.Kleisli
import cats.effect.{IO, SyncIO}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.targetedeffect.TargetedEffect
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

object PlayerEffects {

  def closeInventoryEffect(
    implicit onMainThread: OnMinecraftServerThread[IO]
  ): TargetedEffect[Player] = {
    Kleisli { player =>
      // インベントリを閉じる操作はサーバースレッドでなければならない(Spigot 1.18.2)
      onMainThread.runAction(SyncIO {
        player.closeInventory()
      })
    }
  }

  def openInventoryEffect(
    inventory: => Inventory
  )(implicit onMainThread: OnMinecraftServerThread[IO]): TargetedEffect[Player] =
    Kleisli { player =>
      // インベントリを開く操作はサーバースレッドでなければならない(Spigot 1.12.2)
      onMainThread
        .runAction(SyncIO {
          player.openInventory(inventory)
        })
        .as(())
    }

  def connectToServerEffect(
    serverIdentifier: String
  )(implicit onMainThread: OnMinecraftServerThread[IO]): TargetedEffect[Player] =
    Kleisli { player =>
      // BungeeCordのサーバ移動はサーバスレッドでなければならない(Spigot 1.12.2)
      onMainThread.runAction(SyncIO {

        import com.google.common.io.ByteStreams

        val byteArrayDataOutput = ByteStreams.newDataOutput()
        import byteArrayDataOutput._
        writeUTF("Connect")
        writeUTF(serverIdentifier)
        player.sendPluginMessage(
          SeichiAssist.instance,
          "BungeeCord",
          byteArrayDataOutput.toByteArray
        )
      })
    }

}

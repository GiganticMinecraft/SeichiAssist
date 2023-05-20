package com.github.unchama.seichiassist.util

import cats.effect.SyncIO
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player

trait PlayerSendable[-T, +F[_]] {
  def send(player: Player, content: T): F[Unit]
}

object PlayerSendable {

  implicit def forString[F[_]: OnMinecraftServerThread]: PlayerSendable[String, F] = {
    (player, content) =>
      OnMinecraftServerThread[F].runAction(SyncIO {
        player.sendMessage(content)
      })
  }

  implicit def forStringArray[F[_]: OnMinecraftServerThread]
    : PlayerSendable[Array[String], F] = { (player, content) =>
    OnMinecraftServerThread[F].runAction(SyncIO {
      player.sendMessage(content)
    })
  }

  implicit def forTextComponent[F[_]: OnMinecraftServerThread]
    : PlayerSendable[TextComponent, F] = { (player, content) =>
    OnMinecraftServerThread[F].runAction(SyncIO {
      player.spigot().sendMessage(content)
    })
  }
}

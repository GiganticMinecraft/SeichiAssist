package com.github.unchama.minecraft.bukkit.actions

import cats.effect.{Sync, SyncIO}
import com.github.unchama.minecraft.actions.{GetConnectedPlayers, OnMinecraftServerThread}
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class GetConnectedBukkitPlayers[F[_]: Sync: OnMinecraftServerThread]
    extends GetConnectedPlayers[F, Player] {

  import scala.jdk.CollectionConverters._

  override val now: F[List[Player]] = {
    // プレーヤーのリストはサーバースレッドと並行な読み込みができない(Spigot 1.12.2)
    OnMinecraftServerThread[F].runAction(SyncIO {
      Bukkit.getOnlinePlayers.asScala.toList
    })
  }

}

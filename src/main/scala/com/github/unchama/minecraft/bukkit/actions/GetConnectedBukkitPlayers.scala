package com.github.unchama.minecraft.bukkit.actions

import cats.effect.Sync
import com.github.unchama.minecraft.actions.{GetConnectedPlayers, MinecraftServerThreadShift}
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class GetConnectedBukkitPlayers[
  F[_] : Sync : MinecraftServerThreadShift
] extends GetConnectedPlayers[F, Player] {

  import cats.implicits._

  import scala.jdk.CollectionConverters._

  override val now: F[List[Player]] =
    MinecraftServerThreadShift[F].shift >> Sync[F].delay {
      // プレーヤーのリストはサーバースレッドと並行な読み込みができない(Spigot 1.12.2)
      Bukkit.getOnlinePlayers.asScala.toList
    }

}

package com.github.unchama.seichiassist.subsystems.autosave.bukkit.instances

import cats.effect.Sync
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.autosave.application.CanNotifySaves
import com.github.unchama.seichiassist.util.SendMessageEffect
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import com.github.unchama.minecraft.actions.GetConnectedPlayers

object SyncCanNotifyBukkitSaves {

  import cats.implicits._

  def apply[F[_]: Sync: OnMinecraftServerThread](
    implicit getConnectedPlayers: GetConnectedPlayers[F, Player]
  ): CanNotifySaves[F] =
    (message: String) =>
      for {
        _ <- SendMessageEffect.sendMessageToEveryoneIgnoringPreferenceM[String, F](message)
        _ <- Sync[F].delay(Bukkit.getLogger.info(message))
      } yield ()

}

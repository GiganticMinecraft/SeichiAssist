package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.bukkit

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.idletime.IdleTimeAPI
import com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain.{
  NameColorByIdleMinute,
  UpdatePlayerScreenName
}
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class BukkitUpdatePlayerScreenName[F[_]: Sync](
  implicit idleTimeAPI: IdleTimeAPI[F, Player],
  nameColorByIdleMinute: NameColorByIdleMinute[ChatColor]
) extends UpdatePlayerScreenName[F, Player] {

  import cats.implicits._

  override def updatePlayerNameColor(player: Player): F[Unit] =
    for {
      currentIdleMinute <- idleTimeAPI.currentIdleMinute(player)
    } yield {
      val currentDisplayName = player.getDisplayName
      val currentPlayerListName = player.getPlayerListName
      val newPlayerNameColor = nameColorByIdleMinute.getNameColor(currentIdleMinute)

      player.setDisplayName(newPlayerNameColor + currentDisplayName)
      player.setPlayerListName(newPlayerNameColor + currentPlayerListName)
    }

}

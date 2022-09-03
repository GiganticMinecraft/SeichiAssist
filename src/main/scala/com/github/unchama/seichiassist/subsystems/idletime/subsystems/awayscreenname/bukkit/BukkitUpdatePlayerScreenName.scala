package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.bukkit

import cats.effect.Sync
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.seichiassist.subsystems.idletime.domain.PlayerIdleMinuteRepository
import com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain.UpdatePlayerScreenName
import org.bukkit.entity.Player
import org.bukkit.ChatColor._

class BukkitUpdatePlayerScreenName[F[_]: Sync](
  implicit idleMinuteRepository: KeyedDataRepository[Player, PlayerIdleMinuteRepository[F]]
) extends UpdatePlayerScreenName[F, Player] {

  import cats.implicits._

  override def updatePlayerNameColor(player: Player): F[Unit] =
    for {
      currentIdleMinute <- idleMinuteRepository(player).currentIdleMinute
    } yield {
      val currentDisplayName = player.getDisplayName
      val currentPlayerListName = player.getPlayerListName
      val newPlayerNameColor =
        if (currentIdleMinute.minute >= 10) s"$DARK_GRAY"
        else if (currentIdleMinute.minute >= 3) s"$GRAY"
        else ""

      player.setDisplayName(newPlayerNameColor + currentDisplayName)
      player.setPlayerListName(newPlayerNameColor + currentPlayerListName)
    }

}

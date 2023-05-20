package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.bukkit

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
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
      newPlayerNameColor = nameColorByIdleMinute.getNameColor(currentIdleMinute)
      _ <- Sync[F].delay {
        /*
         * 表示名とマナをレベルと同期する
         * FIXME: ここの更新は、もともとPlayerDataRecalculationRoutine.scalaで行われていたもので、
         *  https://github.com/GiganticMinecraft/SeichiAssist/issues/1878
         *  を修正するためにやむを得ずここに記載している。
         *  この処理は本来ここにあるべきではなく、プレイヤー名関連の処理をリファクタリングする際に
         *  適切な場所へ配置するべきである。
         */
        val playerData = SeichiAssist.playermap(player.getUniqueId)
        val displayName = playerData.displayName()

        player.setDisplayName(s"$newPlayerNameColor$displayName")
        player.setPlayerListName(s"$newPlayerNameColor$displayName")
      }
    } yield ()

}

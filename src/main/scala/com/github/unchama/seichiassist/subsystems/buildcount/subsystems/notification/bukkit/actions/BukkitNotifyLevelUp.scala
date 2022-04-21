package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.bukkit.actions

import cats.Applicative
import cats.effect.Sync
import com.github.unchama.generic.Diff
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.{
  BuildAssistExpTable,
  BuildLevel
}
import com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions.NotifyLevelUp
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor.GOLD
import org.bukkit.entity.Player

object BukkitNotifyLevelUp {
  import cats.implicits._

  def apply[F[_]: Sync]: NotifyLevelUp[F, Player] =
    new NotifyLevelUp[F, Player] {
      override def ofBuildLevelTo(player: Player)(diff: Diff[BuildLevel]): F[Unit] = {
        val Diff(oldLevel, newLevel) = diff
        if (newLevel eqv BuildAssistExpTable.maxLevel) {
          val bukkitPlayer = player
          Sync[F].delay {
            Util.sendMessageToEveryoneIgnoringPreference(
              s"$GOLD${bukkitPlayer.getName}の建築レベルが最大Lvに到達したよ(`･ω･´)"
            )
          } // >> SendMinecraftMessage[F, Player].string(player, s"${GOLD}最大Lvに到達したよ(`･ω･´)") // >>
//            BroadcastMinecraftSound[F].playSound(Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 1.2f)
        } else if (oldLevel < newLevel)
//          SendMinecraftMessage[F, Player].string(
//            player,
//            s"${GOLD}ﾑﾑｯﾚﾍﾞﾙｱｯﾌﾟ∩( ・ω・)∩【建築Lv(${oldLevel.level})→建築Lv(${newLevel.level})】"
//          )
          Applicative[F].unit
        else
          Applicative[F].unit
      }
    }

}

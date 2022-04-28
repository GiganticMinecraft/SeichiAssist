package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.bukkit.actions

import cats.Applicative
import cats.effect.{IO, Sync, SyncIO}
import com.github.unchama.generic.Diff
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.{
  BuildAssistExpTable,
  BuildLevel
}
import com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions.NotifyLevelUp
import com.github.unchama.seichiassist.util.{PlayerSendable, Util}
import org.bukkit.ChatColor.GOLD
import org.bukkit.Sound
import org.bukkit.entity.Player

object BukkitNotifyLevelUp {

  import cats.implicits._
  import PlayerSendable.forString

  def apply[F[_]: OnMinecraftServerThread: Sync]: NotifyLevelUp[F, Player] = {
    new NotifyLevelUp[F, Player] {
      override def ofBuildLevelTo(player: Player)(diff: Diff[BuildLevel]): F[Unit] = {
        val Diff(oldLevel, newLevel) = diff
        if (newLevel eqv BuildAssistExpTable.maxLevel) {
          OnMinecraftServerThread[F].runAction(SyncIO {
            Util.sendMessageToEveryoneIgnoringPreference(
              s"$GOLD${player.getName}の建築レベルが最大Lvに到達したよ(`･ω･´)"
            )(forString[IO])
            player.sendMessage(s"${GOLD}最大Lvに到達したよ(`･ω･´)")
            Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 1.2f)
          })
        } else if (oldLevel < newLevel)
          Sync[F].delay {
            player.sendMessage(
              s"${GOLD}ﾑﾑｯﾚﾍﾞﾙｱｯﾌﾟ∩( ・ω・)∩【建築Lv(${oldLevel.level})→建築Lv(${newLevel.level})】"
            )
          }
        else
          Applicative[F].unit
      }
    }
  }

}

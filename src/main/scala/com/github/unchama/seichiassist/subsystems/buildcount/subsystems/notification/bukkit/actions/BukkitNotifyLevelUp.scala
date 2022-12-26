package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.bukkit.actions

import cats.effect.{IO, Sync}
import com.github.unchama.generic.Diff
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.{
  BuildAssistExpTable,
  BuildLevel
}
import com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions.NotifyLevelUp
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import com.github.unchama.seichiassist.util.SendMessageEffect.sendMessageToEveryoneIgnoringPreference
import com.github.unchama.seichiassist.util.SendSoundEffect.sendEverySound
import com.github.unchama.seichiassist.util.{LaunchFireWorksEffect, PlayerSendable}
import org.bukkit.ChatColor.GOLD
import org.bukkit.Sound
import org.bukkit.entity.Player

object BukkitNotifyLevelUp {

  import PlayerSendable.forString
  import cats.implicits._

  // TODO: BukkitNotifyLevelUpなのにdiffの展開やいつメッセージを出すかなどを扱うべきでない。
  def apply[F[_]: Sync: OnMinecraftServerThread: DiscordNotificationAPI]
    : NotifyLevelUp[F, Player] = {
    new NotifyLevelUp[F, Player] {
      override def ofBuildLevelTo(player: Player)(diff: Diff[BuildLevel]): F[Unit] = {
        val Diff(oldLevel, newLevel) = diff
        if (newLevel eqv BuildAssistExpTable.maxLevel) {
          val messageLevelMaxGlobal = s"$GOLD${player.getName}の建築レベルが最大Lvに到達したよ(`･ω･´)"
          val messageLevelMaxDiscord = s"${player.getName}の建築レベルが最大Lvに到達したよ(`･ω･´)"
          val messageLevelMaxPlayer = s"${GOLD}最大Lvに到達したよ(`･ω･´)"
          Sync[F].delay {
            sendMessageToEveryoneIgnoringPreference(messageLevelMaxGlobal)(forString[IO])
            player.sendMessage(messageLevelMaxPlayer)
            sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 1.2f)
          } >> LaunchFireWorksEffect.launchFireWorks[F](
            player.getLocation
          ) >> DiscordNotificationAPI[F].sendPlainText(messageLevelMaxDiscord)
        } else if (oldLevel < newLevel) {
          val messageLevelUp =
            s"${GOLD}ﾑﾑｯﾚﾍﾞﾙｱｯﾌﾟ∩( ・ω・)∩【建築Lv(${oldLevel.level})→建築Lv(${newLevel.level})】"
          Sync[F].delay {
            player.sendMessage(messageLevelUp)
          } >> LaunchFireWorksEffect.launchFireWorks[F](player.getLocation)
        } else
          Sync[F].unit
      }
    }
  }

}

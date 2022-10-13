package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.bukkit.actions

import cats.Applicative
import cats.effect.Effect.ops.toAllEffectOps
import cats.effect.{ConcurrentEffect, IO, Sync, SyncIO}
import com.github.unchama.generic.Diff
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.{
  BuildAssistExpTable,
  BuildLevel
}
import com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions.NotifyLevelUp
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import com.github.unchama.seichiassist.util.PlayerSendable
import com.github.unchama.seichiassist.util.SendMessageEffect.sendMessageToEveryoneIgnoringPreference
import com.github.unchama.seichiassist.util.SendSoundEffect.sendEverySound
import org.bukkit.ChatColor.GOLD
import org.bukkit.Sound
import org.bukkit.entity.Player

object BukkitNotifyLevelUp {

  import PlayerSendable.forString
  import cats.implicits._

  // TODO: BukkitNotifyLevelUpなのにdiffの展開やいつメッセージを出すかなどを扱うべきでない。
  // see also: https://github.com/GiganticMinecraft/SeichiAssist/blob/8ce9df9e4fc2da8f84d8aed4e2b74bbe95a61a02/src/main/scala/com/github/unchama/seichiassist/subsystems/breakcount/subsystems/notification/bukkit/actions/BukkitNotifyLevelUp.scala
  def apply[F[_]: OnMinecraftServerThread: ConcurrentEffect: DiscordNotificationAPI]
    : NotifyLevelUp[F, Player] = {
    new NotifyLevelUp[F, Player] {
      override def ofBuildLevelTo(player: Player)(diff: Diff[BuildLevel]): F[Unit] = {
        val Diff(oldLevel, newLevel) = diff
        if (newLevel eqv BuildAssistExpTable.maxLevel) {
          OnMinecraftServerThread[F].runAction(SyncIO {
            sendMessageToEveryoneIgnoringPreference(
              s"$GOLD${player.getName}の建築レベルが最大Lvに到達したよ(`･ω･´)"
            )(forString[IO])
            DiscordNotificationAPI[F]
              .sendPlainText(s"${player.getName}の建築レベルが最大Lvに到達したよ(`･ω･´)")
              .toIO
              .unsafeRunAsyncAndForget()
            player.sendMessage(s"${GOLD}最大Lvに到達したよ(`･ω･´)")
            sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 1.2f)
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

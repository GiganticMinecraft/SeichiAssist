package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.bukkit.actions

import cats.Applicative
import cats.effect.Effect.ops.toAllEffectOps
import cats.effect.{ConcurrentEffect, IO, Sync, SyncIO}
import com.github.unchama.generic.Diff
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.{BuildAssistExpTable, BuildLevel}
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions.NotifyLevelUp
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import com.github.unchama.seichiassist.util.{PlayerSendable, SendMessageEffect, SendSoundEffect}
import com.github.unchama.seichiassist.util.SendMessageEffect.sendMessageToEveryoneIgnoringPreference
import com.github.unchama.seichiassist.util.SendSoundEffect.sendEverySound
import org.bukkit.ChatColor.{GOLD, BOLD}
import org.bukkit.Sound
import org.bukkit.entity.Player

object BukkitNotifyLevelUp {

  import PlayerSendable.forString
  import cats.implicits._

  //FIXME ファイル名とやっていることが違うようになっているので修正するべき。
  //例えば、100万到達時の通知はLevelUp時の通知ではない
  //また、BukkitNotifyLevelUpなのにdiffの展開やいつメッセージを出すかなどを扱うべきでない。
  //see also: https://github.com/GiganticMinecraft/SeichiAssist/blob/8ce9df9e4fc2da8f84d8aed4e2b74bbe95a61a02/src/main/scala/com/github/unchama/seichiassist/subsystems/breakcount/subsystems/notification/bukkit/actions/BukkitNotifyLevelUp.scala
  def apply[F[_] : OnMinecraftServerThread : ConcurrentEffect : DiscordNotificationAPI]
  : NotifyLevelUp[F, Player] = {
    new NotifyLevelUp[F, Player] {
      override def ofBuildAmountTo(player: Player)(diff: Diff[BuildAmountData]): F[Unit] = {
        val Diff(oldBuildAmount, newBuildAmount) = diff
        val million = 1000000
        val oldBuildAmount1MUnit = (oldBuildAmount.expAmount.amount / million).toLong
        val newBuildAmount1MUnit = (newBuildAmount.expAmount.amount / million).toLong
        if (oldBuildAmount1MUnit < newBuildAmount1MUnit) {
          OnMinecraftServerThread[F].runAction(SyncIO {
            // ○億xxxx万文言の作成
            // 億桁の数値が0の場合は"x億"は表示せず、百万桁の数値が0の場合はxxxx万を表示しない
            val newBuildAmountDisplay100MUnit = newBuildAmount1MUnit / 100
            val newBuildAmountDisplay1MUnit = newBuildAmount1MUnit % 100
            val newBuildAmountDisplay = (
              if (newBuildAmountDisplay100MUnit > 0) {
                s"${newBuildAmountDisplay100MUnit}億"
              } else {
                ""
              }) + (
              if (newBuildAmountDisplay1MUnit > 0) {
                s"${newBuildAmountDisplay1MUnit}00万"
              } else {
                ""
              })

            val notificationMessage =
              s"${player.getName}の総建築量が${newBuildAmountDisplay}に到達しました！"
            SendMessageEffect.sendMessageToEveryoneIgnoringPreference(
              s"$GOLD$BOLD$notificationMessage"
            )(forString[IO])
            DiscordNotificationAPI[F]
              .sendPlainText(notificationMessage)
              .toIO
              .unsafeRunAsyncAndForget()
            SendSoundEffect.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 1.2f)
          })
        } else Applicative[F].unit
      }

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

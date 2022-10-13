package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.bukkit.actions

import cats.Applicative
import cats.effect.Effect.ops.toAllEffectOps
import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.generic.Diff
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions.NotifyBuildAmountThreshold
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import com.github.unchama.seichiassist.util.{PlayerSendable, SendMessageEffect, SendSoundEffect}
import org.bukkit.ChatColor.{GOLD, BOLD}
import org.bukkit.Sound
import org.bukkit.entity.Player

object BukkitNotifyBuildAmountThreshold {

  import PlayerSendable.forString

  // TODO: BukkitNotifyLevelUpなのにdiffの展開やいつメッセージを出すかなどを扱うべきでない。
  // see also: https://github.com/GiganticMinecraft/SeichiAssist/blob/8ce9df9e4fc2da8f84d8aed4e2b74bbe95a61a02/src/main/scala/com/github/unchama/seichiassist/subsystems/breakcount/subsystems/notification/bukkit/actions/BukkitNotifyLevelUp.scala
  def apply[F[_]: OnMinecraftServerThread: ConcurrentEffect: DiscordNotificationAPI]
    : NotifyBuildAmountThreshold[F, Player] = {
    new NotifyBuildAmountThreshold[F, Player] {
      override def ofBuildAmountTo(player: Player)(diff: Diff[BuildAmountData]): F[Unit] = {
        val Diff(oldBuildAmount, newBuildAmount) = diff
        val million = 1000000
        val oldBuildAmountOneMillionUnit = (oldBuildAmount.expAmount.amount / million).toLong
        val newBuildAmountOneMillionUnit = (newBuildAmount.expAmount.amount / million).toLong
        if (oldBuildAmountOneMillionUnit < newBuildAmountOneMillionUnit) {
          OnMinecraftServerThread[F].runAction(SyncIO {
            // ○億xxxx万文言の作成
            // 億の位の数値が0の場合は"x億"は表示せず、百万の位の数値が0の場合はxxxx万を表示しない
            val newBuildAmountDisplayOneHundredMillionUnit = newBuildAmountOneMillionUnit / 100
            val newBuildAmountDisplayOneMillionUnit = newBuildAmountOneMillionUnit % 100
            val newBuildAmountDisplayOneHundredMillionUnitString =
              if (newBuildAmountDisplayOneHundredMillionUnit > 0) {
                s"${newBuildAmountDisplayOneHundredMillionUnit}億"
              } else {
                ""
              }
            val newBuildAmountDisplayOneMillionUnitString =
              if (newBuildAmountDisplayOneMillionUnit > 0) {
                s"${newBuildAmountDisplayOneMillionUnit}00万"
              } else {
                ""
              }
            val newBuildAmountDisplay =
              newBuildAmountDisplayOneHundredMillionUnitString + newBuildAmountDisplayOneMillionUnitString

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
    }
  }
}

package com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.bukkit.actions

import cats.Applicative
import cats.effect.{ConcurrentEffect, IO, Sync}
import cats.implicits.catsSyntaxFlatMapOps
import com.github.unchama.generic.{Diff, OptionExtra}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.buildcount.subsystems.notification.application.actions.NotifyBuildAmountThreshold
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import com.github.unchama.seichiassist.util.{PlayerSendable, SendMessageEffect, SendSoundEffect}
import org.bukkit.ChatColor.{BOLD, GOLD}
import org.bukkit.Sound
import org.bukkit.entity.Player

object BukkitNotifyBuildAmountThreshold {

  import PlayerSendable.forString

  // TODO: BukkitNotifyLevelUpなのにdiffの展開やいつメッセージを出すかなどを扱うべきでない。
  def apply[F[_]: Sync: DiscordNotificationAPI]
    : NotifyBuildAmountThreshold[F, Player] = {
    new NotifyBuildAmountThreshold[F, Player] {
      override def ofBuildAmountTo(player: Player)(diff: Diff[BuildAmountData]): F[Unit] = {
        val Diff(oldBuildAmount, newBuildAmount) = diff
        val million = 1000000
        val oldBuildAmountOneMillionUnit = (oldBuildAmount.expAmount.amount / million).toInt
        val newBuildAmountOneMillionUnit = (newBuildAmount.expAmount.amount / million).toInt
        if (oldBuildAmountOneMillionUnit < newBuildAmountOneMillionUnit) {
          // ○億xxxx万文言の作成
          // 億の位の数値が0の場合は"x億"は表示せず、百万の位の数値が0の場合はxxxx万を表示しない
          val newBuildAmountDisplayOneHundredMillionUnit = newBuildAmountOneMillionUnit / 100
          val newBuildAmountDisplayOneMillionUnit = newBuildAmountOneMillionUnit % 100
          val newBuildAmountDisplayOneHundredMillionUnitString =
            OptionExtra.getOrDefault(newBuildAmountDisplayOneHundredMillionUnit > 0)(
              Some(s"${newBuildAmountDisplayOneHundredMillionUnit}億"),
              ""
            )
          val newBuildAmountDisplayOneMillionUnitString =
            OptionExtra.getOrDefault(newBuildAmountDisplayOneMillionUnit > 0)(
              Some(s"${newBuildAmountDisplayOneMillionUnit}00万"),
              ""
            )
          val newBuildAmountDisplay =
            newBuildAmountDisplayOneHundredMillionUnitString + newBuildAmountDisplayOneMillionUnitString
          val notificationMessage =
            s"${player.getName}の総建築量が${newBuildAmountDisplay}に到達しました！"

          Sync[F].delay {
            SendMessageEffect.sendMessageToEveryoneIgnoringPreference(
              s"$GOLD$BOLD$notificationMessage"
            )(forString[IO])
            SendSoundEffect.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 1.2f)
          } >> DiscordNotificationAPI[F].sendPlainText(notificationMessage)
        } else Sync[F].unit
      }
    }
  }
}

package com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification.bukkit.actions

import cats.Applicative
import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, IO, Sync, SyncIO}
import com.github.unchama.generic.Diff
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.{
  SeichiLevel,
  SeichiStarLevel
}
import com.github.unchama.seichiassist.subsystems.breakcount.subsystems.notification.application.actions.NotifyLevelUp
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import com.github.unchama.seichiassist.util.{
  LaunchFireWorksEffect,
  SendMessageEffect,
  SendSoundEffect
}
import org.bukkit.ChatColor.{BOLD, GOLD}
import org.bukkit.Sound
import org.bukkit.entity.Player
import com.github.unchama.seichiassist.util.PlayerSendable._

//FIXME ファイル名とやっていることが違うようになっているので修正するべき。
//例えば、10億の倍数到達時の通知はLevelUp時の通知ではない
//また、BukkitNotifyLevelUpなのにdiffの展開やいつメッセージを出すかなどを扱うべきでない。
object BukkitNotifyLevelUp {

  import cats.implicits._

  def apply[F[_]: OnMinecraftServerThread: ConcurrentEffect: DiscordNotificationAPI]
    : NotifyLevelUp[F, Player] =
    new NotifyLevelUp[F, Player] {
      override def ofSeichiAmountTo(player: Player)(diff: Diff[SeichiAmountData]): F[Unit] = {
        val Diff(oldBreakAmount, newBreakAmount) = diff
        val tenBillion = 1000000000
        val nextTenBillion =
          tenBillion * (oldBreakAmount.expAmount.amount / tenBillion + 1).toLong
        if (
          oldBreakAmount.expAmount.amount < nextTenBillion && newBreakAmount
            .expAmount
            .amount >= nextTenBillion
        ) {
          // TODO: ここのSyncIOを剥がせそう
          OnMinecraftServerThread[F].runAction(SyncIO {
            val notificationMessage =
              s"${player.getName}の総整地量が${(newBreakAmount.expAmount.amount / 100000000).toInt}億に到達しました！"

            SendMessageEffect
              .sendMessageToEveryoneIgnoringPreferenceIO(s"$GOLD$BOLD$notificationMessage")(
                forString[IO]
              )
              .unsafeRunAsyncAndForget()
            DiscordNotificationAPI[F]
              .sendPlainText(notificationMessage)
              .toIO
              .unsafeRunAsyncAndForget()
            SendSoundEffect.sendEverySound(Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.2f)
          })
        } else Applicative[F].unit
      }

      override def ofSeichiLevelTo(player: Player)(diff: Diff[SeichiLevel]): F[Unit] = {
        val Diff(oldLevel, newLevel) = diff

        val titleMessage = s"【Lv${oldLevel.level}→Lv${newLevel.level}】"
        val subTitleMessage = s"${GOLD}ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww"

        if (oldLevel < newLevel)
          Sync[F].delay {
            player.sendTitle(titleMessage, subTitleMessage, 1, 20 * 5, 1)
            player.sendMessage(s"$subTitleMessage$titleMessage")
          } >>
            LaunchFireWorksEffect.launchFireWorks[F](player.getLocation)
        else Applicative[F].unit
      }

      override def ofSeichiStarLevelTo(player: Player)(diff: Diff[SeichiStarLevel]): F[Unit] = {
        val Diff(oldStars, newStars) = diff
        val titleMessage = {
          if (oldStars == SeichiStarLevel.zero) {
            s"【Lv199→Lv200(☆${newStars.level})】"
          } else {
            s"【Lv200(☆${oldStars.level})→Lv200(☆${newStars.level})】"
          }
        }
        val subTitleMessage = s"$GOLD★☆★ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww★☆★"

        if (oldStars < newStars) Sync[F].delay {
          player.sendTitle(titleMessage, subTitleMessage, 1, 20 * 5, 1)
          player.sendMessage(s"$subTitleMessage$titleMessage")
        } >> LaunchFireWorksEffect.launchFireWorks[F](player.getLocation)
        else Applicative[F].unit
      }
    }

}

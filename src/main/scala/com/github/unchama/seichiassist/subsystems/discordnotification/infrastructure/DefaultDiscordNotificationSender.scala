package com.github.unchama.seichiassist.subsystems.discordnotification.infrastructure

import cats.effect.LiftIO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import io.chrisdavenport.log4cats.Logger

/**
 * この実装は[[sendPlainText]]が呼ばれるたびに警告をロガーに流す以外は何もしない。
 */
final class DefaultDiscordNotificationSender[F[_]: Logger: LiftIO]
    extends DiscordNotificationAPI[F] {
  override def sendPlainText(message: String): F[Unit] = {
    SeichiAssist
      .instance
      .loggerF
      .warn("Discordへの送信が試みられましたが、URLが無効、もしくは与えられていません。コンフィグを確認してください。")
      .to
  }
}

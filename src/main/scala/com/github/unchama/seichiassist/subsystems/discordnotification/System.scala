package com.github.unchama.seichiassist.subsystems.discordnotification

import cats.effect.{ContextShift, LiftIO, Sync}
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.discordnotification.infrastructure.{
  DefaultDiscordNotificationSender,
  WebhookDiscordNotificationSender
}

trait System[F[_]] extends Subsystem[F] {
  implicit val globalNotification: DiscordNotificationAPI[F]
}

object System {
  def wired[F[_]: Sync: ContextShift: LiftIO](configuration: SystemConfiguration): System[F] =
    new System[F] {
      implicit override val globalNotification: DiscordNotificationAPI[F] = {
        WebhookDiscordNotificationSender
          .tryCreate(configuration.webhookUrl)
          .getOrElse(new DefaultDiscordNotificationSender)
      }
    }
}

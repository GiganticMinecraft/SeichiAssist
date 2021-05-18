package com.github.unchama.seichiassist.subsystems.discordnotification

import cats.effect.{ContextShift, Sync}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.discordnotification.infrastructure.WebhookDiscordNotificationSender

trait System[F[_]] extends Subsystem[F] {
  implicit val globalNotification: DiscordNotificationAPI[F]
}

object System {
  private val seichiAssistConfig = SeichiAssist.seichiAssistConfig

  def wired[F[_] : Sync : ContextShift]: System[F] = new System[F] {
    implicit override val globalNotification: DiscordNotificationAPI[F] =
      new WebhookDiscordNotificationSender[F](seichiAssistConfig.getWebhookUrl)
  }
}

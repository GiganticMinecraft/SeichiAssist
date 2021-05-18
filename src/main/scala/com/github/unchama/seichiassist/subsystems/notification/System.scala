package com.github.unchama.seichiassist.subsystems.notification

import cats.effect.{ContextShift, Sync}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.notification.service.GlobalNotificationSender

trait System[F[_]] extends Subsystem[F] {
  implicit val globalNotification: GlobalNotificationAPI[F]
}

object System {
  private val seichiAssistConfig = SeichiAssist.seichiAssistConfig

  def wired[F[_] : Sync : ContextShift]: System[F] = new System[F] {
    implicit override val globalNotification: GlobalNotificationAPI[F] =
      new GlobalNotificationSender[F](seichiAssistConfig.getWebhookUrl)
  }
}

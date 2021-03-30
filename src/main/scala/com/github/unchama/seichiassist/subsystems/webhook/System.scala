package com.github.unchama.seichiassist.subsystems.webhook

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.webhook.service.{CanSendToWebhook, WebhookSender}

object System {
  private val seichiAssistConfig = SeichiAssist.seichiAssistConfig
  def wired[F[_] : Sync]: Subsystem[F] = new Subsystem[F] {
    val gatewayForGiganticBerserk: CanSendToWebhook[F] = new WebhookSender[F](seichiAssistConfig.getWebhookUrlForGiganticBerserk)
    val gatewayForAssault: CanSendToWebhook[F] = new WebhookSender[F](seichiAssistConfig.getWebhookUrlForAssault)
  }
}

package com.github.unchama.seichiassist.subsystems.webhook

import cats.effect.Sync
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.webhook.service.{CanSendToWebhook, WebhookSender}

object System {
  private val seichiAssistConfig = SeichiAssist.seichiAssistConfig
  def wired[F[_] : Sync]: Subsystem[F] = new Subsystem[F] {
    import com.github.unchama.generic.tag.tag
    private val GiganticBerserkWebhookTag = tag.apply[GatewayKind.ForGiganticBerserk]
    private val AssaultWebhookTag = tag.apply[GatewayKind.ForAssault]
    implicit val gatewayForGiganticBerserk: GiganticBerserkWebhookGateway[F] = GiganticBerserkWebhookTag(
      new WebhookSender[F](seichiAssistConfig.getWebhookUrlForGiganticBerserk)
    )

    implicit val gatewayForAssault: AssaultWebhookGateway[F] = AssaultWebhookTag(
      new WebhookSender[F](seichiAssistConfig.getWebhookUrlForAssault)
    )
  }

  private sealed trait WebhookKind

  object GatewayKind {
    private[System] trait ForGiganticBerserk extends WebhookKind

    private[System] trait ForAssault extends WebhookKind
  }

  import com.github.unchama.generic.tag.tag.@@

  private type Tagged[F[_], Tag] = CanSendToWebhook[F] @@ Tag
  type GiganticBerserkWebhookGateway[F[_]] = Tagged[F, GatewayKind.ForGiganticBerserk]
  type AssaultWebhookGateway[F[_]] = Tagged[F, GatewayKind.ForAssault]
}

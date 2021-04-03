package com.github.unchama.seichiassist.subsystems.webhook

import cats.effect.Sync
import com.github.unchama.generic.tag.tag.@@
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.webhook.System.GatewayKind.{ForAssault, ForGiganticBerserk}
import com.github.unchama.seichiassist.subsystems.webhook.service.{CanSendToWebhook, WebhookSender}

trait System[F[_]] extends Subsystem[F] {
  val api: WebhookWriteAPI[F]
}

object System {
  private val seichiAssistConfig = SeichiAssist.seichiAssistConfig
  def wired[F[_] : Sync]: System[F] = new System[F] {
    import com.github.unchama.generic.tag.tag

    implicit val gatewayForGiganticBerserk: GiganticBerserkWebhookGateway[F] = tag[ForGiganticBerserk][WebhookSender[F]](
      new WebhookSender[F](seichiAssistConfig.getWebhookUrlForGiganticBerserk)
    )

    implicit val gatewayForAssault: AssaultWebhookGateway[F] = tag[ForAssault][WebhookSender[F]](
      new WebhookSender[F](seichiAssistConfig.getWebhookUrlForAssault)
    )

    override val api: WebhookWriteAPI[F] = new WebhookWriteAPI[F] {
      override def sendGiganticBerserkNotification(message: String): F[Either[Exception, Unit]] = gatewayForGiganticBerserk.send(message)

      override def sendAssaultNotification(message: String): F[Either[Exception, Unit]] = gatewayForAssault.send(message)
    }
  }

  private sealed trait WebhookKind

  object GatewayKind {
    private[System] trait ForGiganticBerserk extends WebhookKind

    private[System] trait ForAssault extends WebhookKind
  }

  import com.github.unchama.generic.tag.tag.@@

  private type Tagged[F[_], Tag] = WebhookSender[F] @@ Tag
  type GiganticBerserkWebhookGateway[F[_]] = Tagged[F, GatewayKind.ForGiganticBerserk]
  type AssaultWebhookGateway[F[_]] = Tagged[F, GatewayKind.ForAssault]
}

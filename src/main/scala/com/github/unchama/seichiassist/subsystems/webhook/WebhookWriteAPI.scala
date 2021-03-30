package com.github.unchama.seichiassist.subsystems.webhook

trait WebhookWriteAPI[F[_]] {
  def sendGiganticBerserkNotification(message: String): F[Either[Exception, Unit]]

  def sendAssaultNotification(message: String): F[Either[Exception, Unit]]
}

object WebhookWriteAPI {
  def apply[F[_] : WebhookWriteAPI]: WebhookWriteAPI[F] = implicitly[WebhookWriteAPI[F]]
}
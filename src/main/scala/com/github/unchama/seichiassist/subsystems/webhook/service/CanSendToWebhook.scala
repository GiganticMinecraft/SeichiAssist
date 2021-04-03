package com.github.unchama.seichiassist.subsystems.webhook.service

import simulacrum.typeclass

@typeclass trait CanSendToWebhook[F[_]] {
  def send(message: String): F[Either[Exception, Unit]]
}

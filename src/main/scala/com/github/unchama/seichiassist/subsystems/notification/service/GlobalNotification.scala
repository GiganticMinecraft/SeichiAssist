package com.github.unchama.seichiassist.subsystems.notification.service

import simulacrum.typeclass

/**
 * サーバーの全てのプレイヤー、及びDiscordのWebhookチャンネルにメッセージを送信する。
 */
@typeclass trait GlobalNotification[F[_]] {
  def send(message: String): F[Either[Exception, Unit]]
}

object GlobalNotification {
  def apply[F[_] : GlobalNotification]: GlobalNotification[F] =
    implicitly[GlobalNotification[F]]
}
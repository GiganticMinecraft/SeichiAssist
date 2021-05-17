package com.github.unchama.seichiassist.subsystems.notification

/**
 * サーバーの全てのプレイヤー、及びDiscordのWebhookチャンネルにメッセージを送信する。
 */
trait GlobalNotificationAPI[F[_]] {

  def send(message: String): F[Either[Exception, Unit]]

}

object GlobalNotificationAPI {

  def apply[F[_]](implicit ev: GlobalNotificationAPI[F]): GlobalNotificationAPI[F] = ev

}

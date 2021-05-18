package com.github.unchama.seichiassist.subsystems.discordnotification

/**
 * サーバーの全てのプレイヤー、及びDiscordのWebhookチャンネルにメッセージを送信する。
 */
trait DiscordNotificationAPI[F[_]] {

  def send(message: String): F[Unit]

}

object DiscordNotificationAPI {

  def apply[F[_]](implicit ev: DiscordNotificationAPI[F]): DiscordNotificationAPI[F] = ev

}

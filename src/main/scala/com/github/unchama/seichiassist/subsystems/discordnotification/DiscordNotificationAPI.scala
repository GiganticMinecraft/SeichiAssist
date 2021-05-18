package com.github.unchama.seichiassist.subsystems.discordnotification

/**
 * Discordのチャンネルにメッセージを送信する。
 *
 * 送信先のチャンネルはサーバー全員が見れるような物を想定してよく、実質的に全体通知と考えて良い。
 */
trait DiscordNotificationAPI[F[_]] {

  def send(message: String): F[Unit]

}

object DiscordNotificationAPI {

  def apply[F[_]](implicit ev: DiscordNotificationAPI[F]): DiscordNotificationAPI[F] = ev

}

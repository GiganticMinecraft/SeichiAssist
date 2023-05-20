package com.github.unchama.seichiassist.subsystems.discordnotification

/**
 * Discordのチャンネルにメッセージを送信する。
 *
 * 送信先のチャンネルはサーバー全員が見れるような物を想定してよく、実質的に全体通知と考えて良い。
 */
trait DiscordNotificationAPI[F[_]] {

  /**
   * 引数に与えられたメッセージを表示するペイロードをDiscordに送信する作用を返す。
   * 与えられた引数をDiscordが解釈できる形に加工するのはtraitの実装側の責務である。
   * @param message message to display
   * @return requesting effect
   */
  def sendPlainText(message: String): F[Unit]

}

object DiscordNotificationAPI {

  def apply[F[_]](implicit ev: DiscordNotificationAPI[F]): DiscordNotificationAPI[F] = ev

}

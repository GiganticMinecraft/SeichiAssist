package com.github.unchama.seichiassist.subsystems.webhook.service

import cats.effect.Sync

import java.net.{HttpURLConnection, URL}
import java.nio.charset.StandardCharsets
import scala.util.Using
import scala.util.chaining.scalaUtilChainingOps

class WebhookSender[F[_]: Sync](webhookURL: String) extends CanSendToWebhook[F] {
  assert(webhookURL.nonEmpty, {
    "WebhookSenderのURLに空文字列が指定されました。コンフィグを確認してください。"
  })

  private val parsedURL = new URL(webhookURL)
  override def send(message: String): F[Either[Exception, Unit]] = Sync[F].delay {
    val json = s"""{"content":"$message"}"""
    parsedURL.openConnection().asInstanceOf[HttpURLConnection].pipe { con =>
      con.addRequestProperty("Content-Type", "application/json; charset=utf-8")
      // User-AgentがDiscordBotでない場合403が返却されるため
      con.addRequestProperty("User-Agent", "DiscordBot")
      con.setDoOutput(true)
      con.setRequestMethod("POST")
      // HTTP 411回避？
      con.setRequestProperty("Content-Length", json.length.toString)
      con.connect()

      Using.resource(con.getOutputStream) { os =>
        os.write(json.getBytes(StandardCharsets.UTF_8))
      }

      con.getResponseCode match {
        case HttpURLConnection.HTTP_OK | HttpURLConnection.HTTP_NO_CONTENT => Right(())
        case code @ _ => Left(new Exception(s"Discord Webhook: Bad Response Code: $code with $webhookURL"))
      }
    }
  }
}

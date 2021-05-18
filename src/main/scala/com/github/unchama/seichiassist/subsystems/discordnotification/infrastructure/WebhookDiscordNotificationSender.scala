package com.github.unchama.seichiassist.subsystems.discordnotification.infrastructure

import cats.effect.{ContextShift, Sync}
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import org.bukkit.Bukkit

import java.io.IOException
import java.net.{HttpURLConnection, URL}
import java.nio.charset.StandardCharsets
import scala.util.Using
import scala.util.chaining.scalaUtilChainingOps

class WebhookDiscordNotificationSender[F[_]: Sync: ContextShift](webhookURL: String) extends DiscordNotificationAPI[F] {
  assert(
    webhookURL.nonEmpty,
    "GlobalNotificationSenderのURLに空文字列が指定されました。コンフィグを確認してください。"
  )

  import cats.implicits._

  private val parsedURL = new URL(webhookURL)
  override def send(message: String): F[Unit] =
    for {
      _ <- ContextShift[F].shift
      responseCode <- Sync[F].delay {
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

          con.getResponseCode
        }
      }
      _ <- responseCode match {
        case HttpURLConnection.HTTP_OK | HttpURLConnection.HTTP_NO_CONTENT => Sync[F].unit
        case code @ _ => Sync[F].raiseError {
          new IOException(s"GlobalNotificationSender: Bad Response Code: $code with $webhookURL")
        }
      }
    } yield ()
}

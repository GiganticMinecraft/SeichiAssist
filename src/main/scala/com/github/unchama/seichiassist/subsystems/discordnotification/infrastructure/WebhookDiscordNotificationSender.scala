package com.github.unchama.seichiassist.subsystems.discordnotification.infrastructure

import cats.effect.{ContextShift, Sync}
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import io.chrisdavenport.log4cats.Logger

import java.io.IOException
import java.net.{HttpURLConnection, MalformedURLException, URL}
import java.nio.charset.StandardCharsets
import scala.util.Using
import scala.util.chaining.scalaUtilChainingOps

class WebhookDiscordNotificationSender[F[_]: Sync: ContextShift] private (webhookURL: String)
    extends DiscordNotificationAPI[F] {
  assert(webhookURL.nonEmpty, "GlobalNotificationSenderのURLに空文字列が指定されました。コンフィグを確認してください。")

  import cats.implicits._

  private val parsedURL = new URL(webhookURL)
  override def sendPlainText(message: String): F[Unit] =
    for {
      _ <- ContextShift[F].shift
      responseCode <- Sync[F].delay {
        import io.circe.generic.auto._
        import io.circe.syntax._
        val markdownSafeMessage = message
          .replace("\\", "\\\\")
          .replace("_", "\\_")
          .replace("*", "\\*")
          .replace("`", "\\`")
          .replace("|", "\\|")
          .replace("@", "\\@")
          .replace("~", "\\~")
          .replace(":", "\\:")

        val json =
          WebhookDiscordNotificationSender.PlainMessage(markdownSafeMessage).asJson.noSpaces

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
        case code @ _ =>
          Sync[F].raiseError {
            new IOException(
              s"GlobalNotificationSender: Bad Response Code: $code with $webhookURL"
            )
          }
      }
    } yield ()
}

object WebhookDiscordNotificationSender {

  /**
   * [[WebhookDiscordNotificationSender]] を作成することを試みる。
   * @param webhookURL
   *   Discordに送信されるwebhookのURL
   * @tparam F
   *   文脈
   * @return
   *   初期化に成功した場合はSome、初期化中に特定の例外が送出された場合はNone。マスクされない例外が送出されたときは、再送出する。
   */
  def tryCreate[F[_]: Sync: ContextShift: Logger](
    webhookURL: String
  ): Option[WebhookDiscordNotificationSender[F]] = {
    try {
      Some(new WebhookDiscordNotificationSender[F](webhookURL))
    } catch {
      case _: MalformedURLException => None
      case _: AssertionError        => None
    }
  }

  // This class is for circe's serialization.
  private case class PlainMessage(content: String)
}

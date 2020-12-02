package com.github.unchama.seichiassist.subsystems.webhook.service

import java.net.{HttpURLConnection, URL}
import java.nio.charset.StandardCharsets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

class WebhookService {
  def sendMessage(webhookURL: String, message: String): Future[Int] = Future {
    val json = s"""{"content":"$message"}"""
    val url = new URL(webhookURL)
    val httpURLConnection = url.openConnection().asInstanceOf[HttpURLConnection]
    httpURLConnection
      .tap(_.addRequestProperty("Content-Type", "application/json; charset=utf-8"))
      .tap(_.addRequestProperty("User-Agent", "DiscordBot"))
      .tap(_.setDoOutput(true))
      .tap(_.setRequestMethod("POST"))
      .tap(_.setRequestProperty("Content-Length", json.length.toString))
    val outputStream = httpURLConnection.getOutputStream
    outputStream.write(json.getBytes(StandardCharsets.UTF_8))
    outputStream.flush()
    outputStream.close()
    val statusCode = httpURLConnection.getResponseCode
    httpURLConnection.disconnect()
    statusCode
  }
}

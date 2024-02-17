package com.github.unchama.seichiassist.subsystems.playerheadskin.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.playerheadskin.domain.{
  HeadSkinUrl,
  PlayerHeadSkinUrlFetcher
}
import org.apache.commons.codec.binary.Base64
import org.jline.utils.InputStreamReader

import java.io.BufferedReader
import java.net.{HttpURLConnection, URL}
import java.nio.charset.StandardCharsets

class PlayerHeadSkinUrlFetcherByMojangAPI[F[_]: Sync] extends PlayerHeadSkinUrlFetcher[F] {

  import cats.implicits._

  private def getHttpRequest(url: String): F[Option[String]] = for {
    url <- Sync[F].pure(new URL(url))
    connection <- Sync[F].delay(url.openConnection().asInstanceOf[HttpURLConnection])
    _ <- Sync[F].delay(connection.connect())
    responseCode <- Sync[F].delay(connection.getResponseCode)
    response <- Sync[F].delay {
      Option.when(responseCode == HttpURLConnection.HTTP_OK) {
        val inputStream = connection.getInputStream
        val inputStreamReader = new InputStreamReader(inputStream, "UTF-8")
        val bufferedReader = new BufferedReader(inputStreamReader)

        val result =
          Iterator.continually(bufferedReader.readLine()).takeWhile(_ != null).mkString

        bufferedReader.close()
        inputStreamReader.close()
        inputStream.close()

        result
      }
    }
  } yield response

  import io.circe.parser._

  private def textureUrl(name: String): F[Option[String]] = {
    for {
      profileOpt <- getHttpRequest(s"https://api.mojang.com/users/profiles/minecraft/$name")
      id = for {
        profile <- profileOpt
        id <- parse(profile).toOption.flatMap(_.hcursor.get[String]("id").toOption)
      } yield id
      playerDataOpt <- getHttpRequest(
        s"https://sessionserver.mojang.com/session/minecraft/profile/$id"
      )
      url = for {
        playerData <- playerDataOpt
        base64TextureProperties <- parse(playerData)
          .toOption
          .flatMap(
            _.hcursor
              .downField("properties")
              .values
              .flatMap(_.head.hcursor.get[String]("value").toOption)
          )
        url <- parse(
          new String(Base64.decodeBase64(base64TextureProperties), StandardCharsets.UTF_8)
        ).toOption
          .flatMap(
            _.hcursor.downField("textures").downField("SKIN").get[String]("url").toOption
          )
      } yield url

    } yield url
  }

  override def fetchHeadSkinUrl(playerName: String): F[Option[HeadSkinUrl]] = for {
    textureUrl <- textureUrl(playerName)
  } yield textureUrl.map(HeadSkinUrl)

}

package com.github.unchama.itemstackbuilder

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.apache.commons.codec.binary.Base64
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Bukkit, Material}
import org.jline.utils.InputStreamReader

import java.io.BufferedReader
import java.net.{HttpURLConnection, URI, URL}
import java.nio.charset.StandardCharsets
import java.util.UUID

class SkullItemStackBuilder(private val owner: SkullOwnerReference)
    extends AbstractItemStackBuilder[SkullMeta](Material.PLAYER_HEAD) {

  /**
   * @param ownerUUID
   *   [Material.PLAYER_HEAD] に表示するプレーヤーのUUID
   */
  def this(ownerUUID: UUID) = this(SkullOwnerUuid(ownerUUID))

  private def getHttpRequest(url: String): Option[String] = {
    val _url = new URL(url)

    val connection = _url.openConnection().asInstanceOf[HttpURLConnection]
    connection.connect()

    val responseCode = connection.getResponseCode

    Option.when(responseCode == HttpURLConnection.HTTP_OK) {
      val inputStream = connection.getInputStream
      val inputStreamReader = new InputStreamReader(inputStream, "UTF-8")
      val bufferedReader = new BufferedReader(inputStreamReader)

      val result = Iterator.continually(bufferedReader.readLine()).takeWhile(_ != null).mkString

      bufferedReader.close()
      inputStreamReader.close()
      inputStream.close()

      result
    }
  }

  import io.circe.parser._

  override protected def transformItemMetaOnBuild(meta: SkullMeta): Unit = {
    owner match {
      case SkullOwnerUuidWithName(uuid, name) =>
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid))

        val textureUrl = for {
          profile <- getHttpRequest(s"https://api.mojang.com/users/profiles/minecraft/$name")
          id <- parse(profile).toOption.flatMap(_.hcursor.get[String]("id").toOption)
          playerData <- getHttpRequest(
            s"https://sessionserver.mojang.com/session/minecraft/profile/$id"
          )
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

        textureUrl match {
          case Some(url) =>
            val playerProfile = Bukkit.createPlayerProfile(uuid, name)
            playerProfile.getTextures.setSkin(URI.create(url).toURL)
            meta.setOwnerProfile(playerProfile)
          case None =>
        }
      case SkullOwnerUuid(uuid) =>
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid))
        val gameProfile = new GameProfile(uuid, null)

        val textureUrl =
          s"http://textures.minecraft.net/texture/${uuid.toString.replaceAll("-", "")}"

        val encodedData = Base64.encodeBase64(
          String.format("{textures:{SKIN:{url:\"%s\"}}}", textureUrl).getBytes
        )

        gameProfile
          .getProperties
          .put("textures", new Property("textures", new String(encodedData)))

      /**
       * @see
       *   [[https://www.spigotmc.org/threads/1-12-2-applying-custom-textures-to-skulls.327361/ カスタムヘッドを生成するコード]]
       * @see
       *   [[https://qiita.com/yuta0801/items/edb4804dfb867ea82c5a テクスチャへのリンク周り]]
       */
      case SkullOwnerTextureValue(textureValue) =>
        val uuid = UUID.randomUUID()
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid))

        val gameProfile = new GameProfile(uuid, null)
        gameProfile.getProperties.put("textures", new Property("textures", textureValue, ""))

        val profileField = meta.getClass.getDeclaredField("profile")
        profileField.setAccessible(true)
        profileField.set(meta, gameProfile)
    }
  }
}

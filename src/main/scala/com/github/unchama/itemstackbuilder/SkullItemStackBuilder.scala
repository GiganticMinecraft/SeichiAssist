package com.github.unchama.itemstackbuilder

import cats.effect.IO
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Bukkit, Material}

import java.net.URI
import java.util.UUID

class SkullItemStackBuilder(private val owner: SkullOwnerReference)(
  implicit val playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
) extends AbstractItemStackBuilder[SkullMeta](Material.PLAYER_HEAD) {

  /**
   * @param ownerUUID
   *   [Material.PLAYER_HEAD] に表示するプレーヤーのUUID
   */
  def this(ownerUUID: UUID)(implicit playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]) =
    this(SkullOwnerUuid(ownerUUID))(playerHeadSkinAPI)

  override protected def transformItemMetaOnBuild(meta: SkullMeta): Unit = {
    owner match {
      case SkullOwnerUuid(uuid) =>
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid))
        val name = Bukkit.getOfflinePlayer(uuid).getName

        playerHeadSkinAPI
          .playerHeadSkinUrl(Bukkit.getOfflinePlayer(uuid).getPlayer)
          .unsafeRunSync() match {
          case Some(url) =>
            val playerProfile = Bukkit.createPlayerProfile(uuid, name)
            playerProfile.getTextures.setSkin(URI.create(url.url).toURL)
            meta.setOwnerProfile(playerProfile)
          case None =>
        }

      case SkullOwnerUuidWithNameWithTextureUrl(uuid, name, url) =>
        val playerProfile = Bukkit.createPlayerProfile(uuid, name)
        playerProfile.getTextures.setSkin(URI.create(url).toURL)
        meta.setOwnerProfile(playerProfile)

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

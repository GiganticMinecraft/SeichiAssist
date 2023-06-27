package com.github.unchama.itemstackbuilder

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Bukkit, Material}

import java.util.UUID

/**
 * Created by karayuu on 2019/04/09
 */
class SkullItemStackBuilder(private val owner: SkullOwnerReference)
    extends AbstractItemStackBuilder[SkullMeta](Material.PLAYER_HEAD) {

  /**
   * プレーヤーがサーバーに参加したことのない場合に 頭のスキンを読み込むことができないため、そのようなケースが想定されるされる箇所では
   * プレーヤー名を[[String]]として取るコンストラクタを使用せよ。
   *
   * それ以外の場合はこのコンストラクタを使うようにせよ。 Bukkitは`Persistent storage of users should be by UUID`と記している。
   *
   * @see
   *   SkullMeta.setOwner
   * @param ownerUUID
   *   [Material.PLAYER_HEAD] に表示するプレーヤーのUUID
   */
  def this(ownerUUID: UUID) = this(SkullOwnerUuid(ownerUUID))

  /**
   * @param ownerName
   *   [Material.PLAYER_HEAD] に表示するプレーヤーの名前
   */
  def this(ownerName: String) = this(SkullOwnerName(ownerName))

  override def transformItemMetaOnBuild(meta: SkullMeta): Unit = {
    owner match {
      case SkullOwnerUuid(uuid) =>
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid))
      case SkullOwnerName(name) =>
        /**
         * 参加したことのないプレーヤーはgetOfflinePlayerでデータが取れないのでこうするしか無い
         */
        // noinspection ScalaDeprecation
        meta.setOwner(name)

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

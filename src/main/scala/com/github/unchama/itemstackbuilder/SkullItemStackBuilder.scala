package com.github.unchama.itemstackbuilder

import java.util.UUID

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Bukkit, Material, SkullType}

/**
 * Created by karayuu on 2019/04/09
 */
class SkullItemStackBuilder(private val owner: SkullOwnerReference) extends
  AbstractItemStackBuilder[SkullMeta](Material.SKULL_ITEM, SkullType.PLAYER.ordinal.toShort) {

  /**
   * プレーヤーがサーバーに参加したことのない場合に
   * 頭のスキンを読み込むことができないため、そのようなケースが想定されるされる箇所では
   * プレーヤー名を[[String]]として取るコンストラクタ or [[UUID]]と[[SkullData.TextureValue]]の組をそれぞれコンストラクタに使用せよ。
   *
   * それ以外の場合はこのコンストラクタを使うようにせよ。
   * Bukkitは`Persistent storage of users should be by UUID`と記している。
   *
   * @see SkullMeta.setOwner
   * @param ownerUUID [Material.SKULL_ITEM] に表示するプレーヤーのUUID
   */
  def this(ownerUUID: UUID) = this(SkullOwnerUuid(ownerUUID))

  /**
   * @param ownerName [Material.SKULL_ITEM] に表示するプレーヤーの名前
   */
  def this(ownerName: String) = this(SkullOwnerName(ownerName))

  /**
   * @param uuid
   * @param textureValue TextureValue。[[SkullData]]を参照する
   */
  def this(uuid: UUID, textureValue: String) = this(SkullOwnerUuidWithTextureValue(uuid, textureValue))

  override def transformItemMetaOnBuild(meta: SkullMeta): Unit = {
    owner match {
      case SkullOwnerUuid(uuid) =>
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid))
      case SkullOwnerName(name) =>

        /**
         * 参加したことのないプレーヤーはgetOfflinePlayerでデータが取れないのでこうするしか無い
         */
        //noinspection ScalaDeprecation
        meta.setOwner(name)
      case SkullOwnerUuidWithTextureValue(uuid, textureValue) =>
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid))

        val gameProfile = new GameProfile(uuid, null)
        gameProfile.getProperties.put("textures", new Property("textures", textureValue, ""))

        val profileField = meta.getClass.getDeclaredField("profile")
        profileField.setAccessible(true)
        profileField.set(meta, gameProfile)
    }
  }
}

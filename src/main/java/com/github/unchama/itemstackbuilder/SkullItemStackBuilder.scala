package com.github.unchama.itemstackbuilder

/**
 * Created by karayuu on 2019/04/09
 */
class SkullItemStackBuilder(private val owner: SkullOwnerReference):
    AbstractItemStackBuilder<SkullItemStackBuilder, SkullMeta>(
        Material.SKULL_ITEM, SkullType.PLAYER.ordinal.toShort()
    ) {

  /**
   * プレーヤーがサーバーに参加したことのない場合に
   * 頭のスキンを読み込むことができないため、そのようなケースが想定されるされる箇所では
   * プレーヤー名を[String]として取るコンストラクタを使用せよ。
   *
   * それ以外の場合はこのコンストラクタを使うようにせよ。
   * Bukkitは`Persistent storage of users should be by UUID`と記している。
   *
   * @see SkullMeta.setOwner
   *
   * @param ownerUUID [Material.SKULL_ITEM] に表示するプレーヤーのUUID
   */
  constructor(ownerUUID: UUID): this(Left(ownerUUID))

  /**
   * @param ownerName [Material.SKULL_ITEM] に表示するプレーヤーの名前
   */
  constructor(ownerName: String): this(ownerName.asSkullOwnerReference())

  override def transformItemMetaOnBuild(meta: SkullMeta) {
    when (owner) {
      is Either.Left -> {
        val offlinePlayer = Bukkit.getOfflinePlayer(owner.a)
        meta.owningPlayer = offlinePlayer
      }
      is Either.Right -> {
        /**
         * 参加したことのないプレーヤーはgetOfflinePlayerでデータが取れないのでこうするしか無い
         */
        @Suppress("DEPRECATION")
        meta.owner = owner.b
      }
    }
  }
}

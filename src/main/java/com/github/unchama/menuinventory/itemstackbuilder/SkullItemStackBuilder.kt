package com.github.unchama.menuinventory.itemstackbuilder

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.SkullMeta

import java.util.UUID

/**
 * @param ownerUUID [Material.SKULL_ITEM] に表示するskullのownerを設定します.
 *
 * Created by karayuu on 2019/04/09
 */
class SkullItemStackBuilder(private val ownerUUID: UUID):
    AbstractItemStackBuilder<SkullItemStackBuilder, SkullMeta>(Material.SKULL_ITEM, 3.toShort()) {

  override fun transformItemMeta(meta: SkullMeta) {
    val offlinePlayer = Bukkit.getOfflinePlayer(ownerUUID)
    meta.owningPlayer = offlinePlayer
  }
}

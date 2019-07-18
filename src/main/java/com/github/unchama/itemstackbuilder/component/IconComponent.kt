package com.github.unchama.itemstackbuilder.component

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * ビルダー内で保持されるアイテムスタックの情報をまとめて持つデータ型.
 * ミュータブルな設計になっている.
 *
 * Created by karayuu on 2019/04/09
 */
class IconComponent constructor(val material: Material, private val durability: Short = 0.toShort()) {
  var title: String? = Bukkit.getItemFactory().getItemMeta(material)?.displayName
  var lore: List<String> = emptyList()

  var isEnchanted: Boolean = false
  var amount = 1

  val itemStack: ItemStack
    get() = ItemStack(material, amount, durability)

  val itemMeta: ItemMeta
    get() {
      val meta = Bukkit.getItemFactory().getItemMeta(material)
      title?.let {
        meta.displayName = it
      }
      meta.lore = lore

      if (isEnchanted) {
        meta.addEnchant(Enchantment.DIG_SPEED, 100, false)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
      }

      return meta
    }
}

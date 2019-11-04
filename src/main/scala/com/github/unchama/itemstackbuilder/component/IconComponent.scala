package com.github.unchama.itemstackbuilder.component

import com.github.unchama.util.syntax.Nullability._
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.{Bukkit, Material}

import scala.jdk.javaapi.CollectionConverters.asJava

/**
 * ビルダー内で保持されるアイテムスタックの情報をまとめて持つデータ型.
 * ミュータブルな設計になっている.
 *
 * Created by karayuu on 2019/04/09
 */
class IconComponent(val material: Material, private val durability: Short = 0.toShort) {
  var title: String = Bukkit.getItemFactory.getItemMeta(material).ifNotNull(_.getDisplayName)
  var lore: List[String] = Nil

  var isUnbreakable: Boolean = false

  var isEnchanted: Boolean = false
  var amount = 1

  var itemFlagSet: Set[ItemFlag] = Set()

  def itemStack(): ItemStack = new ItemStack(material, amount, durability)

  def itemMeta(): ItemMeta = {
    val meta = Bukkit.getItemFactory.getItemMeta(material)

    title.ifNotNull(meta.setDisplayName)

    meta.setLore(asJava(lore))

    if (isUnbreakable) {
      meta.setUnbreakable(true)
      meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
    }

    if (isEnchanted) {
      meta.addEnchant(Enchantment.DIG_SPEED, 1, false)
      meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }

    meta.addItemFlags(itemFlagSet.toArray: _*)

    meta
  }
}

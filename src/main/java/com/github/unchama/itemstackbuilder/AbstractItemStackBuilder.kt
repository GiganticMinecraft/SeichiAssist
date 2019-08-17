package com.github.unchama.itemstackbuilder

import com.github.unchama.itemstackbuilder.component.IconComponent
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * ItemStackBuilderのベースとなる抽象クラス.
 *
 * @param T 派生クラス自身の型
 * @param M 派生クラスが生成する[ItemStack]が持つべきであろう[ItemMeta]の型.
 *
 * @author karayuu
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractItemStackBuilder<T : AbstractItemStackBuilder<T, M>, M: ItemMeta>
protected constructor(material: Material, durability: Short) : ItemStackBuilder {

  private val component: IconComponent = IconComponent(material, durability)

  override fun title(title: String): T {
    this.component.title = title
    return this as T
  }

  override fun lore(lore: List<String>): T {
    this.component.lore = lore
    return this as T
  }

  override fun enchanted(): T {
    this.component.isEnchanted = true
    return this as T
  }

  override fun unbreakable(): T {
    this.component.isUnbreakable = true
    return this as T
  }

  override fun amount(amount: Int): T {
    this.component.amount = amount
    return this as T
  }

  final override fun build(): ItemStack = component.itemStack.apply {
    itemMeta = (component.itemMeta as M).also { transformItemMetaOnBuild(it) }
  }

  /**
   * 生成されるアイテムスタックに入る[ItemMeta]を, ビルダー内の情報に基づいて変更する.
   */
  protected abstract fun transformItemMetaOnBuild(meta: M)
}

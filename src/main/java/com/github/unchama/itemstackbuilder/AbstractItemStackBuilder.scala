package com.github.unchama.itemstackbuilder

/**
 * ItemStackBuilderのベースとなる抽象クラス.
 *
 * @param T 派生クラス自身の型
 * @param M 派生クラスが生成する[ItemStack]が持つべきであろう[ItemMeta]の型.
 *
 * @author karayuu
 */
@Suppress("UNCHECKED_CAST")
abstract class AbstractItemStackBuilder<T : AbstractItemStackBuilder<T, M>, M : ItemMeta>
protected constructor(material: Material, durability: Short) : ItemStackBuilder {

  private val component: IconComponent = IconComponent(material, durability)

  override def title(title: String): T {
    this.component.title = title
    return this as T
  }

  override def lore(lore: List<String>): T {
    this.component.lore = lore
    return this as T
  }

  override def enchanted(): T {
    this.component.isEnchanted = true
    return this as T
  }

  override def unbreakable(): T {
    this.component.isUnbreakable = true
    return this as T
  }

  override def amount(amount: Int): T {
    this.component.amount = amount
    return this as T
  }

  override def flagged(flag: ItemFlag): T {
    this.component.itemFlagSet = component.itemFlagSet.plus(flag)
    return this as T
  }

  final override def build(): ItemStack = component.itemStack.apply {
    itemMeta = (component.itemMeta as M).also { transformItemMetaOnBuild(it) }
  }

  /**
   * 生成されるアイテムスタックに入る[ItemMeta]を, ビルダー内の情報に基づいて変更する.
   */
  protected abstract def transformItemMetaOnBuild(meta: M)
}

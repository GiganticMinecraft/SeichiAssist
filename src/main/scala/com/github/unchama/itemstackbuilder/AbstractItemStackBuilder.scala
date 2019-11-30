package com.github.unchama.itemstackbuilder

import com.github.unchama.itemstackbuilder.component.IconComponent
import org.bukkit.Material
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.{ItemFlag, ItemStack}

/**
 * ItemStackBuilderのベースとなる抽象クラス.
 *
 * @tparam M 派生クラスが生成する[ItemStack]が持つべきであろう[ItemMeta]の型.
 * @author karayuu
 */
abstract class AbstractItemStackBuilder[-M <: ItemMeta] protected
(material: Material, durability: Short) extends ItemStackBuilder {

  private val component: IconComponent = new IconComponent(material, durability)

  final override def build(): ItemStack = {
    val itemStack = component.itemStack()

    itemStack.setItemMeta {
      val meta = component.itemMeta().asInstanceOf[M]
      transformItemMetaOnBuild(meta)
      meta
    }

    itemStack
  }

  override def title(title: String): this.type = {
    this.component.title = title
    this
  }

  override def lore(lore: List[String]): this.type = {
    this.component.lore = lore
    this
  }

  override def enchanted(): this.type = {
    this.component.isEnchanted = true
    this
  }

  override def unbreakable(): this.type = {
    this.component.isUnbreakable = true
    this
  }

  override def amount(amount: Int): this.type = {
    this.component.amount = amount
    this
  }

  override def flagged(flag: ItemFlag): this.type = {
    this.component.itemFlagSet = component.itemFlagSet + flag
    this
  }

  /**
   * 生成されるアイテムスタックに入る[ItemMeta]を, ビルダー内の情報に基づいて変更する.
   */
  protected def transformItemMetaOnBuild(meta: M): Unit
}

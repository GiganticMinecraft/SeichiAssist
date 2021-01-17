package com.github.unchama.seichiassist.util.typeclass

import org.bukkit.inventory.{Inventory, ItemStack}

trait OrderedCollection[E] {
  def apply(int: Int): E

  def size: Int
}

object OrderedCollection {
  implicit def forList[E](self: List[E]): OrderedCollection[E] = new OrderedCollection[E] {
    override def apply(int: Int): E = self(int)

    override def size: Int = self.size
  }

  implicit def forInventory(self: Inventory): OrderedCollection[ItemStack] = new OrderedCollection[ItemStack] {
    override def apply(int: Int): ItemStack = self.getItem(int)

    override def size: Int = self.getSize
  }
}
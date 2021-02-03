package com.github.unchama.seichiassist.util.typeclass

import org.bukkit.inventory.{Inventory, ItemStack}

trait OrderedCollection[E] {
  def apply(int: Int): E

  def size: Int

  final def foreach(e: E => Unit): Unit = (0 until size).map(this.apply).foreach(e)
}

object OrderedCollection {
  implicit class ForList[E](self: List[E]) extends OrderedCollection[E] {
    override def apply(int: Int): E = self(int)

    override def size: Int = self.size
  }

  implicit class ForInventory(self: Inventory) extends OrderedCollection[ItemStack] {
    override def apply(int: Int): ItemStack = self.getItem(int)

    override def size: Int = self.getSize
  }

  implicit class ForJUList[E](self: java.util.List[E]) extends OrderedCollection[E] {
    override def apply(int: Int): E = self.get(int)

    override def size: Int = self.size()
  }

  implicit class ForArray[E](self: Array[E]) extends OrderedCollection[E] {
    override def apply(int: Int): E = self(int)

    override def size: Int = self.length
  }
}
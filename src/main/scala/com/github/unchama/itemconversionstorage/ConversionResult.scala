package com.github.unchama.itemconversionstorage

import org.bukkit.inventory.ItemStack

sealed trait ConversionResult {
  val itemStack: Option[ItemStack]
}

object ConversionResult {
  case class Identity(_itemStack: ItemStack) extends ConversionResult {
    override val itemStack: Some[ItemStack] = Some(_itemStack)
  }
  case class Mapped(_itemStack: ItemStack) extends ConversionResult {
    override val itemStack: Some[ItemStack] = Some(_itemStack)
  }
  case object Discard extends ConversionResult {
    override val itemStack: None.type = None
  }
}

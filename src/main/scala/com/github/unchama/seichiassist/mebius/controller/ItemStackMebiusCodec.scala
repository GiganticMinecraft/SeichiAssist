package com.github.unchama.seichiassist.mebius.controller

import com.github.unchama.seichiassist.mebius.domain.MebiusProperty
import org.bukkit.inventory.ItemStack

object ItemStackMebiusCodec {

  def getMebiusProperty(itemStack: ItemStack): Option[MebiusProperty] = {
    ???
  }

  def materialize(property: MebiusProperty): ItemStack = {
    ???
  }

}

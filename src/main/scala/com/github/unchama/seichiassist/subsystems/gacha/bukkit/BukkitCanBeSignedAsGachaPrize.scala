package com.github.unchama.seichiassist.subsystems.gacha.bukkit

import com.github.unchama.seichiassist.subsystems.gacha.domain.CanBeSignedAsGachaPrize
import com.github.unchama.util.bukkit.ItemStackUtil.appendOwnerInformation
import org.bukkit.inventory.ItemStack

class BukkitCanBeSignedAsGachaPrize extends CanBeSignedAsGachaPrize[ItemStack] {
  override def signWith(ownerName: String): ItemStack => ItemStack = { itemStack =>
    appendOwnerInformation(ownerName)(itemStack)
  }
}

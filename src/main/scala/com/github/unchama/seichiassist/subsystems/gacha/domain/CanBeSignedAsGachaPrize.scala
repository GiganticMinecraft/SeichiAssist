package com.github.unchama.seichiassist.subsystems.gacha.domain

trait CanBeSignedAsGachaPrize[ItemStack] {
  def signWith(ownerName: String): GachaPrize[ItemStack] => ItemStack
}

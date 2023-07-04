package com.github.unchama.seichiassist.subsystems.gachaprize.domain

trait CanBeSignedAsGachaPrize[ItemStack] {
  def signWith(ownerName: String): GachaPrize[ItemStack] => ItemStack
}

package com.github.unchama.seichiassist.subsystems.gachaprize.domain

import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.GachaPrize

trait CanBeSignedAsGachaPrize[ItemStack] {
  def signWith(ownerName: String): GachaPrize[ItemStack] => ItemStack
}

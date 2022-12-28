package com.github.unchama.seichiassist.subsystems.gacha.domain

import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize.GachaPrize

trait CanBeSignedAsGachaPrize[ItemStack] {
  def signWith(ownerName: String): GachaPrize[ItemStack] => ItemStack
}

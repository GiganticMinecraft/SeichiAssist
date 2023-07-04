package com.github.unchama.seichiassist.subsystems.minestack.domain

import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrize

case class MineStackGachaObject[ItemStack](
  objectName: String,
  gachaPrize: GachaPrize[ItemStack]
)

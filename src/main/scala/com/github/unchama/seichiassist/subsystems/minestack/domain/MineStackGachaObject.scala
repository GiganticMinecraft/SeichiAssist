package com.github.unchama.seichiassist.subsystems.minestack.domain

import com.github.unchama.seichiassist.subsystems.gachaprize.domain.GachaPrizeTableEntry

case class MineStackGachaObject[ItemStack](
  objectName: String,
  gachaPrize: GachaPrizeTableEntry[ItemStack]
)

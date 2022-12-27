package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.domain

trait StaticTradeItemFactory[ItemStack] {

  /**
   * 所有者名を渡して椎名林檎の[[ItemStack]]を返す
   */
  val getMaxRingo: String => ItemStack

}

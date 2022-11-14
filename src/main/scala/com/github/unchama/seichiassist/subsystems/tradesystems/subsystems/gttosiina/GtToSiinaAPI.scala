package com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina

trait GtToSiinaAPI[ItemStack] {

  /**
   * @return 記名された椎名林檎
   */
  def getMaxSiinaRingo(name: String): ItemStack

}

object GtToSiinaAPI {

  def apply[ItemStack](implicit ev: GtToSiinaAPI[ItemStack]): GtToSiinaAPI[ItemStack] = ev

}

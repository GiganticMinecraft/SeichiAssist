package com.github.unchama.seichiassist.subsystems.gacha.domain

trait StaticGachaPrizeFactory[ItemStack] {

  /**
   * がちゃりんごの[[ItemStack]]を返す
   */
  val gachaRingo: ItemStack

  /**
   * 死神の鎌の[[ItemStack]]を返す
   * TODO: これはここに書かれるべきではなさそう？
   * ガチャアイテムとして排出されていないため。
   */
  val mineHeadItem: ItemStack

}

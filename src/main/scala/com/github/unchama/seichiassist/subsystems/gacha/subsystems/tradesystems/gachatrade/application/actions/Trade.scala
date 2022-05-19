package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gachatrade.application.actions

trait Trade[F[_], ItemStack] {

  /**
   * giganticアイテムから椎名林檎へ交換できるアイテムを列挙する
   * @return Tupleの1つ目は、大当たりから交換できるガチャ券の数
   *         2つ目はあたりから交換できるガチャ券の数
   *         3つ目は返却するアイテム
   */
  def trade(contents: List[ItemStack]): F[(Int, Int, List[ItemStack])]

}

object Trade {

  def apply[F[_], ItemStack](implicit ev: Trade[F, ItemStack]): Trade[F, ItemStack] = ev

}

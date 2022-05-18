package com.github.unchama.seichiassist.subsystems.gacha.subsystems.tradesystems.gttosiina.application.actions

trait Trade[F[_], ItemStack] {

  /**
   * giganticアイテムから椎名林檎へ交換できるアイテムを列挙する
   * @return Tupleの1つ目は、交換できる椎名林檎の数を返す
   *         2つ目は交換不可能なアイテムを返す
   */
  def trade(contents: List[ItemStack]): F[(Int, List[ItemStack])]

}

object Trade {

  def apply[F[_], ItemStack](implicit ev: Trade[F, ItemStack]): Trade[F, ItemStack] = ev

}

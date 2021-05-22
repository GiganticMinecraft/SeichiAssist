package com.github.unchama.seichiassist.subsystems.gacha.domain

sealed trait GachaPrizeTemplate[IS] {

  val amount: Int

  val probability: Double

}

object GachaPrizeTemplate {
  /**
   * 記名が入るガチャ景品のテンプレート。
   *
   * 最終的なガチャ景品は [[stack]] へ名前データを追記し、
   * スタック数を [[amount]] としたものとなる。
   */
  case class Owned[IS](stack: IS,
                       override val amount: Int,
                       override val probability: Double) extends GachaPrizeTemplate[IS]

  /**
   * 記名が入らず、アイテムスタックがそのまま景品として授与されるガチャ景品のテンプレート。
   *
   * 最終的なガチャ景品は [[stack]] のスタック数を [[amount]] としたものとなる。
   */
  case class Fixed[IS](stack: IS,
                       override val amount: Int,
                       override val probability: Double) extends GachaPrizeTemplate[IS]
}

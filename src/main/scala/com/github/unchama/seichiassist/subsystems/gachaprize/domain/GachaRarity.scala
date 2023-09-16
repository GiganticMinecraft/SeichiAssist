package com.github.unchama.seichiassist.subsystems.gachaprize.domain

object GachaRarity {

  import enumeratum._

  /**
   * `probabilityUpperLimit`は[[GachaRarity]]になるガチャアイテムの出現確率の上限値である。
   * 正確には、以下のような定義を用いる。
   *
   * ガチャ景品 `g: GachaPrize[ItemStack]`、レアリティ `r: GachaRarity` について
   * 「`g` が `r` 以上のレアリティである」を `g.probability < r.probabilityUpperLimit`
   * が成り立つことと定義する。
   *
   * また、ガチャレアリティ `r1, r2: GachaRarity` について
   * 「`r1` は `r2` よりもレアである」 を `r1.probabilityUpperLimit < r2.probabilityUpperLimit`
   * が成り立つことと定義する。
   *
   * これらの定義を踏まえた上で、
   * ガチャ景品 `g: GachaPrize[ItemStack]` のレアリティ `r = GachaRarity.of(g)` は、
   * `g` が `rarity` 以上のレアリティであるような `rarity: GachaRarity` のうち
   * 最もレアなレアリティとして定義される。
   * もしそのようなレアリティが存在しなければ、 `r` は `GachaRingoOrExpBottle` とする。
   */
  sealed abstract class GachaRarity(val probabilityUpperLimit: GachaProbability)
      extends EnumEntry

  case object GachaRarity extends Enum[GachaRarity] {

    case object Gigantic extends GachaRarity(GachaProbability(0.001))

    case object Big extends GachaRarity(GachaProbability(0.01))

    case object Regular extends GachaRarity(GachaProbability(0.1))

    case object GachaRingoOrExpBottle extends GachaRarity(GachaProbability(1.0))

    override def values: IndexedSeq[GachaRarity] = findValues

    def of[ItemStack](gachaPrize: GachaPrizeTableEntry[ItemStack]): GachaRarity =
      GachaRarity
        .values
        .filter { rarity => rarity.probabilityUpperLimit.value > gachaPrize.probability.value }
        .minByOption(_.probabilityUpperLimit.value)
        .getOrElse(GachaRingoOrExpBottle)

  }

}

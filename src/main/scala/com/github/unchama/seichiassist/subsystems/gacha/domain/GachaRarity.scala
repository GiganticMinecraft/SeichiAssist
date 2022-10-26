package com.github.unchama.seichiassist.subsystems.gacha.domain

import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize.GachaPrize

object GachaRarity {

  import enumeratum._

  /**
   * `probabilityUpperLimit`は[[GachaRarity]]になるガチャアイテムの出現確率の最大値である。
   */
  sealed abstract class GachaRarity(val probabilityUpperLimit: GachaProbability) extends EnumEntry

  case object GachaRarity extends Enum[GachaRarity] {

    case object Gigantic extends GachaRarity(GachaProbability(0.001))

    case object Big extends GachaRarity(GachaProbability(0.01))

    case object Regular extends GachaRarity(GachaProbability(0.1))

    case object GachaRingoOrExpBottle extends GachaRarity(GachaProbability(1.0))

    override def values: IndexedSeq[GachaRarity] = findValues

    def of[ItemStack](gachaPrize: GachaPrize[ItemStack]): GachaRarity =
      GachaRarity
        .values
        .filter { rarity => rarity.probabilityUpperLimit.value > gachaPrize.probability.value }
        .minByOption(_.probabilityUpperLimit.value)
        .getOrElse(GachaRingoOrExpBottle)

  }

}

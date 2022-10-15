package com.github.unchama.seichiassist.subsystems.gacha.domain

object GachaRarity {

  import enumeratum._

  sealed abstract class GachaRarity(val maxProbability: GachaProbability) extends EnumEntry

  case object GachaRarity extends Enum[GachaRarity] {

    case object Gigantic extends GachaRarity(GachaProbability(0.001))

    case object Big extends GachaRarity(GachaProbability(0.01))

    case object Regular extends GachaRarity(GachaProbability(0.1))

    case object GachaRingoOrExpBottle extends GachaRarity(GachaProbability(1.0))

    override def values: IndexedSeq[GachaRarity] = findValues

    def of[ItemStack](gachaPrize: GachaPrize[ItemStack]): GachaRarity =
      GachaRarity
        .values
        .find { gachaRarities =>
          gachaRarities.maxProbability.value >= gachaPrize.probability.value
        }
        .minByOption(_.maxProbability.value)
        .getOrElse(GachaRingoOrExpBottle)

  }

}

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

    def of[ItemStack](gachaPrize: GachaPrize[ItemStack]): GachaRarity = {
      val gachaPrizeProbability = gachaPrize.probability.value
      if (gachaPrizeProbability < GachaRarity.Gigantic.maxProbability.value)
        GachaRarity.Gigantic
      else if (gachaPrizeProbability < GachaRarity.Big.maxProbability.value)
        GachaRarity.Big
      else if (gachaPrizeProbability < GachaRarity.Regular.maxProbability.value)
        GachaRarity.Regular
      else GachaRarity.GachaRingoOrExpBottle
    }

  }

}

package com.github.unchama.seichiassist.subsystems.gacha.domain

object GachaRarity {

  import enumeratum._

  sealed abstract class GachaRarity(val maxProbability: Double) extends EnumEntry

  case object GachaRarity extends Enum[GachaRarity] {

    case object Gigantic extends GachaRarity(0.001)

    case object Big extends GachaRarity(0.01)

    case object Regular extends GachaRarity(0.1)

    case object Potato extends GachaRarity(1.0)

    override def values: IndexedSeq[GachaRarity] = findValues

  }

}

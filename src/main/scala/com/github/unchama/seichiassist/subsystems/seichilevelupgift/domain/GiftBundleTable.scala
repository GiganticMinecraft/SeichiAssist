package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel

object GiftBundleTable {

  import cats.implicits._

  def bundleAt(level: SeichiLevel): GiftBundle = {
    val gachaTicketForBeginners = if (level <= SeichiLevel.ofPositive(50)) {
      GiftBundle.ofSinglePair(Gift.GachaPointWorthSingleTicket, level.level * 5)
    } else {
      GiftBundle.empty
    }

    val thresholdGifts = level.level match {
      case 10 => GiftBundle.ofSinglePair(Gift.Item.SuperPickaxe, 5)
      case 20 => GiftBundle.ofSinglePair(Gift.AutomaticGachaRun, 13)
      case 40 => GiftBundle.ofSinglePair(Gift.Item.GachaApple, 256)
      case 50 => GiftBundle.ofSinglePair(Gift.AutomaticGachaRun, 23)
      case 60 => GiftBundle.ofSinglePair(Gift.AutomaticGachaRun, 26)
      case 70 => GiftBundle.ofSinglePair(Gift.AutomaticGachaRun, 25)
      case 80 => GiftBundle.ofSinglePair(Gift.AutomaticGachaRun, 24)
      case 90 => GiftBundle.ofSinglePair(Gift.AutomaticGachaRun, 20)
      case 100 =>
        GiftBundle.ofSinglePair(Gift.AutomaticGachaRun, 21).combinePair(Gift.Item.Elsa, 1)
      case _ => GiftBundle.empty
    }

    gachaTicketForBeginners.combine(thresholdGifts)
  }

}

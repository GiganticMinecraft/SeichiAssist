package com.github.unchama.seichiassist.subsystems.gacha.domain

import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaevent.GachaEventName

sealed abstract class GachaPrizeDischargeCategory(eventName: Option[GachaEventName])

object GachaPrizeDischargeCategory {

  /**
   * イベントの開催状況に左右されず、常に排出する
   */
  case object Always extends GachaPrizeDischargeCategory(None)

  /**
   * イベントの開催状況に左右され、イベント開催時のみ排出する
   */
  case class OnlyEventHeld(gachaEventName: GachaEventName)
      extends GachaPrizeDischargeCategory(Some(gachaEventName))

}

package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

import java.time.LocalDate

import enumeratum.{Enum, EnumEntry}

sealed trait LimitedLoginEvent extends EnumEntry with LoginBonusItemList with LimitedLoginPeriod

/**
 * イベントの列挙及びそれぞれに必要なものの定義
 */
object LimitedLoginEvents extends Enum[LimitedLoginEvent] {

  val values: IndexedSeq[LimitedLoginEvent] = findValues

  def findActiveEvent: Option[LimitedLoginEvent] = values.find(_.isInEvent)

  case object Valentine extends LimitedLoginEvent {
    override val map = Map(
      (0, Set(LoginBonus(LoginBonusGachaTicket, 20))),
      (20, Set(LoginBonus(LoginBonusGachaTicket, 200)))
    )

    override val EVENT_PERIOD: EventPeriod = EventPeriod(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 31))
  }

  case object Anniversary extends LimitedLoginEvent {
    override val map = Map(
      (0, Set(LoginBonus(LoginBonusGachaTicket, 20))),
      (20, Set(LoginBonus(LoginBonusGachaTicket, 200)))
    )

    override val EVENT_PERIOD: EventPeriod = EventPeriod(LocalDate.of(2018, 1, 1), LocalDate.of(2018, 1, 31))
  }
}

package com.github.unchama.seichiassist.subsystems.gachapoint.domain.settings

sealed trait GachaTicketReceivingSettings {

  def next: GachaTicketReceivingSettings

}

object GachaTicketReceivingSettings {

  case object EveryMinute extends GachaTicketReceivingSettings {
    def next: GachaTicketReceivingSettings = Batch
  }

  case object Batch extends GachaTicketReceivingSettings {
    def next: GachaTicketReceivingSettings = EveryMinute
  }

}

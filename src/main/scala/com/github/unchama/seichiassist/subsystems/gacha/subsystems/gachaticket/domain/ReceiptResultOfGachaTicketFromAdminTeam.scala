package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain

sealed trait ReceiptResultOfGachaTicketFromAdminTeam

object ReceiptResultOfGachaTicketFromAdminTeam {

  /**
   * 成功した
   */
  case object Success extends ReceiptResultOfGachaTicketFromAdminTeam

  /**
   * 見つからなかった(失敗)
   */
  case object NotExists extends ReceiptResultOfGachaTicketFromAdminTeam

  /**
   * 複数見つかった
   */
  case object MultipleFound extends ReceiptResultOfGachaTicketFromAdminTeam

}

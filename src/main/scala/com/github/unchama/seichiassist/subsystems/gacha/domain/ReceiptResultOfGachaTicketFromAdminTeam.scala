package com.github.unchama.seichiassist.subsystems.gacha.domain

sealed trait ReceiptResultOfGachaTicketFromAdminTeam

object ReceiptResultOfGachaTicketFromAdminTeam {

  case object Success extends ReceiptResultOfGachaTicketFromAdminTeam

  case object NotExists extends ReceiptResultOfGachaTicketFromAdminTeam

  /**
   * 受け取り結果を取得する
   * NOTE: 受け取り結果は更新された件数によって決められる
   */
  def getReceiptResult(updatedRows: Int): ReceiptResultOfGachaTicketFromAdminTeam =
    updatedRows match {
      case 0 => ReceiptResultOfGachaTicketFromAdminTeam.NotExists
      case 1 => ReceiptResultOfGachaTicketFromAdminTeam.Success
    }

}

package com.github.unchama.seichiassist.subsystems.gacha.domain

sealed trait ReceiptResultOfGachaTicketFromAdminTeam

object ReceiptResultOfGachaTicketFromAdminTeam {

  case object Success extends ReceiptResultOfGachaTicketFromAdminTeam

  case object NotExists extends ReceiptResultOfGachaTicketFromAdminTeam

}

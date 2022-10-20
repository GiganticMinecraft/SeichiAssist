package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain

sealed trait GrantResultOfGachaTicketFromAdminTeam

object GrantResultOfGachaTicketFromAdminTeam {

  /**
   * 成功した
   */
  case object Success extends GrantResultOfGachaTicketFromAdminTeam

  /**
   * 見つからなかった(失敗)
   */
  case object NotExists extends GrantResultOfGachaTicketFromAdminTeam

  /**
   * 複数見つかった
   */
  case object MultipleFound extends GrantResultOfGachaTicketFromAdminTeam

}

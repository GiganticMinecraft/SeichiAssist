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
   * 付与対象が複数見つかり、全員に付与した
   */
  case object GrantedToMultiplePlayers extends GrantResultOfGachaTicketFromAdminTeam

}

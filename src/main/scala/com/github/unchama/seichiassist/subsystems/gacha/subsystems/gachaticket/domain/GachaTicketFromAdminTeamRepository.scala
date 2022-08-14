package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain

import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  PlayerName,
  ReceiptResultOfGachaTicketFromAdminTeam
}

import java.util.UUID

trait GachaTicketFromAdminTeamRepository[F[_]] {

  /**
   * @return 呼び出された時点で永続化バックエンド中にある全プレイヤーの「運営からのガチャ券」を増加させる作用
   */
  def addToAllKnownPlayers(amount: Int): F[Unit]

  /**
   * @return 指定されたプレイヤー名の「運営からのガチャ券」の枚数を増加させる作用
   */
  def addByPlayerName(
    amount: Int,
    playerName: PlayerName
  ): F[ReceiptResultOfGachaTicketFromAdminTeam]

  /**
   * @return 指定されたUUIDの「運営からのガチャ券」の枚数を増加させる作用
   */
  def addByUUID(amount: Int, uuid: UUID): F[ReceiptResultOfGachaTicketFromAdminTeam]

}

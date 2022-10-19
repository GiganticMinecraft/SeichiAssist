package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain

import com.github.unchama.seichiassist.subsystems.gacha.domain.PlayerName

import java.util.UUID

trait GachaTicketFromAdminTeamRepository[F[_]] {

  /**
   * @return 呼び出された時点で永続化バックエンド中にある全プレイヤーの「運営からのガチャ券」を増加させる作用
   */
  def addToAllKnownPlayers(amount: GachaTicketAmount): F[Unit]

  /**
   * @return 指定されたプレイヤー名の「運営からのガチャ券」の枚数を増加させる作用
   */
  def addByPlayerName(
    amount: GachaTicketAmount,
    playerName: PlayerName
  ): F[ReceiptResultOfGachaTicketFromAdminTeam]

  /**
   * @return 指定されたUUIDの「運営からのガチャ券」の枚数を増加させる作用
   */
  def addByUUID(amount: GachaTicketAmount, uuid: UUID): F[ReceiptResultOfGachaTicketFromAdminTeam]

  /**
   * @return 運営からのガチャ券を受け取った枚数
   */
  def receive(uuid: UUID): F[GachaTicketAmount]

}

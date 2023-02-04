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
  ): F[GrantResultOfGachaTicketFromAdminTeam]

  /**
   * @return 指定されたUUIDの「運営からのガチャ券」の枚数を増加させる作用
   */
  def addByUUID(amount: GachaTicketAmount, uuid: UUID): F[GrantResultOfGachaTicketFromAdminTeam]

  /**
   * 指定されたUUIDの「運営からのガチャ券」の枚数を最大 576、負にならない範囲で差し引く作用。
   * 作用の結果として、何枚「運営からのガチャ券」が差し引かれたかの `GachaTicketAmount` が返る。
   *
   * この作用は、プレーヤーが「運営からのガチャ券」を受け取るときに実行する作用に対応する。
   * 受け取り可能枚数の上限は 576 (`= 9 * 64`、9スタック) 枚と設定されている。
   */
  def receive(uuid: UUID): F[GachaTicketAmount]

}

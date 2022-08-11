package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain

import com.github.unchama.seichiassist.subsystems.gacha.domain.PlayerName

trait GachaTicketFromAdminTeamRepository[F[_]] {

  /**
   * 現在データベース中にある全プレイヤーの「運営からのガチャ券」の枚数を増加させる作用
   */
  def addToAllKnownPlayers(amount: Int): F[Unit]

  /**
   * 指定されたプレイヤー名の「運営からのガチャ券」の枚数を増加させる作用
   */
  def add(amount: Int, playerName: PlayerName): F[Unit]

}

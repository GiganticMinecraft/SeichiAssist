package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain

import java.util.UUID

trait GachaTicketFromAdminTeamGateway[F[_]] {

  /**
   * 現在データベース中にある全プレイヤーの「運営からのガチャ券」の枚数を増加させる作用
   */
  def add(amount: Int): F[Boolean]

  /**
   * 指定されたUUIDのプレイヤーの「運営からのガチャ券」の枚数を増加させる作用
   */
  def add(amount: Int, uuid: UUID): F[Boolean]

}

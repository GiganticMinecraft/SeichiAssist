package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain

import java.util.UUID

trait GachaTicketFromAdminTeamGateway[F[_]] {

  /**
   * 運営からのガチャ券の枚数を全員に増加させる作用
   */
  def add(amount: Int): F[Boolean]

  /**
   * 運営からのガチャ券の枚数を指定UUIDのプレイヤーに増加させる作用
   */
  def add(amount: Int, uuid: UUID): F[Boolean]

}

package com.github.unchama.seichiassist.subsystems.gacha.subsystems.consumegachaticket

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.consumegachaticket.domain.GachaTicketConsumeAmount

trait ConsumeGachaTicketAPI[F[_], Player] {

  /**
   * @return 一度に引くガチャ券の枚数をトグルする作用
   */
  def toggleConsumeGachaTicketAmount: Kleisli[F, Player, Unit]

  /**
   * @return 一度に引くガチャ券の枚数を取得する作業
   */
  def consumeGachaTicketAmount(player: Player): F[GachaTicketConsumeAmount]

}

object ConsumeGachaTicketAPI {

  def apply[F[_], Player](
    implicit ev: ConsumeGachaTicketAPI[F, Player]
  ): ConsumeGachaTicketAPI[F, Player] = ev

}

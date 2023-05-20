package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.GachaPoint

/**
 * レベルアップ報酬をプレイヤーに付与する algebra。
 */
trait GrantLevelUpGiftAlgebra[F[_], Player] {

  /**
   * プレイヤーに `item` に相当するアイテムを付与する。
   */
  def grantGiftItem(item: Gift.Item): Kleisli[F, Player, Unit]

  /**
   * プレイヤーにガチャポイントを付与する。
   */
  def grantGachaPoint(gachaPoint: GachaPoint): Kleisli[F, Player, Unit]

  /**
   * ガチャを一回回して、結果をプレイヤーに付与する。
   */
  def runGachaForPlayer: Kleisli[F, Player, Unit]

  final def grant(gift: Gift): Kleisli[F, Player, Unit] =
    gift match {
      case item: Gift.Item                  => grantGiftItem(item)
      case Gift.GachaPointWorthSingleTicket => grantGachaPoint(GachaPoint.perGachaTicket)
      case Gift.AutomaticGachaRun           => runGachaForPlayer
    }
}

object GrantLevelUpGiftAlgebra {

  def apply[F[_], Player](
    implicit ev: GrantLevelUpGiftAlgebra[F, Player]
  ): GrantLevelUpGiftAlgebra[F, Player] = implicitly

}

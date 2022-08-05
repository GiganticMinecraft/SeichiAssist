package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import cats.data.Kleisli

/**
 * アイテムギフトの付与を実行するインタプリタ。
 */
trait GiftItemInterpreter[F[_], Player] {

  def apply(item: Gift.Item): Kleisli[F, Player, Unit]

}

object GiftItemInterpreter {

  def apply[F[_], Player](
    implicit ev: GiftItemInterpreter[F, Player]
  ): GiftItemInterpreter[F, Player] = implicitly

}

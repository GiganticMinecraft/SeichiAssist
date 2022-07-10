package com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain

import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi

/**
 * アイテムギフトの付与を実行するインタプリタ。
 */
trait GiftItemInterpreter[F[_], G[_], Player] {

  def apply(item: Gift.Item)(
    implicit gachaPointApi: GachaPointApi[F, G, Player]
  ): Kleisli[F, Player, Unit]

}

object GiftItemInterpreter {

  def apply[F[_], G[_], Player](
    implicit ev: GiftItemInterpreter[F, G, Player]
  ): GiftItemInterpreter[F, G, Player] = implicitly

}

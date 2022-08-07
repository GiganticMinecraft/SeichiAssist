package com.github.unchama.minecraft.actions

import java.util.UUID

trait GetPlayerUUID[F[_]] {

  /**
   * プレイヤー名からプレイヤーの[[UUID]]を[[Option]]で返す作用
   */
  def byPlayerName(name: String): F[Option[UUID]]

}

object GetPlayerUUID {

  def apply[F[_]](implicit ev: GetPlayerUUID[F]): GetPlayerUUID[F] = ev

}

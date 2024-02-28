package com.github.unchama.seichiassist.subsystems.playerheadskin.domain

import java.util.UUID

trait PlayerHeadSkinPersistence[F[_]] {

  /**
   * @return `player`に一致するUUIDの、最後に確認されたプレイヤー名を取得する作用
   */
  def fetchLastSeenPlayerName(player: UUID): F[Option[String]]

}

package com.github.unchama.seichiassist.domain.minecraft

import java.util.UUID

trait UuidRepository[F[_]] {

  /**
   * プレーヤー名の文字列から対応する [[UUID]] を取得する
   */
  def getUuid(playerName: String): F[Option[UUID]]

}

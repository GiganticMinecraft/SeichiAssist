package com.github.unchama.seichiassist.subsystems.lastQuit.domain

import java.util.UUID

trait LastQuitPersistence[F[_]] {

  /**
   * 最終ログアウトを現在の日時で更新します
   */
  def updateLastQuitNow(uuid: UUID): F[Unit]

  /**
   * 最終ログアウト日時を取得します
   */
  def lastQuitDateTime(uuid: UUID): F[LastQuitDateTime]

}

package com.github.unchama.seichiassist.subsystems.lastquit.domain

import java.util.UUID

trait LastQuitPersistence[F[_]] {

  /**
   * @return 最終ログアウトを現在の日時で更新する作用
   */
  def updateLastQuitNow(uuid: UUID): F[Unit]

  /**
   * @return 最終ログアウト日時を取得する作用
   */
  def lastQuitDateTime(uuid: UUID): F[Option[LastQuitDateTime]]

}

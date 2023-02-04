package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain

trait UpdatePlayerScreenName[F[_], Player] {

  /**
   * @return プレイヤー名の色を変更する作用
   */
  def updatePlayerNameColor(player: Player): F[Unit]

}

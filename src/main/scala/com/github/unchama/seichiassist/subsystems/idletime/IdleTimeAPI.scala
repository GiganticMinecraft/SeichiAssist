package com.github.unchama.seichiassist.subsystems.idletime

import com.github.unchama.seichiassist.subsystems.idletime.domain.IdleMinute

trait IdleTimeAPI[F[_], Player] {

  /**
   * @return 現在の放置時間を取得する作用
   */
  def currentIdleMinute(player: Player): F[IdleMinute]

}

object IdleTimeAPI {

  def apply[F[_], Player](implicit ev: IdleTimeAPI[F, Player]): IdleTimeAPI[F, Player] = ev

}

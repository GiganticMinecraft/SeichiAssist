package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname

import com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain.IdleMinute

trait AwayScreenNameAPI[F[_], Player] {

  /**
   * @return 現在の放置時間を取得する作用
   */
  def currentIdleMinute(player: Player): F[IdleMinute]

}

object AwayScreenNameAPI {

  def apply[F[_], Player](
    implicit ev: AwayScreenNameAPI[F, Player]
  ): AwayScreenNameAPI[F, Player] = ev

}

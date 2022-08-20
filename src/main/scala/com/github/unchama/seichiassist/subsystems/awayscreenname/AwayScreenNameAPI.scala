package com.github.unchama.seichiassist.subsystems.awayscreenname

import com.github.unchama.seichiassist.subsystems.awayscreenname.domain.IdleMinute

import java.util.UUID

trait AwayScreenNameAPI[F[_]] {

  /**
   * @return 指定したUUIDプレイヤーの[[IdleMinute]]を返す作用
   */
  def idleTime(uuid: UUID): F[IdleMinute]

}

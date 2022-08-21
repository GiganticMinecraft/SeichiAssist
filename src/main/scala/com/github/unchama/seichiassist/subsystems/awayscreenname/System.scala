package com.github.unchama.seichiassist.subsystems.awayscreenname

import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.awayscreenname.domain.IdleMinute

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {

  val api: AwayScreenNameAPI[F]

}

object System {

  def wired[F[_]]: System[F] = {
    new System[F] {
      override val api: AwayScreenNameAPI[F] = new AwayScreenNameAPI[F] {

        /**
         * @return 指定したUUIDプレイヤーの[[IdleMinute]]を返す作用
         */
        override def idleTime(uuid: UUID): F[IdleMinute] = ???
      }
    }
  }

}

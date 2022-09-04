package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain

import com.github.unchama.seichiassist.subsystems.idletime.domain.IdleMinute

trait NameColorByIdleMinute[F[_], ChatColor] {

  /**
   * @return [[IdleMinute]]に紐づいた[[ChatColor]]を返す作用
   */
  def getNameColor(idleMinute: IdleMinute): F[ChatColor]

}

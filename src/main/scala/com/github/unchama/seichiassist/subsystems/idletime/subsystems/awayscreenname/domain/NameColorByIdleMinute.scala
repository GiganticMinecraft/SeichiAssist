package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain

import com.github.unchama.seichiassist.subsystems.idletime.domain.IdleMinute

trait NameColorByIdleMinute[ChatColor] {

  /**
   * この関数は決して例外を投げてはならず、全射でなければならない。
   *
   * @param idleMinute 放置時間
   * @return [[IdleMinute]]に基づいた[[ChatColor]]
   */
  def getNameColor(idleMinute: IdleMinute): ChatColor

}

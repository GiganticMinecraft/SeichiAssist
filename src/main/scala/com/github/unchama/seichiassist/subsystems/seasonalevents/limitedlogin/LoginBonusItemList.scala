package com.github.unchama.seichiassist.subsystems.seasonalevents.limitedlogin

trait LoginBonusItemList {
  protected val map: Map[Int, Set[LoginBonus]]
  def loginBonusAt(day: Int): Option[Set[LoginBonus]] = map.get(day)
}
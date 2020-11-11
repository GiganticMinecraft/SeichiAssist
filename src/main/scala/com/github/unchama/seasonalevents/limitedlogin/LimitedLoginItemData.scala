package com.github.unchama.seasonalevents.limitedlogin

object LimitedLoginItemData {
  private val itemDataMap = Map(
    (0, (1, 20)),
    (20, (1, 100))
  )

  def getItemData(day: Int): (Int, Int) =
    itemDataMap.get(day) match {
      case Some(pair) => pair
      case None if itemDataMap.contains(0) => itemDataMap(0)
      case _ => throw new NoSuchElementException
    }
}
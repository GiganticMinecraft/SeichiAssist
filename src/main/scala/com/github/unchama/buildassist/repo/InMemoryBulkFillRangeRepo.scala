package com.github.unchama.buildassist.repo

import org.bukkit.entity.Player
import scala.collection._

object InMemoryBulkFillRangeRepo {
  @inline
  val max = 11
  @inline
  val min = 3
  @inline
  val defaults = 5
  private val mapping = mutable.Map[Player, Int]().withDefaultValue(defaults)

  def update(player: Player, value: Int): Unit = {
    if (value % 2 == 0) throw new IllegalArgumentException("value must be odd")
    if (value > max || value < min) throw new IllegalArgumentException(s"value is out of range: $min .. $max is accepted")
    mapping.put(player, value)
  }

  def update(player: Player, map: (Player, Int) => Int): Unit = {
    update(player, map(player, get(player)))
  }

  def get(player: Player): Int = {
    mapping(player)
  }

  def drop(player: Player): Unit = {
    mapping.remove(player)
  }

  def dropAll(): Unit = {
    mapping.clear()
  }
}

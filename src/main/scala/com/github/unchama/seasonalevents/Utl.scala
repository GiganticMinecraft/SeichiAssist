package com.github.unchama.seasonalevents

import java.util.Random

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

object Utl {
  // TODO dropItem部分の共通化。isdropに注意
  def dropItem(entity: Entity, loc: Location, item: ItemStack): Unit = {
    val dp = SeasonalEvents.config.getDropRate
    val rand = new Random().nextInt(100)
    if (rand < dp) {
      // 報酬をドロップ
      entity.getWorld.dropItemNaturally(loc, item)
    }
  }
}
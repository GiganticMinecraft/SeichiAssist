package com.github.unchama.seasonalevents

import scala.util.Random

import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

object Utl {
  /**
   *指定されたEntityがいるLocationに、指定されたitemをドロップする
   *
   * @param entity 対象のエンティティ
   * @param item ドロップさせるItemStack
   */
  def dropItem(entity: Entity, item: ItemStack): Unit = {
    val rate = SeasonalEvents.config.getDropRate
    val rand = new Random().nextInt(100)
    if (rand < rate) {
      // 報酬をドロップ
      entity.getWorld.dropItemNaturally(entity.getLocation, item)
    }
  }
}
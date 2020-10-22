package com.github.unchama.seasonalevents

import java.util.Random

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
    val dp = SeasonalEvents.config.getDropRate
    val rand = new Random().nextInt(100)
    if (rand < dp) {
      // 報酬をドロップ
      entity.getWorld.dropItemNaturally(entity.getLocation, item)
    }
  }
}
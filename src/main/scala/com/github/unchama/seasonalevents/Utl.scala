package com.github.unchama.seasonalevents

import java.time.LocalDate

import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

import scala.util.Random

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

  def tryNewDate(year: Int, month: Int, daysOfMonth: Int): LocalDate = {
    try {
      LocalDate.of(year, month, daysOfMonth)
    } catch {
      case e: java.time.DateTimeException =>
        Bukkit.getServer.getLogger.severe("SeasonalEventsは日付の処理を正常に完了できませんでした。以下にエラーを表示します。")
        e.printStackTrace()
        LocalDate.of(2020,1,1)
    }
  }
}
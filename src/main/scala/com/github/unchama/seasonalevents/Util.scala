package com.github.unchama.seasonalevents

import java.time.LocalDate
import java.util.Random

import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

object Util {
  /**
   *指定されたEntityがいるLocationに、指定されたitemをドロップする
   *
   * @param entity 対象のエンティティ
   * @param item ドロップさせるItemStack
   */
  def randomlyDropItemAt(entity: Entity, item: ItemStack)(implicit config: SeasonalEventsConfig): Unit = {
    val rate = config.itemDropRate
    val rand = new Random().nextDouble() * 100
    if (rand < rate) entity.getWorld.dropItemNaturally(entity.getLocation, item)
  }

  /**
   * 指定された年月日を`LocalDate`に変換する。
   * @param year 年（西暦）
   * @param month 月
   * @param daysOfMonth 日
   * @return `LocalDate`
   */
  def localDateFromYearMonthDays(year: Int, month: Int, daysOfMonth: Int): LocalDate = LocalDate.of(year, month, daysOfMonth)
}

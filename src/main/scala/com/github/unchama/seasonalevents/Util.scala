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
  def randomlyDropItemAt(entity: Entity, item: ItemStack, rate: Double): Unit = {
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

  def validateItemDropRate(rate: Int): Double = Option(rate)
    .filter(rate => 0 <= rate && rate <= 100)
    .getOrElse(30)
    .toDouble

  def validateUrl(url: String): String = Option(url)
    .filter(_.startsWith("https://www.seichi.network/post/"))
    .getOrElse("https://www.seichi.network/blog/categories/%E3%82%A4%E3%83%99%E3%83%B3%E3%83%88%E6%83%85%E5%A0%B1")
}

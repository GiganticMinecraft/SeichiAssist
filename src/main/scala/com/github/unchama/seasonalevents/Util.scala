package com.github.unchama.seasonalevents

import java.util.Random

import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack

object Util {
  /**
   * 指定されたEntityがいるLocationに、指定されたitemをドロップする
   *
   * @param entity 対象のエンティティ
   * @param item   ドロップさせるItemStack
   */
  def randomlyDropItemAt(entity: Entity, item: ItemStack, rate: Double): Unit = {
    val rand = new Random().nextDouble() * 100
    if (rand < rate) entity.getWorld.dropItemNaturally(entity.getLocation, item)
  }

  /**
   * 引数で指定されたIntがドロップ率として適当な範囲（0または100以下の自然数）にあるかどうか検証し、Doubleにして返す
   *
   * @param rate ドロップ率
   * @return [[Double]]。適当な値であれば`rate.toDouble`、適当な値でなければ `30.toDouble`
   */
  def validateItemDropRate(rate: Int): Double = Option(rate)
    .filter(rate => 0 <= rate && rate <= 100)
    .getOrElse(30)
    .toDouble

  /**
   * 引数で指定されたStringが告知のブログ記事として適当なものかどうかを検証し、Stringを返す。
   *
   * @param url URL
   * @return [[String]]。適当であれば指定された`url`そのまま、適当でなければブログ記事のイベント情報一覧のURL
   */
  def validateUrl(url: String): String = Option(url)
    .filter(_.startsWith("https://www.seichi.network/post/"))
    .getOrElse("https://www.seichi.network/blog/categories/%E3%82%A4%E3%83%99%E3%83%B3%E3%83%88%E6%83%85%E5%A0%B1")
}

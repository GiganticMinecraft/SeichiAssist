package com.github.unchama.seasonalevents

import java.time.LocalDate
import java.time.temporal.ChronoUnit
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
    val rand = new Random().nextDouble()
    if (rand < rate) entity.getWorld.dropItemNaturally(entity.getLocation, item)
  }

  /**
   * 引数で指定されたDoubleがドロップ率として適当な範囲（0.0以上1.0以下の小数）にあるかどうか検証して返す
   *
   * @param rate ドロップ率
   * @return 適当な値であれば`rate`、適当な値でなければ`IllegalArgumentException`
   * @throws IllegalArgumentException 指定されたドロップ率が適切ではない
   */
  def validateItemDropRate(rate: Double): Double =
    if (0.0 <= rate && rate <= 1.0) rate
    else throw new IllegalArgumentException("適切ではないアイテムドロップ率が指定されました。")

  /**
   * 引数で指定されたStringが告知のブログ記事として適切なものかどうかを検証し、Stringを返す
   *
   * @param url URL
   * @return 適切であれば指定された`url`をそのまま返し、適切でなければ`IllegalArgumentException`を出す
   * @throws IllegalArgumentException 指定されたURLが適切ではない
   */
  def validateUrl(url: String): String =
    if (url.startsWith("https://www.seichi.network/post/")) url
    else throw new IllegalArgumentException("適切ではないURLが指定されました。")

  /**
   * 指定された期間に含まれるすべての日付を返す
   *
   * @param from 期間の開始日
   * @param to   期間の終了日
   * @return 期間に含まれるすべてのLocalDateをもつSeq
   * @see [[https://qiita.com/pictiny/items/357630e48043185da223 Qiita: Scalaで日付の範囲を指定してリストを作る]]
   * @throws IllegalArgumentException `from`に`to`より後の日付が指定された時
   */
  def dateRangeAsSequence(from: LocalDate, to: LocalDate): Seq[LocalDate] =
    if (from.isAfter(to)) throw new IllegalArgumentException("適切ではない期間が指定されました。")
    else Range(0, from.until(to, ChronoUnit.DAYS).toInt + 1).map(from.plusDays(_))
}

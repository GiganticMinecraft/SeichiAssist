package com.github.unchama.seasonalevents

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.{Random, UUID}

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

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
   * 引数で指定されたIntがドロップ率として適当な範囲（0以上100以下の整数）にあるかどうか検証し、Doubleにして返す
   *
   * @param rate ドロップ率
   * @return 適当な値であれば`rate.toDouble`、適当な値でなければ `30.toDouble`
   */
  def validateItemDropRate(rate: Int): Double = Option(rate)
    .filter(rate => 0 <= rate && rate <= 100)
    .getOrElse(30)
    .toDouble

  /**
   * 引数で指定されたDoubleがドロップ率として適当な範囲（0.0以上100.0以下の小数）にあるかどうか検証して返す
   *
   * @param rate ドロップ率
   * @return 適当な値であれば`rate`、適当な値でなければ `0.2`
   */
  def validateItemDropRate(rate: Double): Double = Option(rate)
    .filter(rate => 0 <= rate && rate <= 100)
    .getOrElse(0.2)

  /**
   * 引数で指定されたStringが告知のブログ記事として適当なものかどうかを検証し、Stringを返す
   *
   * @param url URL
   * @return 適当であれば指定された`url`そのまま、適当でなければブログ記事のイベント情報一覧のURLをそれぞれStringで返す
   */
  def validateUrl(url: String): String = Option(url)
    .filter(_.startsWith("https://www.seichi.network/post/"))
    .getOrElse("https://www.seichi.network/blog/categories/%E3%82%A4%E3%83%99%E3%83%B3%E3%83%88%E6%83%85%E5%A0%B1")

  /**
   * 指定された期間に含まれるすべての日付を返す
   *
   * @param from 期間の開始日
   * @param to   期間の終了日
   * @return 期間に含まれるすべてのLocalDateをもつSeq
   * @see [[https://qiita.com/pictiny/items/357630e48043185da223 Qiita: Scalaで日付の範囲を指定してリストを作る]]
   */
  def dateRangeAsSequence(from: LocalDate, to: LocalDate): Seq[LocalDate] =
    Range(0, from.until(to, ChronoUnit.DAYS).toInt + 1).map(from.plusDays(_))

  /**
   * `value`の中に含まれるテクスチャデータを適用したSkullのItemStackをOptionに包んで返す
   *
   * @param customHead [[com.github.unchama.seasonalevents.SkullData]]
   * @return Option[ItemStack]
   * @see [[https://www.spigotmc.org/threads/1-12-2-applying-custom-textures-to-skulls.327361/  カスタムヘッドを生成するコード]]
   * @see [[https://qiita.com/yuta0801/items/edb4804dfb867ea82c5a テクスチャへのリンク周り]]
   */
  def createCustomHead(customHead: SkullData): Option[ItemStack] = {
    val skull = new ItemStack(Material.SKULL_ITEM, 1, 3.toShort)
    // ↑のMaterialをSKULL_ITEM以外にしなければ、↓のmatch caseは_の方には進まないはず（返り値がNoneにはならないはず）
    skull.getItemMeta match {
      case meta: SkullMeta =>
        val gameProfile = new GameProfile(UUID.randomUUID, null)
        gameProfile.getProperties.put("textures", new Property("textures", customHead.textureValue, ""))

        val profileField = meta.getClass.getDeclaredField("profile")
        profileField.setAccessible(true)
        profileField.set(meta, gameProfile)

        skull.setItemMeta(meta)
        Some(skull)
      case _ => None
    }
  }
}

package com.github.unchama.seasonalevents.valentine

import java.time.LocalDate
import java.util.{Random, UUID}

import com.github.unchama.seichiassist.util.Util
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor._
import org.bukkit.{Bukkit, Material, Sound}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.potion.{PotionEffect, PotionEffectType}

import scala.jdk.CollectionConverters._
import scala.util.chaining._

object ValentineItemData {
  private val baseLore = List(
    s"${GRAY}食べると一定時間ステータスが変化する。",
    s"${GRAY}賞味期限を超えると効果が無くなる。",
    "",
    s"${DARK_GREEN}消費期限：${Valentine.DISPLAYED_END_DATE}",
    s"${AQUA}ステータス変化（10分）$GRAY （期限内）"
  ).map(str => s"$RESET$str")

  private val cookieName = s"$GOLD${BOLD}チョコチップクッキー"

  def isValidCookie(item: ItemStack): Boolean = {
    val today = LocalDate.now()
    val exp = new NBTItem(item).getObject(NBTTagConstants.expirationDateTag, classOf[LocalDate])
    today.isBefore(exp)
  }

  //region DroppedCookie -> 爆発したmobからドロップするやつ

  val droppedCookie: ItemStack = {
    val loreList = {
      List(
        "",
        s"$RESET${GRAY}リア充を爆発させて奪い取った。"
      ) ++ baseLore
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKIE)
      .tap(_.setDisplayName(cookieName))
      .tap(_.setLore(loreList))

    val itemStack = new ItemStack(Material.COOKIE, 1)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap(_.setByte(NBTTagConstants.typeIdTag, 1.toByte))
      .tap(_.setObject(NBTTagConstants.expirationDateTag, Valentine.END_DATE))
      .pipe(_.getItem)
  }

  def isDroppedCookie(item: ItemStack): Boolean =
    item != null && item.getType != Material.AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 1
    }

  def useDroppedCookie(player: Player): Unit = {
    val potionEffects = Map(
      "火炎耐性" -> new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 60 * 10, 1),
      "暗視" -> new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 60 * 10, 1),
      "耐性" -> new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 60 * 10, 1),
      "跳躍力上昇" -> new PotionEffect(PotionEffectType.JUMP, 20 * 60 * 10, 1),
      "再生能力" -> new PotionEffect(PotionEffectType.REGENERATION, 20 * 60 * 10, 1),
      "移動速度上昇" -> new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 10, 1),
      "水中呼吸" -> new PotionEffect(PotionEffectType.WATER_BREATHING, 20 * 60 * 10, 1),
      "緩衝吸収" -> new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60 * 10, 1),
      "攻撃力上昇" -> new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 60 * 10, 1),
      "不運" -> new PotionEffect(PotionEffectType.UNLUCK, 20 * 60, 1)
    )
    val effectsName = potionEffects.keys.toSeq
    val effects = potionEffects.values.toSeq
    val num: Int = new Random().nextInt(potionEffects.size)
    val msg =
      if (num == 9) s"${effectsName(num)}IIを感じてしまった…はぁ…むなしいなぁ…"
      else s"${effectsName(num)}IIを奪い取った！あぁ、おいしいなぁ！"

    player
      .tap(_.addPotionEffect(effects(num)))
      .tap(_.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F))
      .tap(_.sendMessage(msg))
  }

  //endregion

  //region GiftedCookie -> 棒メニューでもらえるやつ

  private def giftedCookie(player: Player) = {
    val playerName = player.getName
    val loreList = {
      val header = List(
        "",
        s"$RESET${GRAY}手作りのチョコチップクッキー。")
      val producer = List(s"$RESET${DARK_GREEN}製作者：$playerName")

      header ++ baseLore ++ producer
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKIE)
      .tap(_.setDisplayName(cookieName))
      .tap(_.setLore(loreList))

    val itemStack = new ItemStack(Material.COOKIE, 64)
    itemStack.setItemMeta(itemMeta)

    new NBTItem(itemStack)
      .tap(_.setByte(NBTTagConstants.typeIdTag, 2.toByte))
      .tap(_.setObject(NBTTagConstants.expirationDateTag, Valentine.END_DATE))
      .tap(_.setObject(NBTTagConstants.producerUuidTag, player.getUniqueId))
      .tap(_.setString(NBTTagConstants.producerNameTag, playerName))
      .pipe(_.getItem)
  }

  def isGiftedCookie(item: ItemStack): Boolean =
    item != null && item.getType != Material.AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 2
    }

  def useGiftedCookie(player: Player, item: ItemStack): Unit = {
    val playerName = player.getName
    val cookieProducerName = new NBTItem(item).getString(NBTTagConstants.producerNameTag)
    val messages = Seq(
      s"${playerName}は${cookieProducerName}のチョコレートを食べた！猟奇的な味だった。",
      s"$playerName！${cookieProducerName}からのチョコだと思ったかい？ざぁんねんっ！",
      s"${playerName}は${cookieProducerName}のプレゼントで鼻血が止まらない！（計画通り）",
      s"${playerName}は${cookieProducerName}のチョコレートを頬張ったまま息絶えた！",
      s"${playerName}は${cookieProducerName}のチョコにアレが入っているとはを知らずに食べた…",
      s"${playerName}は${cookieProducerName}のチョコなんか食ってないであくしろはたらけ",
      s"${cookieProducerName}は${playerName}に日頃の恨みを晴らした！スッキリ！",
      s"${cookieProducerName}による${playerName}への痛恨の一撃！ハッピーバレンタインッ！",
      s"${cookieProducerName}は${playerName}が食べる姿を、満面の笑みで見つめている！",
      s"${cookieProducerName}は悪くない！${playerName}が悪いんだっ！",
      s"${cookieProducerName}は${playerName}を討伐した！",
      s"こうして${cookieProducerName}のイタズラでまた1人${playerName}が社畜となった。",
      s"おい聞いたか！${cookieProducerName}が${playerName}にチョコ送ったらしいぞー！"
    )
    if (isCookieSender(item, player.getUniqueId)) {
      // HP最大値アップ
      player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 20 * 60 * 10, 10))
    } else {
      // 死ぬ
      player.setHealth(0)
      // 全体にメッセージ送信
      Util.sendEveryMessage(messages(new Random().nextInt(messages.size)))
    }
    player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
  }

  private def isCookieSender(item: ItemStack, uuid: UUID): Boolean =
    uuid == new NBTItem(item).getObject(NBTTagConstants.producerUuidTag, classOf[UUID])

  // SeichiAssistで呼ばれてるだけ
  // 棒メニューで使われるログイン時のクッキー配布処理
  def giveCookie(player: Player): Unit = {
    if (Util.isPlayerInventoryFull(player)) Util.dropItem(player, giftedCookie(player))
    else Util.addItem(player, giftedCookie(player))
  }

  //endregion

  // SeichiAssistで呼ばれてるだけ
  def valentinePlayerHead(head: SkullMeta): SkullMeta = {
    if (Valentine.isDrop) {
      val year: String = Valentine.DROP_END_DATE.getYear.toString
      val lore = List(
        "",
        s"$GREEN${ITALIC}大切なあなたへ。",
        s"$UNDERLINE$YELLOW${ITALIC}Happy Valentine $year"
      ).map(str => s"$RESET$str")
        .asJava
      head.setLore(lore)
    }
    head
  }

  private object NBTTagConstants {
    val typeIdTag = "valentineCookieTypeId"
    val expirationDateTag = "valentineCookieExpirationDate"
    val producerNameTag = "valentineCookieProducerName"
    val producerUuidTag = "valentineCookieProducerUuid"
  }
}
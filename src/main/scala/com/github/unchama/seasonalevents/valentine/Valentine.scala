package com.github.unchama.seasonalevents.valentine

import java.text.{ParseException, SimpleDateFormat}
import java.util.{Date, Random, UUID}

import com.github.unchama.seasonalevents.SeasonalEvents
import com.github.unchama.seasonalevents.Utl
import de.tr7zw.itemnbtapi.NBTItem
import com.github.unchama.seichiassist.util.Util
import org.bukkit.{Bukkit, Material, Sound}
import org.bukkit.ChatColor._
import org.bukkit.entity.{Monster, Player}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.{EntityDeathEvent, EntityExplodeEvent}
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.Plugin
import org.bukkit.potion.{PotionEffect, PotionEffectType}

import scala.jdk.CollectionConverters._

class Valentine(private val plugin: Plugin) extends Listener {
  private var isdrop = false
  private val config = SeasonalEvents.config
  /*
	時間に関してだが、Date#beforeは指定した日付よりも前->true 後->false
	つまり2018-02-20の場合は、2018/02/20 00:00 よりも前ならtrueを返します。
	鯖再起動は4時なので、その際に判定が行われる関係で2018/02/20 4時までが期限となります。
	*/
  private val DROPDAY = config.getDropFinishDay
  private val DROPDAYDISP = config.getDropFinishDayDisp
  private val FINISH = config.getEventFinishDay
  private val FINISHDISP = config.getEventFinishDayDisp

  try {
    // イベント開催中か判定
    val format = new SimpleDateFormat("yyyy-MM-dd")
    // TODO Objectの中に移動する
    val finishdate = format.parse(FINISH)
    val dropdate = format.parse(DROPDAY)

    val now = new Date
    if (now.before(finishdate)) {
      // リスナーを登録
      plugin.getServer.getPluginManager.registerEvents(this, plugin)
      Valentine.isInEvent = true
    }
    if (now.before(dropdate)) isdrop = true
  } catch {
    case e: ParseException =>
      e.printStackTrace()
  }

  //region Listener

  @EventHandler
  def onEntityExplode(event: EntityExplodeEvent): Unit = {
    val entity = event.getEntity
    if (!isdrop || entity == null) return

    if (entity.isInstanceOf[Monster] && entity.isDead){
      Utl.dropItem(entity, droppedCookie)
    }
  }

  // TODO TNTで爆破死した敵からも出るのを直す
  // TODO 爆破死したモンスター以外のmob(スノーゴーレム、プレイヤーなど)からもチョコチップクッキーが出る
  @EventHandler
  def onEntityDeath(event: EntityDeathEvent): Unit = {
    val entity = event.getEntity
    if (!isdrop || entity == null) return

    if (entity.getLastDamageCause.getCause == DamageCause.ENTITY_EXPLOSION) {
      // 死因が爆発の場合、確率でアイテムをドロップ
      Utl.dropItem(entity, droppedCookie)
    }
  }

  @EventHandler
  def onPlayerJoinEvent(event: PlayerJoinEvent): Unit = {
    if (isdrop) {
      Seq(
        s"$LIGHT_PURPLE${DROPDAYDISP}までの期間限定で、シーズナルイベント『＜ブラックバレンタイン＞リア充 vs 整地民！』を開催しています。",
        "詳しくは下記wikiをご覧ください。",
        s"$DARK_GREEN$UNDERLINE${SeasonalEvents.config.getWikiAddr}"
      ).foreach(
        event.getPlayer.sendMessage(_)
      )
    }
  }

  @EventHandler
  def onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent): Unit = {
    val item = event.getItem
    val player = event.getPlayer
    if (isDroppedCookie(item) && isValidCookie(item)) useDroppedCookie(player)
    if (isGiftedCookie(item) && isValidCookie(item)) useGiftedCookie(player, item)
  }

  //endregion

  //region ItemData

  /*
  Prize = DroppedCookie -> 爆発したmobからドロップするやつ
  Choco = GiftedCookie -> 棒メニューでもらえるやつ
   */

  private val baseLore = List(
    s"$RESET${GRAY}食べると一定時間ステータスが変化する。",
    s"$RESET${GRAY}賞味期限を超えると効果が無くなる。",
    "",
    s"$RESET${DARK_GREEN}消費期限：$FINISHDISP",
    s"$RESET${AQUA}ステータス変化（10分）$GRAY （期限内）"
  )

  private val cookieName = s"$GOLD${BOLD}チョコチップクッキー"

  private def isValidCookie(item: ItemStack) = {
    val now = new Date()
    // TODO 時刻は比較しない
    new NBTItem(item).getObject(NBTTagConstants.expirationDateTag, classOf[Date]).after(now)
  }

  //region Prize = DroppedCookie -> 爆発したmobからドロップするやつ

  private val droppedCookie = {
    val loreList = {
      val header = List(
        "",
        s"$RESET${GRAY}リア充を爆発させて奪い取った。")
      header ++ baseLore
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKIE)
    itemMeta.setDisplayName(cookieName)
    itemMeta.setLore(loreList)

    val cookie = new ItemStack(Material.COOKIE, 1)
    cookie.setItemMeta(itemMeta)

    val nbtItem = new NBTItem(cookie)
    nbtItem.setByte(NBTTagConstants.typeIdTag, 1.toByte)
    val n: Date = new SimpleDateFormat("yyyy-MM-dd").parse(FINISH)
    nbtItem.setObject(NBTTagConstants.expirationDateTag, n)
    nbtItem.getItem
  }

  private def isDroppedCookie(item: ItemStack) =
    item != null && item.getType != Material.AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 1
    }

  // アイテム使用時の処理
  private def useDroppedCookie(player: Player): Unit = {
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

    // TODO tap
    player.addPotionEffect(effects(num))
    player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
    player.sendMessage(msg)
  }

  //endregion

  //region Choco = GiftedCookie -> 棒メニューでもらえるやつ

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
    // TODO tap
    itemMeta.setDisplayName(cookieName)
    itemMeta.setLore(loreList)

    val cookie = new ItemStack(Material.COOKIE, 64)
    cookie.setItemMeta(itemMeta)

    val nbtItem = new NBTItem(cookie)
    // TODO tap
    nbtItem.setByte(NBTTagConstants.typeIdTag, 2.toByte)
    // FIXME finishDateがObjectにいったら
    val n: Date = new SimpleDateFormat("yyyy-MM-dd").parse(FINISH)
    nbtItem.setObject(NBTTagConstants.expirationDateTag, n)
    nbtItem.setString(NBTTagConstants.producerNameTag, playerName)
    nbtItem.setObject(NBTTagConstants.producerUuidTag, player.getUniqueId)
    nbtItem.getItem
  }

  private def isGiftedCookie(item: ItemStack) =
    item != null && item.getType != Material.AIR && {
      new NBTItem(item).getByte(NBTTagConstants.typeIdTag) == 2
    }

  private def useGiftedCookie(player: Player, item: ItemStack): Unit = {
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
      player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 12000, 10))
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
    if (isdrop) {
      val prefix: String = DROPDAY.substring(0, 4)
      val lore = List(
        "",
        s"$RESET$ITALIC${GREEN}大切なあなたへ。",
        s"$RESET$ITALIC$UNDERLINE${YELLOW}Happy Valentine $prefix"
      ).asJava
      head.setLore(lore)
    }
    head
  }

  //endregion

  private object NBTTagConstants {
    val typeIdTag = "valentineCookieTypeId"
    val expirationDateTag = "valentineCookieExpirationDate"
    val producerNameTag = "valentineCookieProducerName"
    val producerUuidTag = "valentineCookieProducerUuid"
  }
}

object Valentine {
  var isInEvent: Boolean = false
}
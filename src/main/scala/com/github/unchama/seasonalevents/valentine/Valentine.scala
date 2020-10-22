package com.github.unchama.seasonalevents.valentine

import java.text.{ParseException, SimpleDateFormat}
import java.util
import java.util.{Date, Random}

import com.github.unchama.seasonalevents.SeasonalEvents
import com.github.unchama.seasonalevents.Utl
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

import scala.collection.mutable
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
    val finishdate = format.parse(FINISH)
    val dropdate = format.parse(DROPDAY)

    val now = new Date
    if (now.before(finishdate)) {
      // リスナーを登録
      plugin.getServer.getPluginManager.registerEvents(this, plugin)
      Valen.isInEvent = true
    }
    if (now.before(dropdate)) isdrop = true
  } catch {
    case e: ParseException =>
      e.printStackTrace()
  }

  //region Listener

  @EventHandler
  def onEntityExplode(event: EntityExplodeEvent): Unit = {
    if (!isdrop) return

    val entity = event.getEntity
    if (entity == null) return

    if (entity.isInstanceOf[Monster] && entity.isDead){
//      killEvent(event.getEntity, event.getEntity.getLocation)
      Utl.dropItem(entity, entity.getLocation, droppedCookie)
    }
  }

  // TODO TNTで爆破死した敵からも出るのを直す
  // TODO 爆破死したモンスター以外のmob(スノーゴーレム、プレイヤーなど)からもチョコチップクッキーが出る
  @EventHandler
  def onEntityDeath(event: EntityDeathEvent): Unit = {
    if (!isdrop) return

    val entity = event.getEntity
    if (entity == null) return

    if (entity.getLastDamageCause.getCause == DamageCause.ENTITY_EXPLOSION) {
      // 死因が爆発の場合、確率でアイテムをドロップ
      Utl.dropItem(entity, entity.getLocation, droppedCookie)
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
    if (isDroppedCookie(item)) useDroppedCookie(player)
    if (isGiftedCookie(item)) useGiftedCookie(player, item)
  }

  //endregion

  //region ItemData

  /*
  Choco = GiftedCookie -> 棒メニューでもらえるやつ
  Prize = DroppedCookie -> 爆発したmobからドロップするやつ
   */

  private val baseLore = List(
    s"$RESET${GRAY}食べると一定時間ステータスが変化する。",
    s"$RESET${GRAY}賞味期限を超えると効果が無くなる。",
    "",
    s"$RESET${DARK_GREEN}賞味期限：$FINISHDISP",
    s"$RESET${AQUA}ステータス変化（10分）$GRAY （期限内）"
  )

  private val cookieName = s"$GOLD${BOLD}チョコチップクッキー"

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

    val prize = new ItemStack(Material.COOKIE, 1)
    prize.setItemMeta(itemMeta)
    prize
  }

  // TODO NBT化？　賞味期限があることに注意　製作者も？
  // チョコレート判定
  private def isGiftedCookie(item: ItemStack): Boolean = {
//    if (!item.hasItemMeta || !item.getItemMeta.hasLore) return false
//    val lore: util.List[String] = item.getItemMeta.getLore
//    val plore: util.List[String] = getChocoLore
//    lore.containsAll(plore)
  }

  // チョコチップクッキー判定
  private def isDroppedCookie(item: ItemStack): Boolean = {
    // Lore取得
//    if (!item.hasItemMeta || !item.getItemMeta.hasLore) return false
//    val lore: util.List[String] = item.getItemMeta.getLore
//    val plore: util.List[String] = valentineCookieLore
//    // 比較
//    lore.containsAll(plore)
  }

  private def useGiftedCookie(player: Player, item: ItemStack): Unit = {
    val messages = Seq(
      s"${player.getName}は${getCookieProducer(item)}のチョコレートを食べた！猟奇的な味だった。",
      s"${player.getName}！${getCookieProducer(item)}からのチョコだと思ったかい？ざぁんねんっ！",
      s"${player.getName}は${getCookieProducer(item)}のプレゼントで鼻血が止まらない！（計画通り）",
      s"${player.getName}は${getCookieProducer(item)}のチョコレートを頬張ったまま息絶えた！",
      s"${player.getName}は${getCookieProducer(item)}のチョコにアレが入っているとはを知らずに食べた…",
      s"${player.getName}は${getCookieProducer(item)}のチョコなんか食ってないであくしろはたらけ",
      s"${getCookieProducer(item)}は${player.getName}に日頃の恨みを晴らした！スッキリ！",
      s"${getCookieProducer(item)}による${player.getName}への痛恨の一撃！ハッピーバレンタインッ！",
      s"${getCookieProducer(item)}は${player.getName}が食べる姿を、満面の笑みで見つめている！",
      s"${getCookieProducer(item)}は悪くない！${player.getName}が悪いんだっ！",
      s"${getCookieProducer(item)}は${player.getName}を討伐した！",
      s"こうして${getCookieProducer(item)}のイタズラでまた1人${player.getName}が社畜となった。",
      s"おい聞いたか！${getCookieProducer(item)}が${player.getName}にチョコ送ったらしいぞー！"
    )
    if (isCookieSender(item, player.getName)) {
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

  private def isCookieSender(item: ItemStack, owner: String): Boolean = {
    getCookieProducer(item) == owner
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

  // TODO 変数名これでいいの？prefixなだけで、別に製作者の名前自体は入ってないよね？
  private val producerName = s"$RESET${DARK_GREEN}製作者："

  private def getCookieProducer(item: ItemStack) = {
    var owner: String = "名称未設定"
    try {
      val lore: util.List[String] = item.getItemMeta.getLore
      val ownerRow: String = lore.get(lore.size - 1)
      if (ownerRow.contains(producerName)) owner = ownerRow.replace(producerName, "")
    } catch {
      case e: NullPointerException => e.printStackTrace()
    }
    owner
  }

  //region これらはSeichiAssistで呼ばれてるだけ

  def valentinePlayerHead(head: SkullMeta): SkullMeta = {
    if (isdrop) {
      val prefix: String = DROPDAY.substring(0, 4)
      val lore = List(
        "",
        s"$RESET$ITALIC${GREEN}大切なあなたへ。",
        s"$RESET$ITALIC$UNDERLINE${YELLOW}Happy Valentine $prefix"
      )
      head.setLore(lore)
    }
    head
  }

  // チョコレート配布
  def giveCookie(player: Player): Unit = {
    if (Util.isPlayerInventoryFull(player)) Util.dropItem(player, giftedCookie(player))
    else Util.addItem(player, giftedCookie(player))
  }

  private def giftedCookie(player: Player): ItemStack = {
    val loreList = {
      val header = List(
        "",
        s"$RESET${GRAY}手作りのチョコチップクッキー。")
      val producer = List(s"$RESET$GRAY$producerName${player.getName}")

      header ++ baseLore ++ producer
    }.asJava

    val itemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKIE)
    itemMeta.setDisplayName(cookieName)
    itemMeta.setLore(loreList)

    val choco = new ItemStack(Material.COOKIE, 64)
    choco.setItemMeta(itemMeta)
    choco
  }

  //endregion

  //endregion
}

object Valentine {
  var isInEvent: Boolean = false
}
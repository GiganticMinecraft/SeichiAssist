package com.github.unchama.seasonalevents.event

import java.text.{ParseException, SimpleDateFormat}
import java.util
import java.util.{Date, Random}

import com.github.unchama.seasonalevents.SeasonalEvents
import com.github.unchama.seichiassist.util.Util
import org.bukkit.entity.{Entity, Monster, Player}
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.{EntityDeathEvent, EntityExplodeEvent}
import org.bukkit.event.player.{PlayerItemConsumeEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.{ItemMeta, SkullMeta}
import org.bukkit.plugin.Plugin
import org.bukkit.potion.{PotionEffect, PotionEffectType}
import org.bukkit.{Bukkit, ChatColor, Location, Material, Sound}

import scala.jdk.CollectionConverters._

class Valentine(private val plugin: Plugin) extends Listener {
  private var isdrop = false
  var isInEvent = false
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

  try { // イベント開催中か判定
    val format = new SimpleDateFormat("yyyy-MM-dd")
    val finishdate = format.parse(FINISH)
    val dropdate = format.parse(DROPDAY)

    val now = new Date
    if (now.before(finishdate)) {
      // リスナーを登録
      plugin.getServer.getPluginManager.registerEvents(this, plugin)
      isInEvent = true
    }
    if (now.before(dropdate)) isdrop = true
  } catch {
    case e: ParseException =>
      e.printStackTrace()
  }

  def playerHeadMeta(head: SkullMeta): SkullMeta = {
    if (isdrop) {
      head.setLore(playerHeadLoreList)
    }
    head
  }

  private def playerHeadLoreList = {
    val year = DROPDAY.substring(0, 4)
    List(
      "",
      s"${ChatColor.RESET}${ChatColor.ITALIC}${ChatColor.GREEN}大切なあなたへ。",
      s"${ChatColor.RESET}${ChatColor.ITALIC}${ChatColor.UNDERLINE}${ChatColor.YELLOW}Happy Valentine $year"
    ).asJava
  }

  @EventHandler
  def onEntityExplode(event: EntityExplodeEvent): Unit = {
    try if (event.getEntity.isInstanceOf[Monster] && event.getEntity.isDead)
      killEvent(event.getEntity, event.getEntity.getLocation)
    catch {
      case e: NullPointerException => e.printStackTrace()
    }
  }

  @EventHandler
  def onEntityDeath(event: EntityDeathEvent): Unit = {
    try if (event.getEntity.getLastDamageCause.getCause == DamageCause.ENTITY_EXPLOSION) {
      // 死因が爆発の場合、確率でアイテムをドロップ
      killEvent(event.getEntity, event.getEntity.getLocation)
    }
    catch {
      case e: NullPointerException => e.printStackTrace()
    }
  }

  @EventHandler
  def onplayerJoinEvent(event: PlayerJoinEvent): Unit = {
    try if (isdrop) {
      List(
        s"${ChatColor.LIGHT_PURPLE}${DROPDAYDISP}までの期間限定で、シーズナルイベント『＜ブラックバレンタイン＞リア充 vs 整地民！』を開催しています。",
        "詳しくは下記wikiをご覧ください。",
        s"${ChatColor.DARK_GREEN}${ChatColor.UNDERLINE}${SeasonalEvents.config.getWikiAddr}"
      ).foreach(
        event.getPlayer.sendMessage(_)
      )
    }
    catch {
      case e: NullPointerException => e.printStackTrace()
    }
  }

  @EventHandler
  def onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent): Unit = {
    try {
      if (checkPrize(event.getItem)) usePrize(event.getPlayer)
      if (isChoco(event.getItem)) useChoco(event.getPlayer, event.getItem)
    } catch {
      case e: NullPointerException => e.printStackTrace()
    }
  }

  // プレイヤーにクリーパーが倒されたとき発生
  private def killEvent(entity: Entity, loc: Location): Unit = {
    if (isdrop) {
      val dp = SeasonalEvents.config.getDropPer
      val rand = new Random().nextInt(100)
      if (rand < dp) {
        // 報酬をドロップ
        entity.getWorld.dropItemNaturally(loc, makePrize)
      }
    }
  }

  // チョコチップクッキー判定
  private def checkPrize(item: ItemStack): Boolean = {
    // Lore取得
    if (!item.hasItemMeta || !item.getItemMeta.hasLore) return false
    val lore: util.List[String] = item.getItemMeta.getLore
    val plore: util.List[String] = getPrizeLore
    // 比較
    lore.containsAll(plore)
  }

  // アイテム使用時の処理
  private def usePrize(player: Player): Unit = {
    val ef = List(
      new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 12000, 1),
      new PotionEffect(PotionEffectType.NIGHT_VISION, 12000, 1),
      new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 12000, 1),
      new PotionEffect(PotionEffectType.JUMP, 12000, 1),
      new PotionEffect(PotionEffectType.REGENERATION, 12000, 1),
      new PotionEffect(PotionEffectType.SPEED, 12000, 1),
      new PotionEffect(PotionEffectType.WATER_BREATHING, 12000, 1),
      new PotionEffect(PotionEffectType.ABSORPTION, 12000, 1),
      new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 12000, 1),
      new PotionEffect(PotionEffectType.UNLUCK, 1200, 1)
    )
    val msg = Set("火炎耐性", "暗視", "耐性", "跳躍力上昇", "再生能力", "移動速度上昇", "水中呼吸", "緩衝吸収", "攻撃力上昇", "不運")
    val ran: Int = new Random().nextInt(ef.size)
    if (ran != 9) {
      player.addPotionEffect(ef(ran))
      player.sendMessage(msg(ran) + " IIを奪い取った！あぁ、おいしいなぁ！")
    }
    else {
      player.addPotionEffect(ef(ran))
      player.sendMessage(msg(ran) + " IIを感じてしまった…はぁ…むなしいなぁ…")
    }
    player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
  }

  private def makePrize: ItemStack = {
    val prize: ItemStack = new ItemStack(Material.COOKIE, 1)
    val itemmeta: ItemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKIE)
    itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "チョコチップクッキー")
    itemmeta.setLore(getPrizeLore)
    prize.setItemMeta(itemmeta)
    prize
  }

  private def getPrizeLore = List(
    "",
    s"${ChatColor.RESET}${ChatColor.GRAY}リア充を爆発させて奪い取った。",
    "食べると一定時間ステータスが変化する。",
    "賞味期限を超えると効果が無くなる。",
    "",
    s"${ChatColor.RESET}${ChatColor.DARK_GREEN}賞味期限：$FINISHDISP",
    s"${ChatColor.RESET}${ChatColor.AQUA}ステータス変化（10分）${ChatColor.GRAY} （期限内）"
  ).asJava

  // チョコレート配布
  def giveChoco(player: Player): Unit = {
    if (Util.isPlayerInventoryFull(player)) Util.dropItem(player, makeChoco(player))
    else Util.addItem(player, makeChoco(player))
  }

  // チョコレート判定
  private def isChoco(item: ItemStack): Boolean = {
    if (!item.hasItemMeta || !item.getItemMeta.hasLore) return false
    val lore: util.List[String] = item.getItemMeta.getLore
    val plore: util.List[String] = getChocoLore
    lore.containsAll(plore)
  }

  private def useChoco(player: Player, item: ItemStack): Unit = {
    val msg = Set(
      s"${player.getName}は${getChocoOwner(item)}のチョコレートを食べた！猟奇的な味だった。",
      s"${player.getName}！${getChocoOwner(item)}からのチョコだと思ったかい？ざぁんねんっ！",
      s"${player.getName}は${getChocoOwner(item)}のプレゼントで鼻血が止まらない！（計画通り）",
      s"${player.getName}は${getChocoOwner(item)}のチョコレートを頬張ったまま息絶えた！",
      s"${player.getName}は${getChocoOwner(item)}のチョコにアレが入っているとはを知らずに食べた…",
      s"${player.getName}は${getChocoOwner(item)}のチョコなんか食ってないであくしろはたらけ",
      s"${getChocoOwner(item)}は${player.getName}に日頃の恨みを晴らした！スッキリ！",
      s"${getChocoOwner(item)}による${player.getName}への痛恨の一撃！ハッピーバレンタインッ！",
      s"${getChocoOwner(item)}は${player.getName}が食べる姿を、満面の笑みで見つめている！",
      s"${getChocoOwner(item)}は悪くない！${player.getName}が悪いんだっ！",
      s"${getChocoOwner(item)}は${player.getName}を討伐した！",
      s"こうして${getChocoOwner(item)}のイタズラでまた1人${player.getName}が社畜となった。",
      s"おい聞いたか！${getChocoOwner(item)}が${player.getName}にチョコ送ったらしいぞー！"
    )
    if (isChocoOwner(item, player.getName)) {
      // HP最大値アップ
      player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 12000, 10))
    }
    else {
      // 死ぬ
      player.setHealth(0)
      // 全体にメッセージ送信
      Util.sendEveryMessage(msg(new Random().nextInt(msg.size)))
    }
    player.playSound(player.getLocation, Sound.ENTITY_WITCH_DRINK, 1.0F, 1.2F)
  }

  private def makeChoco(player: Player): ItemStack = {
    val choco: ItemStack = new ItemStack(Material.COOKIE, 64)
    val itemmeta: ItemMeta = Bukkit.getItemFactory.getItemMeta(Material.COOKIE)
    itemmeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "チョコチップクッキー")
    choco.setItemMeta(itemmeta)
    setChocoLore(choco)
    setChocoOwner(choco, player.getName)
    choco
  }

  private def isChocoOwner(item: ItemStack, owner: String): Boolean = {
    getChocoOwner(item) == owner
  }

  private def setChocoLore(item: ItemStack): Unit = {
    try {
      val meta: ItemMeta = item.getItemMeta
      meta.setLore(getChocoLore)
      item.setItemMeta(meta)
    } catch {
      case e: NullPointerException => e.printStackTrace()
    }
  }

  private def getChocoLore = List(
    "",
    s"${ChatColor.RESET}${ChatColor.GRAY}手作りのチョコチップクッキー。",
    "食べると一定時間ステータスが変化する。",
    "賞味期限を超えると効果が無くなる。",
    "",
    s"${ChatColor.RESET}${ChatColor.DARK_GREEN}賞味期限：$FINISHDISP",
    s"${ChatColor.RESET}${ChatColor.AQUA}ステータス変化（10分）${ChatColor.GRAY} （期限内）"
  ).asJava

  private val CHOCO_HEAD = s"${ChatColor.RESET}${ChatColor.DARK_GREEN}製作者："

  private def setChocoOwner(item: ItemStack, owner: String): Unit = {
    try {
      val meta: ItemMeta = item.getItemMeta
      val lore: util.List[String] = meta.getLore
      lore.add(CHOCO_HEAD + owner)
      meta.setLore(lore)
      item.setItemMeta(meta)
    } catch {
      case e: NullPointerException => e.printStackTrace()
    }
  }

  private def getChocoOwner(item: ItemStack) = {
    var owner: String = "名称未設定"
    try {
      val lore: util.List[String] = item.getItemMeta.getLore
      val ownerRow: String = lore.get(lore.size - 1)
      if (ownerRow.contains(CHOCO_HEAD)) owner = ownerRow.replace(CHOCO_HEAD, "")
    } catch {
      case e: NullPointerException => e.printStackTrace()
    }
    owner
  }
}
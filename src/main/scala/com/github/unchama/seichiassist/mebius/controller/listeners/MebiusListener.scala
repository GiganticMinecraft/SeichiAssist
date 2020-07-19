package com.github.unchama.seichiassist.mebius.controller.listeners

import java.util.Objects

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.mebius.domain.{MebiusEnchantment, MebiusTalk}
import com.github.unchama.seichiassist.util.Util
import de.tr7zw.itemnbtapi.NBTItem
import org.bukkit.ChatColor.{RED, RESET}
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{Monster, Player}
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.{EntityDamageByEntityEvent, EntityDeathEvent}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryDragEvent}
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.{AnvilInventory, ItemFlag, ItemStack}
import org.bukkit.{Bukkit, ChatColor, Material, Sound}

import scala.collection.mutable
import scala.util.Random

// TODO cleanup
object MebiusListener {

  import scala.jdk.CollectionConverters._

  /**
   * 経験値瓶をボーナスするLv
   * この値未満だとボーナス発生
   */
  private val EXPBONUS = 50
  /** 最大Lv */
  private val LVMAX = 30
  /** 初期の名前 */
  private val DEFNAME = "MEBIUS"

  /** 識別用の先頭Lore */
  private val LOREFIRST = List(
    s"$RESET${ChatColor.GRAY}経験値瓶 効果2倍$RED(整地レベル${EXPBONUS}未満限定)",
    "",
    s"$RESET${ChatColor.AQUA}初心者をサポートする不思議なヘルメット。",
    ""
  )
  private val LOREFIRST2 = List(
    s"$RESET",
    s"$RESET${ChatColor.AQUA}初心者をサポートする不思議なヘルメット。",
    s"$RESET${ChatColor.AQUA}整地により成長する。",
    ""
  )
  private val LV = 4
  private val TALK = 5
  private val DEST = 6
  private val OWNER = 8
  private val NAMEHEAD = s"$RESET${ChatColor.GOLD}${ChatColor.BOLD}"
  private val ILHEAD = s"$RESET$RED${ChatColor.BOLD}アイテムLv. "
  private val TALKHEAD = s"$RESET${ChatColor.GOLD}${ChatColor.ITALIC}"
  private val DESTHEAD = s"$RESET${ChatColor.GRAY}${ChatColor.ITALIC}"
  private val OWNERHEAD = s"$RESET${ChatColor.DARK_GREEN}所有者："

  /** レベルアップ確率テーブル */
  private val lvPer = List(
    500, 500, 500, 500, 800,
    800, 800, 800, 800, 1700,
    1700, 1700, 1700, 1700, 1800,
    1800, 1800, 1800, 1800, 2200,
    2200, 2200, 2200, 2200, 2600,
    2600, 2600, 2600, 3000, 3000
  )

  // Mebiusドロップ率
  private val dropPer = 50000

  /** 見た目テーブル */
  private val APPEARANCE = new mutable.LinkedHashMap[Int, Material]() {}

  /** レベル別Talk */
  private val TALKDEST = List(
    MebiusTalk("こんにちは！これからよろしくねー！", "いつの間にか被っていた。"),
    MebiusTalk("僕のこと外さないでね？", "段々成長していくらしい。"),
    MebiusTalk("モンスターって怖いねえ…", "どこから喋っているのだろう。"),
    MebiusTalk("どこでもルールって大切だね。", "ちゃんと守らなきゃね。"),
    MebiusTalk("整地神様って知ってる？偉いんだよ！", "どうやら神様を知ってるみたい。"),
    MebiusTalk("知らないこと、いっぱい学びたいなぁ。", "どこに記憶しているんだろう。"),
    MebiusTalk("ゾンビっておいしいのかな？", "それだけはやめておけ。"),
    MebiusTalk("どこかに僕の兄弟が埋まってるんだー。", "採掘で手に入るのかな。"),
    MebiusTalk("…はっ！寝てないからね！？", "たまに静かだよね。"),
    MebiusTalk("スキルって気持ち良いよね！", "マナが吸い取られるけどね。"),
    MebiusTalk("メインワールドの探検しようよー！", "息抜きは大切だね。"),
    MebiusTalk("宿題も大切だよ？", "何の話をしてるんだろう…"),
    MebiusTalk("空を自由に飛びたいなー！", "はい、タケコプター！"),
    MebiusTalk("ジュースが飲みたいよー！", "どこから飲むつもりだろう。"),
    MebiusTalk("君の頭って落ち着くねぇ。", "君のお喋りにも慣れたなぁ。"),
    MebiusTalk("APOLLO様みたいになれるかな？", "どんな関係があるんだろう…"),
    MebiusTalk("僕って役に立つでしょー！", "静かならもっといいんだけどね。"),
    MebiusTalk("赤いりんごがあるらしいよ！？", "りんごは普通赤いんだよ。"),
    MebiusTalk("ヘルメット式電動耳掃除…", "何を怖いことを言っている…"),
    MebiusTalk("ここまで育つなんてね！", "立派になったもんだね。"),
    MebiusTalk("動きすぎると酔っちゃうよね。", "三半規管はあるのかな。"),
    MebiusTalk("僕は整地神様に生み出されたんだよ！", "整地神ってお喋りなのかな…"),
    MebiusTalk("君とドラゴンを倒す夢を見たよ…", "エンダードラゴンのことかな。"),
    MebiusTalk("君は僕が育てたと胸を張って言えるね！", "逆でしょう。"),
    MebiusTalk("ああー饅頭が怖いなあ！", "落語でも見た？あげないよ。"),
    MebiusTalk("僕にも手足があったらなー…！", "被れなくなるでしょ。"),
    MebiusTalk("このフィット感…着心地抜群だよね？", "もう少し静かだったらね。"),
    MebiusTalk("餃子っておいしいんだねえ！", "ニンニク臭がこもってるよ…"),
    MebiusTalk("君も立派になったねえ", "同じこと思ってたとこ。"),
    MebiusTalk("育ててくれてありがとう！", "ある意味、最強のヘルメット。")
  )

  /** エンチャント別レベル制限 */
  private val ENCHANT = List(
    MebiusEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2, 10, "ダメージ軽減"),
    MebiusEnchantment(Enchantment.PROTECTION_FIRE, 6, 10, "火炎耐性"),
    MebiusEnchantment(Enchantment.PROTECTION_PROJECTILE, 6, 10, "飛び道具耐性"),
    MebiusEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 6, 10, "爆発耐性"),
    MebiusEnchantment(Enchantment.OXYGEN, 15, 3, "水中呼吸"),
    MebiusEnchantment(Enchantment.WATER_WORKER, 15, 1, "水中採掘"),
    MebiusEnchantment(Enchantment.DURABILITY, 2, 10, "耐久力")
  )

  private val UNBREAK = s"$RESET${ChatColor.AQUA}耐久無限"
  private val ROMAN = List(
    "", "", " II", " III", " IV", " V",
    " VI", " VII", " VIII", " IX", " X",
    " XI", " XII", " XIII", " XIV", " XV",
    " XVI", " XVII", " XVIII", " XIX", " XX"
  )

  private val tips = List(
    "僕の名前は、/mebius naming <名前> コマンドで変更できるよ！<名前>の代わりに新しい名前を入れてね！",
    "僕は整地によって成長するんだー。アイテムレベル30まであるんだよ！",
    "僕たち兄弟のステータスはみんなバラバラなんだよー！",
    "僕たちはこの世界のどこかに埋まってるんだー。整地して僕の兄弟も見つけて欲しいな！",
    "困ったときはwikiを見ようね！",
    "1日1回投票をすると、ガチャ券とピッケルが貰えるよ！",
    "第2整地ワールドは自分で保護を掛けたところしか整地出来ないみたい。誰にも邪魔されずに黙々と掘りたい人に好都合だね。",
    "エリトラ装備中は上を向きながらダッシュすると空を飛べるんだって！",
    "公共施設サーバからデパートに行ってみようよ！修繕の本やダイヤのツールが買えるんだってー！",
    "余った鉱石は公共施設サーバの交換所で交換券に出来るって知ってた？交換券で強いピッケルやスコップが手に入るらしいよ！"
  )

  // デバッグフラグ
  private var DEBUGENABLE = false
  private val debugFlg = false

  // Tipsが呼び出されたとき
  def callTips(player: Player): Unit = if (isEquip(player)) {
    val no = new Random().nextInt(tips.size + 1)
    if (no == tips.size) {
      getPlayerData(player).mebius.speak(getTalk(getMebiusLevel(player.getInventory.getHelmet)))
    } else {
      // tipsの中身を設定
      getPlayerData(player).mebius.speak(tips(no))
    }
  }

  /** ブロックを破壊した時 */
  def onBlockBreak(event: BlockBreakEvent): Unit = { // TODO move to class
    val msgs = Set(
      "ポコポコポコポコ…整地の音って、落ち着くねえ。",
      "頑張れー！頑張れー！そこをまっすぐ！左にも石があるよー！…うるさい？",
      "一生懸命掘ってると、いつの間にか無心になっちゃうよねえ…！",
      "なんだか眠たくなってきちゃったー、[str1]は平気ー？",
      "今日はどこまで掘るのかなー？",
      "[str1]と一緒に整地するの、楽しいねえ！"
    )

    val player = event.getPlayer
    if (isEquip(player)) {
      val pd = getPlayerData(player)
      pd.mebius.speak(getMessage(msgs, Objects.requireNonNull(getNickname(player)), ""))
      // Level UP☆
      if (isLevelUp(player)) levelUp(player)
    }
    // 取得判定
    if (isDrop) discovery(player)
  }

  /** Mebiusを装備しているか */
  def isEquip(player: Player): Boolean = {
    try return isMebius(player.getInventory.getHelmet)
    catch {
      case e: NullPointerException =>
    }
    false
  }

  /** MebiusのDisplayNameを設定 */
  def setName(player: Player, name: String): Boolean = {
    if (isEquip(player)) {
      val mebius = player.getInventory.getHelmet
      val meta = mebius.getItemMeta
      meta.setDisplayName(NAMEHEAD + name)
      player.sendMessage(getName(mebius) + RESET + "に命名しました。")
      mebius.setItemMeta(meta)
      player.getInventory.setHelmet(mebius)
      getPlayerData(player).mebius.speakForce("わーい、ありがとう！今日から僕は" + NAMEHEAD + name + RESET + "だ！")
      return true
    }
    false
  }

  /** MebiusのDisplayNameを取得 */
  def getName(mebius: ItemStack): String = {
    try if (isMebius(mebius)) return mebius.getItemMeta.getDisplayName
    catch {
      case e: NullPointerException =>
    }
    NAMEHEAD + DEFNAME
  }

  def setNickname(player: Player, name: String): Boolean = if (!isEquip(player)) false
  else {
    val mebius = player.getInventory.getHelmet
    val nbtItem = new NBTItem(mebius)
    nbtItem.setString("nickname", name)
    player.getInventory.setHelmet(nbtItem.getItem)
    getPlayerData(player).mebius.speakForce("わーい、ありがとう！今日から君のこと" + ChatColor.GREEN + name + RESET + "って呼ぶね！")
    true
  }

  // FIXME あの！ここはListenerクラスですよ！！
  def getNickname(player: Player): String =
    if (!isEquip(player)) null
    else {
      val mebius = player.getInventory.getHelmet
      val nbtItem = new NBTItem(mebius)
      if (nbtItem.getString("nickname").isEmpty) {
        nbtItem.setString("nickname", player.getName)
        player.getName
      }
      else nbtItem.getString("nickname")
    }

  // PlayerData取得
  private def getPlayerData(player: Player) = SeichiAssist.playermap.apply(player.getUniqueId)

  // ItemStackがMebiusか
  private def isMebius(item: ItemStack): Boolean = {
    val meta = item.getItemMeta

    meta.hasLore && {
      val lore = meta.getLore.asScala
      LOREFIRST2.forall(lore.contains) || LOREFIRST.forall(lore.contains)
    }
  }

  /**
   * 新規Mebius配布処理
   * 新規参加者に配る
   */
  // FIXME This is listener class.
  @deprecated def give(player: Player): Unit = {
    val mebius = create(null, player)
    player.getInventory.setHelmet(mebius)
  }

  // 新規Mebius発見処理(採掘時)
  private def discovery(player: Player): Unit = {
    val mebius = create(null, player)
    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}おめでとうございます。採掘中にMEBIUSを発見しました。")
    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}MEBIUSはプレイヤーと共に成長するヘルメットです。")
    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}あなただけのMEBIUSを育てましょう！")
    Bukkit.getServer.getScheduler.runTaskLater(
      SeichiAssist.instance,
      () => getPlayerData(player).mebius
        .speakForce(s"こんにちは、${player.getName}$RESET。僕は${getName(mebius)}$RESET！これからよろしくね！"),
      10
    )

    player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
    if (!Util.isPlayerInventoryFull(player)) Util.addItem(player, mebius)
    else {
      player.sendMessage(s"$RESET$RED${ChatColor.BOLD}所持しきれないためMEBIUSをドロップしました。")
      Util.dropItem(player, mebius)
    }
  }

  // MebiusのLvを取得
  private def getMebiusLevel(mebius: ItemStack): Int = {
    mebius.getItemMeta.getLore.get(LV).replace(ILHEAD, "").toInt
  }

  // MebiusのOwnerを取得
  private def getOwner(mebius: ItemStack): String = {
    mebius.getItemMeta.getLore.get(OWNER).replaceFirst(OWNERHEAD, "")
  }

  // MebiusLvアップ判定
  private def isLevelUp(player: Player) = {
    val chk = Random.nextInt(lvPer(getMebiusLevel(player.getInventory.getHelmet) - 1))
    chk == 0
  }

  // Mebiusドロップ判定
  private def isDrop = {
    var chk = new Random().nextInt(dropPer)
    if (debugFlg) chk /= 100
    chk == 0
  }

  // Mebius更新処理
  private def levelUp(player: Player): Unit = {
    val mebius = player.getInventory.getHelmet

    // 上限Lvチェック
    var level = getMebiusLevel(mebius)
    if (level == LVMAX) return

    // 所有者が異なる場合…名前変更でもNG
    if (player.getName.toLowerCase != getOwner(mebius)) return

    // Level Up
    level += 1

    // レベルアップ通知
    player.sendMessage(s"${getName(mebius)}${RESET}がレベルアップしました。")

    val newMebius =
      if (APPEARANCE.contains(level)) {
        // ItemStack更新レベルなら新規アイテムに更新
        create(mebius, player)
      } else {
        val cloned = mebius.clone()
        cloned.setItemMeta {
          val meta = cloned.getItemMeta;
          import meta._

          setLore(updateTalkDest(meta.getLore.asScala, level).asJava)
          setEnchant(meta, level, player)

          meta
        }
        cloned
      }

    // 耐久を回復
    newMebius.setDurability(0.toShort)
    player.getInventory.setHelmet(newMebius)
    getPlayerData(player).mebius.speakForce(getTalk(level))
  }

  // 新しいMebiusのひな形を作る
  private def create(mebius: ItemStack, player: Player): ItemStack = {
    val (name, nickname, level, enchantments) =
      if (mebius != null) {
        val level = getMebiusLevel(mebius) + 1
        val name = mebius.getItemMeta.getDisplayName
        val enchantments = mebius.getItemMeta.getEnchants.asScala.view.mapValues(_.toInt).toMap
        val nickname = new NBTItem(mebius).getString("nickname")

        // Mebiusの進化を通知する
        // FIXME createなのに通知ロジックがある
        player.sendMessage(s"$name${RESET}の見た目が進化しました。")

        (name, nickname, level, enchantments)
      } else {
        (NAMEHEAD + DEFNAME, "", 1, Map[Enchantment, Int]())
      }

    val newMebius = new ItemStack(APPEARANCE(level))

    val meta = Bukkit.getItemFactory.getItemMeta(APPEARANCE(level))
    meta.setDisplayName(name)

    // Lore生成
    val lore = LOREFIRST2.concat(List(ILHEAD + level, "", "", "", OWNERHEAD + player.getName.toLowerCase))

    meta.setLore(updateTalkDest(lore, level).asJava)

    // エンチャントを付与する
    enchantments.foreachEntry { case (enchantment, level) =>
      meta.removeEnchant(enchantment)
      meta.addEnchant(enchantment, level, true)
    }

    // フラグ設定
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
    newMebius.setItemMeta(meta)

    val nbtItem = new NBTItem(newMebius)
    nbtItem.setString("nickname", nickname)

    nbtItem.getItem
  }

  // Talk更新
  private def updateTalkDest(currentLore: Iterable[String], level: Int): List[String] = {
    val currentLoreView = mutable.ListBuffer.from(currentLore)

    LOREFIRST2.zipWithIndex.foreach { case (row, index) => currentLoreView(index) = row }
    currentLoreView(LV) = ILHEAD + level
    currentLoreView(TALK) = s"$TALKHEAD「${TALKDEST(level - 1).mebiusMessage}」"
    currentLoreView(DEST) = s"$DESTHEAD${TALKDEST(level - 1).playerMessage}"

    currentLoreView.toList
  }

  // Talkを取得
  private def getTalk(level: Int): String = TALKDEST(level - 1).mebiusMessage

  private def setEnchant(meta: ItemMeta, level: Int, player: Player): Unit = { // LvMAXなら無限とLoreをセット
    if (level == LVMAX) {
      meta.spigot.setUnbreakable(true)
      val lore = meta.getLore
      lore.add(UNBREAK)
      meta.setLore(lore)

      player.sendMessage(s"$RESET${ChatColor.GREEN}おめでとうございます。${meta.getDisplayName}$RESET${ChatColor.GREEN}のレベルが最大になりました。")
      player.sendMessage(s"$UNBREAK${RESET}が付与されました。")
    } else {
      // その他はレベル別Enchantから設定
      val currentEnchantments = Map.from(meta.getEnchants.asScala)

      def getCurrentLevelOf(mebiusEnchantment: MebiusEnchantment): Int = {
        currentEnchantments.get(mebiusEnchantment.enchantment).map(_.toInt).getOrElse(0)
      }

      val enchantmentToGive = {
        val candidateEnchantmentsToGive =
          ENCHANT.filter { candidate =>
            // 解放レベル以上かつ、未取得または上り幅があるエンチャント
            level >= candidate.unlockLevel && getCurrentLevelOf(candidate) < candidate.maxLevel
          }

        candidateEnchantmentsToGive(Random.nextInt(candidateEnchantmentsToGive.size))
      }

      val previousLevel = getCurrentLevelOf(enchantmentToGive)
      val newLevel = previousLevel + 1

      // メッセージを生成
      val message = if (previousLevel == 0) {
        s"${ChatColor.GRAY}${enchantmentToGive.displayName}${RESET}が付与されました。"
      } else {
        s"${ChatColor.GRAY}${enchantmentToGive.displayName}${ROMAN(previousLevel)}${RESET}が" +
          s"${ChatColor.GRAY}${enchantmentToGive.displayName}${ROMAN(newLevel)}${RESET}に強化されました。"
      }

      player.sendMessage(message)

      meta.removeEnchant(enchantmentToGive.enchantment)
      meta.addEnchant(enchantmentToGive.enchantment, newLevel, true)
    }
  }

  // メッセージリストからランダムに取り出し、タグを置換する
  private def getMessage(messages: Set[String], str1: String, str2: String) = {
    var msg = messages.toList(Random.nextInt(messages.size))

    if (!str1.isEmpty) msg = msg.replace("[str1]", s"$str1$RESET")
    if (!str2.isEmpty) msg = msg.replace("[str2]", s"$str2$RESET")

    msg
  }
}

class MebiusListener() // 起動時
  extends Listener {

  if (SeichiAssist.seichiAssistConfig.getMebiusDebug == 1) { // mebiusdebug=1の時はコマンドでトグル可能
    Bukkit.getServer.getConsoleSender.sendMessage(RED + "メビウス帽子のdebugモードトグル機能：有効")
    MebiusListener.DEBUGENABLE = true
  }
  else { // debugmode=0の時はトグル不可能
    Bukkit.getServer.getConsoleSender.sendMessage(ChatColor.GREEN + "メビウス帽子のdebugモードトグル機能：無効")
  }

  // ダメージを受けた時
  @EventHandler def onDamage(event: EntityDamageByEntityEvent): Unit = {
    val breakmsgs = Set(
      "いたた…もうすぐ壊れちゃいそうだ…",
      "もうダメかも…こんなところで、悔しいなぁ",
      "お願い、修繕して欲しいよ…",
      "ごめんね…これ以上は[str1]のこと、守ってあげられそうにないよ…",
      "もっと[str1]と、旅したかったなぁ",
      "まだ平気…壊れるまでは、[str1]のことを守るんだ…")

    val warnmsgs = Set(
      "[str2]からの攻撃だ！気を付けて！",
      "お前なんか余裕なんだからなー！さあ[str1]、やっちゃえ！",
      "びっくりしたなー！人が休んでるときにー！",
      "もーなんで今攻撃してくるのさあああ！",
      "いったーいっ、今僕の小指踏んだなー！？",
      "いてっ！やめろよー！僕を怒らせたら怖いぞー！"
    )

    // プレイヤーがダメージを受けた場合
    event.getEntity match {
      case player: Player =>
        // プレイヤーがMebiusを装備していない場合は除外
        if (!MebiusListener.isEquip(player)) return
        val mebius = player.getInventory.getHelmet
        // 耐久無限じゃない場合
        if (!mebius.getItemMeta.spigot.isUnbreakable) { // 耐久閾値を超えていたら破損警告
          val max = mebius.getType.getMaxDurability
          val dur = mebius.getDurability
          if (dur >= max - 10) {
            MebiusListener.getPlayerData(player).mebius
              .speak(MebiusListener.getMessage(breakmsgs, Objects.requireNonNull(MebiusListener.getNickname(player)), ""))
          }
        }
        // モンスターからダメージを受けた場合
        event.getDamager match {
          case monster: Monster =>
            // 対モンスターメッセージ
            MebiusListener.getPlayerData(player).mebius
              .speak(MebiusListener.getMessage(warnmsgs, Objects.requireNonNull(MebiusListener.getNickname(player)), monster.getName))
          case _ =>
        }
      case _ =>
    }
  }

  // 壊れたとき
  @EventHandler def onBreak(event: PlayerItemBreakEvent): Unit = {
    val msgs = Set(
      "ここまでかぁっ…[str1]と一緒に旅したこと、すごく楽しかったなぁ…",
      "この先[str1]のこと、守ってあげられなくなっちゃった…ごめんね…",
      "僕、少しは[str1]の役に立てたかなぁ…もしそうだったら、嬉しいなぁ",
      "[str1]のおかげで最期まで防具としていられたんだぁ…使ってくれて、ありがとう。",
      "最期まで[str1]の頭にいれたことって、すごく幸せなことだよ",
      "もし生まれ変わっても、また[str1]と…"
    )
    val item = event.getBrokenItem
    // 壊れたアイテムがMEBIUSなら
    if (MebiusListener.isMebius(item)) {
      val player = event.getPlayer
      MebiusListener
        .getPlayerData(event.getPlayer).mebius
        .speak(MebiusListener.getMessage(msgs, Objects.requireNonNull(MebiusListener.getNickname(player)), ""))
      player.sendMessage(s"${MebiusListener.getName(item)}${RESET}が旅立ちました。")
      // エンドラが叫ぶ
      player.playSound(player.getLocation, Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 0.1f)
    }
  }

  // モンスターを倒した時
  @EventHandler def onKill(event: EntityDeathEvent): Unit = {
    val msgs = Set(
      "さすが[str1]！[str2]なんて敵じゃないね！",
      "僕にかかれば[str2]なんてこんなもんだよー！",
      "モンスターってなんで人間を襲うんだろう…？",
      "ねえ[str1]、今の僕のおかげだよね！ね？",
      "たまにはやられてみたいもんだねー、ふふん！",
      "[str2]なんて僕の力を出すまでもなかったね！"
    )
    val lived = event.getEntity
    // プレイヤーがモンスターを倒した場合以外は除外
    if (lived == null) return
    val player = lived.getKiller
    if (player == null) return
    val monsterName = lived.getName
    if (!MebiusListener.isEquip(player)) return
    //もしモンスター名が取れなければ除外
    if (monsterName == "") return
    val playerNick = MebiusListener.getNickname(player)
    Objects.requireNonNull(playerNick)
    MebiusListener.getPlayerData(player).mebius.speak(MebiusListener.getMessage(msgs, playerNick, monsterName))
  }

  // 金床配置時（クリック）
  @EventHandler def onRename(event: InventoryClickEvent): Unit = { // 金床を開いていない場合return
    if (!event.getView.getTopInventory.isInstanceOf[AnvilInventory]) return
    val inv = event.getClickedInventory
    if (inv.isInstanceOf[AnvilInventory]) { // mebiusを選択中
      val item = event.getCursor
      if (MebiusListener.isMebius(item)) { // mebiusを左枠に置いた場合はcancel
        val rawSlot = event.getRawSlot
        if (rawSlot == event.getView.convertSlot(rawSlot) && rawSlot == 0) {
          event.setCancelled(true)
          event.getWhoClicked.sendMessage(s"${RED}MEBIUSへの命名は$RESET/mebius naming <name>${RED}で行ってください。")
        }
      }
    }
    else { // mebiusをShiftクリックした場合
      if (event.getClick.isShiftClick && MebiusListener.isMebius(event.getCurrentItem)) { // 左枠が空いている場合はcancel
        if (event.getView.getTopInventory.getItem(0) == null) {
          event.setCancelled(true)
          event.getWhoClicked.sendMessage(s"${RED}MEBIUSへの命名は$RESET/mebius naming <name>${RED}で行ってください。")
        }
      }
    }
  }

  // 金床配置時（ドラッグ）
  @EventHandler def onDrag(event: InventoryDragEvent): Unit = { // 金床じゃなければreturn
    val inv = event.getInventory
    if (!inv.isInstanceOf[AnvilInventory]) return
    // mebiusを選択中じゃなければreturn
    val item = event.getOldCursor
    if (!MebiusListener.isMebius(item)) return

    import scala.jdk.CollectionConverters._
    for (rawSlot <- event.getRawSlots.asScala) {
      if ((rawSlot.toInt == event.getView.convertSlot(rawSlot)) && (rawSlot == 0)) {
        event.setCancelled(true)
        event.getWhoClicked.sendMessage(s"${RED}MEBIUSへの命名は$RESET/mebius naming <name>${RED}で行ってください。")
      }
    }
  }
}
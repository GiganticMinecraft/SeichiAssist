package com.github.unchama.seichiassist.data.player

import java.text.SimpleDateFormat
import java.util.{GregorianCalendar, UUID}

import cats.effect.IO
import com.github.unchama.menuinventory.syntax._
import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.seichiassist.data.player.settings.PlayerSettings
import com.github.unchama.seichiassist.data.potioneffect.FastDiggingEffect
import com.github.unchama.seichiassist.data.subhome.SubHome
import com.github.unchama.seichiassist.data.{ActiveSkillData, GridTemplate, Mana}
import com.github.unchama.seichiassist.event.SeichiLevelUpEvent
import com.github.unchama.seichiassist.minestack.MineStackUsageHistory
import com.github.unchama.seichiassist.task.{MebiusTask, VotingFairyTask}
import com.github.unchama.seichiassist.util.Util.DirectionType
import com.github.unchama.seichiassist.util.exp.{ExperienceManager, IExperienceManager}
import com.github.unchama.seichiassist.util.{ClosedRange, Util}
import org.bukkit.ChatColor._
import org.bukkit._
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.potion.{PotionEffect, PotionEffectType}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._
import scala.util.control.Breaks

/**
 * @deprecated PlayerDataはuuidに依存するべきではない
 */
class PlayerData(
                  @Deprecated() val uuid: UUID,
                  val name: String
                ) {

  import com.github.unchama.targetedeffect._
  import com.github.unchama.targetedeffect.player.ForcedPotionEffect._
  import com.github.unchama.targetedeffect.syntax._
  import com.github.unchama.util.InventoryUtil._

  lazy val mebius: MebiusTask = new MebiusTask(uuid)

  //region session-specific data
  // TODO many properties here might not be right to belong here
  //各統計値差分計算用配列
  lazy private val statisticsData: mutable.ArrayBuffer[Int] = {
    val buffer: mutable.ArrayBuffer[Int] = ArrayBuffer()

    buffer ++= (MaterialSets.materials -- PlayerData.exclude).toBuffer[Material]
      .map(material => player.getStatistic(Statistic.MINE_BLOCK, material))

    buffer
  }
  //経験値マネージャ
  lazy private val expmanager: IExperienceManager = new ExperienceManager(player)
  val settings = new PlayerSettings()
  //３０分間のデータを保存する．
  val halfhourblock: MineBlock = new MineBlock()
  //持ってるポーションエフェクト全てを格納する．
  val effectdatalist: mutable.ListBuffer[FastDiggingEffect] = mutable.ListBuffer.empty
  //プレイヤー名
  val lowercaseName: String = name.toLowerCase()

  /**
   * プレーヤーに付与されるべき採掘速度上昇効果を計算する.
   */
  val computeFastDiggingEffect: IO[ForcedPotionEffect] = for {
    activeEffects <- IO {
      effectdatalist.toList
    }
    computedAmplifier <- IO {
      val amplifierSum = activeEffects.map(_.amplifier).sum
      Math.floor(amplifierSum - 1).toInt
    }
    maxSpeed <- settings.fastDiggingEffectSuppression.maximumAllowedEffectAmplifier()
    maxDuration <- IO {
      activeEffects.map(_.duration).maxOption.getOrElse(0)
    }
  } yield {
    // 実際に適用されるeffect量
    val amplifier = Math.min(computedAmplifier, maxSpeed)

    val effect =
      if (amplifier >= 0)
        new PotionEffect(PotionEffectType.FAST_DIGGING, maxDuration, amplifier, false, false)
      else
        new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false)

    effect.asTargetedEffect()
  }

  /**
   * @deprecated Should be moved to external scope
   */
  @Deprecated()
  val toggleExpBarVisibility: TargetedEffect[Player] = {
    import com.github.unchama.generic.syntax._

    UnfocusedEffect {
      this.settings.isExpBarVisible = !this.settings.isExpBarVisible
    }.followedBy {
      deferredEffect {
        IO({
          if (this.settings.isExpBarVisible)
            s"${GREEN}整地量バー表示"
          else
            s"${RED}整地量バー非表示"
          }.asMessageEffect())
      }
    }.followedBy {
      UnfocusedEffect {
        SeichiAssist.instance.expBarSynchronization.synchronizeFor(player)
      }
    }
  }

  private val subHomeMap: mutable.Map[Int, SubHome] = mutable.HashMap[Int, SubHome]()
  private val dummyDate = new GregorianCalendar(2100, 1, 1, 0, 0, 0)
  //チェスト破壊トグル
  var chestflag = true

  /**
   * チェスト破壊のON/OFFを切り替える[UnfocusedEffect]
   */
  val toggleChestBreakFlag: TargetedEffect[Player] = UnfocusedEffect {
    chestflag = !chestflag
  }

  var canCreateRegion = true
  var unitPerClick = 1
  //今回の採掘速度上昇レベルを格納
  var minespeedlv = 0
  //前回の採掘速度上昇レベルを格納
  var lastminespeedlv = 0
  //投票受け取りボタン連打防止用
  var votecooldownflag = true
  //ガチャボタン連打防止用
  var gachacooldownflag = true
  //インベントリ共有ボタン連打防止用
  var shareinvcooldownflag = true
  var selectHomeNum = 0
  var samepageflag = false //実績ショップ用

  //endregion
  //MineStackの履歴
  val hisotryData: MineStackUsageHistory = new MineStackUsageHistory()
  var titlepage = 1 //実績メニュー用汎用ページ指定
  //現在座標
  var loc: Option[Location] = None
  //放置時間
  var idleMinute = 0
  //共有インベントリにアイテムが入っているかどうか
  var contentsPresentInSharedInventory = false
  //ガチャの基準となるポイント
  var gachapoint = 0
  //現在のプレイヤーレベル
  var level = 1
  //詫び券をあげる数
  var unclaimedApologyItems = 0
  //ワールドガード保護自動設定用
  var regionCount = 0
  /**
   * 保護申請の番号を更新させる[UnfocusedEffect]
   */
  val incrementRegionNumber: TargetedEffect[Any] = UnfocusedEffect {
    this.regionCount += 1
  }

  var starLevels = StarLevel(0, 0, 0)
  var minestack = new MineStack()
  //プレイ時間
  var playTick = 0
  //トータル破壊ブロック
  var totalbreaknum: Long = 0.toLong
  //合計経験値
  var totalexp = 0
  //合計経験値統合済みフラグ
  var expmarge: Byte = 0
  //特典受け取り済み投票数
  var p_givenvote = 0
  //連続・通算ログイン用
  // ロード時に初期化される
  var lastcheckdate: String = _
  var loginStatus = LoginStatus(null, 0, 0)
  //期間限定ログイン用
  var LimitedLoginCount = 0
  var ChainVote = 0
  //アクティブスキル関連データ
  val activeskilldata: ActiveSkillData = new ActiveSkillData()
  //二つ名解禁フラグ保存用
  var TitleFlags: mutable.BitSet = new mutable.BitSet(10001)

  //二つ名関連用にp_vote(投票数)を引っ張る。(予期せぬエラー回避のため名前を複雑化)
  var p_vote_forT = 0
  //二つ名配布予約NOの保存
  var giveachvNo = 0
  //実績ポイント用
  var achievePoint = AchievementPoint()

  var buildCount = BuildCount(1, java.math.BigDecimal.ZERO, 0)
  // 1周年記念
  var anniversary = false
  var templateMap: mutable.Map[Int, GridTemplate] = mutable.HashMap()
  //投票妖精関連
  var usingVotingFairy = false
  var voteFairyPeriod = new ClosedRange(dummyDate, dummyDate)
  var hasVotingFairyMana = 0
  var VotingFairyRecoveryValue = 0
  var toggleGiveApple = 1
  var toggleVotingFairy = 1
  var p_apple: Long = 0
  var toggleVFSound = true
  //貢献度pt
  var added_mana = 0
  var contribute_point = 0
  //正月イベント用
  var hasNewYearSobaGive = false
  var newYearBagAmount = 0
  //バレンタインイベント用
  var hasChocoGave = false
  var giganticBerserk = GiganticBerserk(0, 0, 0, canEvolve = false, 0)
  //ハーフブロック破壊抑制用
  private val allowBreakingHalfBlocks = false
  //プレイ時間差分計算用int
  private var totalPlayTick: Option[Int] = None

  //region calculated
  // TODO many properties here may be inlined and deleted
  //拡張インベントリ
  private var _pocketInventory: Inventory = createInventory(None, 1.chestRows, Some(s"$DARK_PURPLE${BOLD}4次元ポケット"))
  //グリッド式保護関連
  private var claimUnit = ClaimUnit(0, 0, 0, 0)

  def pocketInventory: Inventory = {
    // 許容サイズが大きくなっていたら新規インベントリにアイテムをコピーしてそのインベントリを持ち回す
    if (_pocketInventory.getSize < pocketSize) {
      val newInventory =
        Bukkit.getServer
          .createInventory(null, pocketSize, s"$DARK_PURPLE${BOLD}4次元ポケット")
      _pocketInventory.asScala.zipWithIndex.map(_.swap).foreach { case (i, is) => newInventory.setItem(i, is) }
      _pocketInventory = newInventory
    }

    _pocketInventory
  }

  def pocketInventory_=(inventory: Inventory): Unit = {
    _pocketInventory = inventory
  }

  //四次元ポケットのサイズを取得
  private def pocketSize: Int = level match {
    case _ if level < 46 => 9 * 3
    case _ if level < 56 => 9 * 4
    case _ if level < 66 => 9 * 5
    case _ => 9 * 6
  }

  def subHomeEntries: Set[(Int, SubHome)] = subHomeMap.toSet

  def gridChunkAmount: Int = (claimUnit.ahead + claimUnit.behind + 1) * (claimUnit.right + claimUnit.left + 1)

  //オフラインかどうか
  def isOffline: Boolean = SeichiAssist.instance.getServer.getPlayer(uuid) == null

  def GBexp: Int = giganticBerserk.exp

  def GBexp_=(value: Int): Unit = {
    giganticBerserk = giganticBerserk.copy(exp = value)
  }

  def isGBStageUp: Boolean = giganticBerserk.canEvolve

  def isGBStageUp_=(value: Boolean): Unit = {
    giganticBerserk = giganticBerserk.copy(canEvolve = value)
  }

  // FIXME: BAD NAME; not clear meaning
  def GBcd: Int = giganticBerserk.cd

  def GBcd_=(value: Int): Unit = {
    giganticBerserk = giganticBerserk.copy(cd = value)
  }

  //join時とonenable時、プレイヤーデータを最新の状態に更新
  def updateOnJoin(): Unit = {
    //破壊量データ(before)を設定
    halfhourblock.before = totalbreaknum
    updateLevel()

    // TODO statisticsDataは別の箇所に持たれるべき
    // statisticsDataを初期化する
    {
      statisticsData
    }

    if (unclaimedApologyItems > 0) {
      player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
      player.sendMessage(s"${GREEN}運営チームから${unclaimedApologyItems}枚の${GOLD}ガチャ券${WHITE}が届いています！\n木の棒メニューから受け取ってください")
    }

    activeskilldata.updateOnJoin(player, level)
    //サーバー保管経験値をクライアントに読み込み
    loadTotalExp()
    isVotingFairy()
  }

  //レベルを更新
  def updateLevel(): Unit = {
    updatePlayerLevel()
    updateStarLevel()
    setDisplayName()
    SeichiAssist.instance.expBarSynchronization.synchronizeFor(player)
    activeskilldata.mana.display(player, level)
  }

  //表示される名前に整地レベルor二つ名を追加
  def setDisplayName(): Unit = {
    val playerName = player.getName

    //放置時に色を変える
    val idleColor: String =
      if (idleMinute >= 10) s"$DARK_GRAY"
      else if (idleMinute >= 3) s"$GRAY"
      else ""

    val newDisplayName = idleColor + {
      val nicknameSettings = settings.nickname
      val currentNickname =
        Option.unless(nicknameSettings.style == NicknameStyle.Level)(
          Nicknames.getCombinedNicknameFor(nicknameSettings.id1, nicknameSettings.id2, nicknameSettings.id3)
        ).flatten

      currentNickname.fold {
        val levelPart =
          if (totalStarLevel <= 0)
            s"[ Lv$level ]"
          else
            s"[Lv$level☆$totalStarLevel]"

        s"$levelPart$playerName$WHITE"
      } { nickname =>
        s"[$nickname]$playerName$WHITE"
      }
    }

    player.setDisplayName(newDisplayName)
    player.setPlayerListName(newDisplayName)
  }

  /**
   * スターレベルの合計を返すショートカットフィールド。
   */
  def totalStarLevel: Int = starLevels.total()

  //プレイヤーレベルを計算し、更新する。
  private def updatePlayerLevel(): Unit = {
    //現在のランクを取得
    var i: Int = level

    //既にレベル上限に達していたら終了
    if (i >= LevelThresholds.levelExpThresholds.size) return

    // TODO: 三枚におろして処す
    val increasingRank = new Breaks
    increasingRank.breakable {

      //ランクが上がらなくなるまで処理
      while (LevelThresholds.levelExpThresholds(i) <= totalbreaknum && i + 1 <= LevelThresholds.levelExpThresholds.size) {
        //レベルアップ時のメッセージ
        player.sendMessage(s"${GOLD}ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww【Lv($i)→Lv(${i + 1})】")

        //レベルアップイベント着火
        Bukkit.getPluginManager.callEvent(new SeichiLevelUpEvent(player, this, i + 1))

        //レベルアップ時の花火の打ち上げ
        val loc = player.getLocation
        Util.launchFireWorks(loc) // TODO: fix Util
        val lvmessage = SeichiAssist.seichiAssistConfig.getLvMessage(i + 1)
        if (!lvmessage.isEmpty) {
          player.sendMessage(AQUA + lvmessage)
        }

        i += 1

        if (activeskilldata.mana.isLoaded) {
          //マナ最大値の更新
          activeskilldata.mana.onLevelUp(player, i)
        }

        //レベル上限に達したら終了
        if (i >= LevelThresholds.levelExpThresholds.size) {
          increasingRank.break()
        }
      }

    }

    level = i
  }

  /**
   * @deprecated PlayerDataはPlayerに依存するべきではない。
   */
  @Deprecated()
  def player: Player = Bukkit.getPlayer(uuid)

  /**
   * スターレベルの計算、更新を行う。
   * このメソッドはスター数が増えたときにメッセージを送信する副作用を持つ。
   */
  def updateStarLevel(): Unit = {
    //処理前の各レベルを取得
    val oldStars = starLevels.total()
    val oldBreakStars = starLevels.fromBreakAmount
    val oldTimeStars = starLevels.fromConnectionTime
    //処理後のレベルを保存する入れ物
    val newBreakStars = totalbreaknum / 87115000

    //整地量の確認
    if (oldBreakStars < newBreakStars) {
      player.sendMessage(s"${GOLD}ｽﾀｰﾚﾍﾞﾙ(整地量)がﾚﾍﾞﾙｱｯﾌﾟ!!【☆($oldBreakStars)→☆($newBreakStars)】")
      starLevels = starLevels.copy(fromBreakAmount = newBreakStars.toInt)
    }

    //参加時間の確認(19/4/3撤廃)
    if (oldTimeStars > 0) {
      starLevels = starLevels.copy(fromConnectionTime = 0)
    }

    //TODO: イベント入手分スターの確認

    //TODO: 今後実装予定。

    val newStars: Int = starLevels.total()
    //合計値の確認
    if (oldStars < newStars) {
      player.sendMessage(s"$GOLD★☆★ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww★☆★【Lv200(☆($oldStars))→Lv200(☆($newStars))】")
    }
  }

  private def loadTotalExp(): Unit = {
    val internalServerId = SeichiAssist.seichiAssistConfig.getServerNum
    //経験値が統合されてない場合は統合する
    if (expmarge.toInt != 0x07 && (1 to 3).contains(internalServerId)) {
      if (expmarge.&(0x01 << internalServerId - 1).toByte == 0.toByte) {
        if (expmarge.toInt == 0) {
          // 初回は加算じゃなくベースとして代入にする
          totalexp = expmanager.getCurrentExp
        } else {
          totalexp += expmanager.getCurrentExp
        }
        expmarge = (expmarge | (0x01 << internalServerId - 1).toByte).toByte
      }
    }
    expmanager.setExp(totalexp)
  }

  private def isVotingFairy(): Unit = {
    //効果は継続しているか
    if (this.usingVotingFairy && !Util.isVotingFairyPeriod(this.votingFairyStartTime, this.votingFairyEndTime)) {
      this.usingVotingFairy = false
      player.sendMessage(s"$LIGHT_PURPLE${BOLD}妖精は何処かへ行ってしまったようだ...")
    } else if (this.usingVotingFairy) {
      VotingFairyTask.speak(player, "おかえり！" + player.getName, true)
    }
  }

  def votingFairyEndTime: GregorianCalendar = voteFairyPeriod.endInclusive

  def votingFairyEndTime_=(value: GregorianCalendar): Unit = {
    voteFairyPeriod = new ClosedRange(voteFairyPeriod.start, value)
  }

  def updateNickname(id1: Int = settings.nickname.id1,
                     id2: Int = settings.nickname.id2,
                     id3: Int = settings.nickname.id3,
                     style: NicknameStyle = NicknameStyle.TitleCombination): Unit = {
    settings.nickname = settings.nickname.copy(id1 = id1, id2 = id2, id3 = id3, style = style)
  }

  //quit時とondisable時、プレイヤーデータを最新の状態に更新
  def updateOnQuit(): Unit = {
    //総整地量を更新
    updateAndCalcMinedBlockAmount()
    //総プレイ時間更新
    updatePlayTick()

    activeskilldata.updateOnQuit()

    mebius.cancel()

    //クライアント経験値をサーバー保管
    saveTotalExp()

    activeskilldata.RemoveAllTask()
  }

  //総プレイ時間を更新する
  def updatePlayTick(): Unit = {
    // WARN: 1分毎にupdatePlayTickが呼び出されるというコンテクストに依存している.
    val nowTotalPlayTick = player.getStatistic(Statistic.PLAY_ONE_TICK)
    val diff = nowTotalPlayTick - totalPlayTick.getOrElse(nowTotalPlayTick)

    totalPlayTick = Some(nowTotalPlayTick)
    playTick += diff
  }

  //総破壊ブロック数を更新する
  def updateAndCalcMinedBlockAmount(): Int = {
    val blockIncreases = for {
      (m, i) <- (MaterialSets.materials -- PlayerData.exclude).zipWithIndex
    } yield {
      val materialStatistics = player.getStatistic(Statistic.MINE_BLOCK, m)
      val increase = materialStatistics - statisticsData(i)
      val amount = calcBlockExp(m, increase)
      if (SeichiAssist.DEBUG) {
        if (amount > 0.0) {
          player.sendMessage(s"calcの値:$amount($m)")
        }
      }
      statisticsData(i) = materialStatistics

      amount
    }

    val sum = blockIncreases.sum.round.toInt

    totalbreaknum += sum
    gachapoint += sum

    sum
  }

  //スターレベルの計算、更新

  //ブロック別整地数反映量の調節
  private def calcBlockExp(m: Material, i: Int): Double = {
    val amount = i.toDouble

    //ブロック別重み分け
    val materialFactor = m match {
      //DIRTとGRASSは二重カウントされているので半分に
      case Material.DIRT => 0.5
      case Material.GRASS => 0.5

      //氷塊とマグマブロックの整地量を2倍
      case Material.PACKED_ICE | Material.MAGMA => 2.0

      case _ => 1.0
    }

    val managedWorld = ManagedWorld.fromBukkitWorld(player.getWorld)
    val swMult = if (managedWorld.exists(_.isSeichi)) 1.0 else 0.0
    val sw01PenaltyMult = if (managedWorld.contains(ManagedWorld.WORLD_SW)) 0.8 else 1.0

    amount * materialFactor * swMult * sw01PenaltyMult
  }

  private def saveTotalExp(): Unit = {
    totalexp = expmanager.getCurrentExp
  }

  def giganticBerserkLevelUp(): Unit = {
    val currentLevel = giganticBerserk.level
    giganticBerserk = if (currentLevel >= 10) giganticBerserk else giganticBerserk.copy(level = currentLevel + 1, exp = 0)
  }

  def recalculateAchievePoint(): Unit = {
    val max = TitleFlags.toList
      .filter(index => (1000 to 9799) contains index)
      .count(_ => true) * 10 /* Safe Conversation: BitSet indexes => Int */

    achievePoint = achievePoint.copy(fromUnlockedAchievements = max)
  }

  def consumeAchievePoint(amount: Int): Unit = {
    achievePoint = achievePoint.copy(used = achievePoint.used + amount)
  }

  def convertEffectPointToAchievePoint(): Unit = {
    achievePoint = achievePoint.copy(conversionCount = achievePoint.conversionCount + 1)
    activeskilldata.effectpoint -= 10
  }

  //エフェクトデータのdurationを60秒引く
  def calcEffectData(): Unit = {
    val tmplist = mutable.Buffer[FastDiggingEffect]()

    //effectdatalistのdurationをすべて60秒（1200tick）引いてtmplistに格納
    effectdatalist.foreach { effectData =>
      effectData.duration -= 1200
      tmplist += effectData
    }

    //tmplistのdurationが3秒以下（60tick）のものはeffectdatalistから削除
    effectdatalist.foreach { effectData =>
      if (effectData.duration <= 60) {
        effectdatalist -= effectData
      }
    }
  }

  //現在の採掘量順位
  def calcPlayerRank(): Int = {
    //ランク用関数
    var i = 0

    if (SeichiAssist.ranklist.isEmpty) return 1

    var rankdata = SeichiAssist.ranklist(i)

    //ランクが上がらなくなるまで処理
    while (rankdata.totalbreaknum > totalbreaknum) {
      i += 1
      rankdata = SeichiAssist.ranklist(i)
    }

    i + 1
  }

  def calcPlayerApple(): Int = {
    //ランク用関数
    var i = 0
    val t = p_apple

    if (SeichiAssist.ranklist_p_apple.isEmpty) return 1

    var rankdata = SeichiAssist.ranklist_p_apple(i)

    //ランクが上がらなくなるまで処理
    while (rankdata.p_apple > t) {
      i += 1
      rankdata = SeichiAssist.ranklist_p_apple(i)
    }

    i + 1
  }

  //パッシブスキルの獲得量表示
  def getPassiveExp: Double = {
    if (level < 8) 0.0
    else if (level < 18) SeichiAssist.seichiAssistConfig.getDropExplevel(1)
    else if (level < 28) SeichiAssist.seichiAssistConfig.getDropExplevel(2)
    else if (level < 38) SeichiAssist.seichiAssistConfig.getDropExplevel(3)
    else if (level < 48) SeichiAssist.seichiAssistConfig.getDropExplevel(4)
    else if (level < 58) SeichiAssist.seichiAssistConfig.getDropExplevel(5)
    else if (level < 68) SeichiAssist.seichiAssistConfig.getDropExplevel(6)
    else if (level < 78) SeichiAssist.seichiAssistConfig.getDropExplevel(7)
    else if (level < 88) SeichiAssist.seichiAssistConfig.getDropExplevel(8)
    else if (level < 98) SeichiAssist.seichiAssistConfig.getDropExplevel(9)
    else SeichiAssist.seichiAssistConfig.getDropExplevel(10)
  }

  //サブホームの位置をセットする
  def setSubHomeLocation(location: Location, subHomeIndex: Int): Unit = {
    if (subHomeIndex >= 0 && subHomeIndex < SeichiAssist.seichiAssistConfig.getSubHomeMax) {
      val currentSubHome = this.subHomeMap.get(subHomeIndex)
      val currentSubHomeName = currentSubHome.map(_.name).orNull

      this.subHomeMap(subHomeIndex) = new SubHome(location, currentSubHomeName)
    }
  }

  def setSubHomeName(name: String, subHomeIndex: Int): Unit = {
    if (subHomeIndex >= 0 && subHomeIndex < SeichiAssist.seichiAssistConfig.getSubHomeMax) {
      val currentSubHome = this.subHomeMap.getOrElse(subHomeIndex, return)

      this.subHomeMap(subHomeIndex) = new SubHome(currentSubHome.getLocation, name)
    }
  }

  // サブホームの位置を読み込む
  def getSubHomeLocation(subHomeIndex: Int): Option[Location] = {
    val subHome = this.subHomeMap.get(subHomeIndex)
    subHome.map(_.getLocation)
  }

  def getSubHomeName(subHomeIndex: Int): String = {
    val subHome = this.subHomeMap.get(subHomeIndex)
    val subHomeName = subHome.map(_.name)
    subHomeName.getOrElse(s"サブホームポイント${subHomeIndex + 1}")
  }

  def canBreakHalfBlock: Boolean = this.allowBreakingHalfBlocks

  def canGridExtend(directionType: DirectionType, world: String): Boolean = {
    val limit = SeichiAssist.seichiAssistConfig.getGridLimitPerWorld(world)
    val chunkMap = unitMap

    //チャンクを拡大すると仮定する
    val assumedAmoont = chunkMap(directionType) + this.unitPerClick

    //一応すべての拡張値を出しておく
    val ahead = chunkMap(DirectionType.AHEAD)
    val behind = chunkMap(DirectionType.BEHIND)
    val right = chunkMap(DirectionType.RIGHT)
    val left = chunkMap(DirectionType.LEFT)

    //合計チャンク再計算値
    val assumedUnitAmount = directionType match {
      case DirectionType.AHEAD => (assumedAmoont + 1 + behind) * (right + 1 + left)
      case DirectionType.BEHIND => (ahead + 1 + assumedAmoont) * (right + 1 + left)
      case DirectionType.RIGHT => (ahead + 1 + behind) * (assumedAmoont + 1 + left)
      case DirectionType.LEFT => (ahead + 1 + behind) * (right + 1 + assumedAmoont)
    }

    assumedUnitAmount <= limit
  }

  def unitMap: Map[DirectionType, Int] = {
    val unitMap = mutable.Map[DirectionType, Int]().empty

    unitMap.put(DirectionType.AHEAD, claimUnit.ahead)
    unitMap.put(DirectionType.BEHIND, claimUnit.behind)
    unitMap.put(DirectionType.RIGHT, claimUnit.right)
    unitMap.put(DirectionType.LEFT, claimUnit.left)

    unitMap.toMap
  }

  def canGridReduce(directionType: DirectionType): Boolean = {
    val chunkMap = unitMap

    //減らしたと仮定する
    val sizeAfterShrink = chunkMap(directionType) - unitPerClick

    sizeAfterShrink >= 0
  }

  def setUnitAmount(directionType: DirectionType, amount: Int): Unit = {
    this.claimUnit = directionType match {
      case DirectionType.AHEAD => this.claimUnit.copy(ahead = amount)
      case DirectionType.BEHIND => this.claimUnit.copy(behind = amount)
      case DirectionType.RIGHT => this.claimUnit.copy(right = amount)
      case DirectionType.LEFT => this.claimUnit.copy(left = amount)
    }
  }

  import com.github.unchama.seichiassist.AntiTypesafe

  def addUnitAmount(directionType: DirectionType, amount: Int): Unit = {
    directionType match {
      case DirectionType.AHEAD => this.claimUnit = this.claimUnit.copy(ahead = this.claimUnit.ahead + amount)
      case DirectionType.BEHIND => this.claimUnit = this.claimUnit.copy(behind = this.claimUnit.behind + amount)
      case DirectionType.RIGHT => this.claimUnit = this.claimUnit.copy(right = this.claimUnit.right + amount)
      case DirectionType.LEFT => this.claimUnit = this.claimUnit.copy(left = this.claimUnit.left + amount)
    }
  }

  def toggleUnitPerGrid(): Unit = {
    this.unitPerClick = this.unitPerClick match {
      case 1 => 10
      case 10 => 100
      case 100 => 1
    }
  }

  @AntiTypesafe
  def getVotingFairyStartTimeAsString: String = {
    val cal = this.votingFairyStartTime

    if (votingFairyStartTime == dummyDate) {
      //設定されてない場合
      ",,,,,"
    } else {
      //設定されてる場合
      val date = cal.getTime
      val format = new SimpleDateFormat("yyyy,MM,dd,HH,mm,")
      format.format(date)
    }
  }

  def votingFairyStartTime: GregorianCalendar = voteFairyPeriod.start

  def votingFairyStartTime_=(value: GregorianCalendar): Unit = {
    voteFairyPeriod = new ClosedRange(value, voteFairyPeriod.endInclusive)
  }

  def setVotingFairyTime(@AntiTypesafe str: String): Unit = {
    val s = str.split(",")
    if (s.size < 5) return
    if (!s.slice(0, 5).contains("")) {
      val year = s(0).toInt
      val month = s(1).toInt - 1
      val dayOfMonth = s(2).toInt
      val starts = new GregorianCalendar(year, month, dayOfMonth, Integer.parseInt(s(3)), Integer.parseInt(s(4)))

      var min = Integer.parseInt(s(4)) + 1
      var hour = Integer.parseInt(s(3))

      min = if (this.toggleVotingFairy % 2 != 0) min + 30 else min
      hour = this.toggleVotingFairy match {
        case 2 | 3 => hour + 1
        case 4 => hour + 2
        case _ => hour
      }

      val ends = new GregorianCalendar(year, month, dayOfMonth, hour, min)

      this.votingFairyStartTime = starts
      this.votingFairyEndTime = ends
    }
  }

  def setContributionPoint(addAmount: Int): Unit = {
    val mana = new Mana()

    //負数(入力ミスによるやり直し中プレイヤーがオンラインだった場合)の時
    if (addAmount < 0) {
      player.sendMessage(s"$GREEN${BOLD}入力者のミスによって得た不正なマナを${-10 * addAmount}分減少させました.")
      player.sendMessage(s"$GREEN${BOLD}申し訳ございません.")
    } else {
      player.sendMessage(s"$GREEN${BOLD}運営からあなたの整地鯖への貢献報酬として")
      player.sendMessage(s"$GREEN${BOLD}マナの上限値が${10 * addAmount}上昇しました．(永久)")
    }
    this.added_mana += addAmount

    mana.calcAndSetMax(player, this.level)
  }

  def toggleMessageFlag(): TargetedEffect[Player] = deferredEffect(IO {
    settings.receiveFastDiggingEffectStats = !settings.receiveFastDiggingEffectStats

    val responseMessage = if (settings.receiveFastDiggingEffectStats) {
      s"${GREEN}内訳表示:ON(OFFに戻したい時は再度コマンドを実行します。)"
    } else {
      s"${GREEN}内訳表示:OFF"
    }

    responseMessage.asMessageEffect()
  })

  /**
   * 運営権限により強制的に実績を解除することを試みる。
   * 解除に成功し、このインスタンスが指す[Player]がオンラインであるならばその[Player]に解除の旨がチャットにて通知される。
   *
   * @param number 解除対象の実績番号
   * @return この作用の実行者に向け操作の結果を記述する[MessageToSender]
   */
  def tryForcefullyUnlockAchievement(number: Int): TargetedEffect[CommandSender] = deferredEffect(IO {
    if (!TitleFlags(number)) {
      TitleFlags.addOne(number)
      player.sendMessage(s"運営チームよりNo${number}の実績が配布されました。")

      s"$lowercaseName に実績No. $number を${GREEN}付与${RESET}しました。".asMessageEffect()
    } else {
      s"$GRAY$lowercaseName は既に実績No. $number を獲得しています。".asMessageEffect()
    }
  })

  /**
   * 運営権限により強制的に実績を剥奪することを試みる。
   * 実績剥奪の通知はプレーヤーには行われない。
   *
   * @param number 解除対象の実績番号
   * @return この作用の実行者に向け操作の結果を記述する[TargetedEffect]
   */
  def forcefullyDepriveAchievement(number: Int): TargetedEffect[CommandSender] = deferredEffect(IO {
    if (!TitleFlags(number)) {
      TitleFlags(number) = false

      s"$lowercaseName から実績No. $number を${RED}剥奪${GREEN}しました。".asMessageEffect()
    } else {
      s"$GRAY$lowercaseName は実績No. $number を獲得していません。".asMessageEffect()
    }
  })
}

object PlayerData {
  //TODO:もちろんここにあるべきではない
  val passiveSkillProbability = 10

  val exclude: Set[Material] = Set(
    Material.GRASS_PATH,
    Material.SOIL, Material.MOB_SPAWNER,
    Material.CAULDRON, Material.ENDER_CHEST,
    Material.ENDER_PORTAL_FRAME,Material.ENDER_PORTAL
  )
}

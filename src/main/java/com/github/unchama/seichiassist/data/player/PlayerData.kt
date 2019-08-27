package com.github.unchama.seichiassist.data.player

import com.github.unchama.menuinventory.rows
import com.github.unchama.seichiassist.*
import com.github.unchama.seichiassist.data.ActiveSkillData
import com.github.unchama.seichiassist.data.ExpBar
import com.github.unchama.seichiassist.data.GridTemplate
import com.github.unchama.seichiassist.data.Mana
import com.github.unchama.seichiassist.data.player.settings.PlayerSettings
import com.github.unchama.seichiassist.data.potioneffect.FastDiggingEffect
import com.github.unchama.seichiassist.data.subhome.SubHome
import com.github.unchama.seichiassist.event.SeichiLevelUpEvent
import com.github.unchama.seichiassist.minestack.MineStackUsageHistory
import com.github.unchama.seichiassist.task.MebiusTask
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.ClosedRangeWithComparator
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.util.Util.DirectionType
import com.github.unchama.seichiassist.util.exp.ExperienceManager
import com.github.unchama.seichiassist.util.exp.IExperienceManager
import com.github.unchama.targetedeffect.*
import com.github.unchama.targetedeffect.ops.plus
import com.github.unchama.targetedeffect.player.asTargetedEffect
import com.github.unchama.util.createInventory
import org.bukkit.*
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt


class PlayerData constructor(@Deprecated("PlayerDataはuuidに依存するべきではない") val uuid: UUID) {
  //読み込み済みフラグ
  var loaded = false

  val settings = PlayerSettings()

  //region session-specific data
  // TODO many properties here might not be right to belong here

  //ハーフブロック破壊抑制用
  private var allowBreakingHalfBlocks = false

  //チェスト破壊トグル
  var chestflag = true

  //各統計値差分計算用配列
  private val statisticsData: MutableList<Int> = (MaterialSets.materials - exclude)
      .map { player.getStatistic(Statistic.MINE_BLOCK, it) }
      .toMutableList()

  @get:JvmName("canCreateRegion")
  var canCreateRegion = true
  var unitPerClick = 1
    private set

  //３０分間のデータを保存する．
  val halfhourblock: MineBlock = MineBlock()

  //今回の採掘速度上昇レベルを格納
  var minespeedlv = 0

  //前回の採掘速度上昇レベルを格納
  var lastminespeedlv = 0

  //持ってるポーションエフェクト全てを格納する．
  val effectdatalist: MutableList<FastDiggingEffect> = LinkedList()

  //プレイ時間差分計算用int
  private var totalPlayTick: Int? = null

  //投票受け取りボタン連打防止用
  var votecooldownflag = true

  //ガチャボタン連打防止用
  var gachacooldownflag = true

  //インベントリ共有ボタン連打防止用
  var shareinvcooldownflag = true

  var selectHomeNum = 0
  var setHomeNameNum = 0
  var isSubHomeNameChange = false

  var samepageflag = false//実績ショップ用

  //MineStackの履歴
  var hisotryData: MineStackUsageHistory = MineStackUsageHistory()

  //経験値マネージャ
  private val expmanager: IExperienceManager = ExperienceManager(player)

  var titlepage = 1 //実績メニュー用汎用ページ指定

  //現在座標
  var loc: Location? = null

  val mebius: MebiusTask = MebiusTask(this)

  //放置時間
  var idleMinute = 0

  //endregion

  //共有インベントリにアイテムが入っているかどうか
  var contentsPresentInSharedInventory = false

  //ガチャの基準となるポイント
  var gachapoint = 0

  //現在のプレイヤーレベル
  var level = 1
  //詫び券をあげる数
  var unclaimedApologyItems = 0
  //拡張インベントリ
  var pocketInventory: Inventory = createInventory(size = 1.rows(), title = "$DARK_PURPLE${BOLD}4次元ポケット")
    get() {
      // 許容サイズが大きくなっていたら新規インベントリにアイテムをコピーしてそのインベントリを持ち回す
      if (field.size < pocketSize) {
        field = Bukkit.getServer()
            .createInventory(null, pocketSize, "$DARK_PURPLE${BOLD}4次元ポケット")
            .also { field.forEachIndexed(it::setItem) }
      }

      return field
    }

  //ワールドガード保護自動設定用
  var regionCount = 0

  var starLevels = StarLevel(0, 0, 0)

  var minestack = MineStack()

  //プレイ時間
  var playTick = 0
  //トータル破壊ブロック
  var totalbreaknum = 0.toLong()
  //整地量バー
  val expbar: ExpBar = ExpBar(this, player)
  //合計経験値
  var totalexp = 0
  //合計経験値統合済みフラグ
  var expmarge: Byte = 0
  //特典受け取り済み投票数
  var p_givenvote = 0

  //連続・通算ログイン用
  var lastcheckdate: String? = null
  var loginStatus = LoginStatus(null, 0, 0)

  //期間限定ログイン用
  var LimitedLoginCount = 0

  var ChainVote = 0

  //アクティブスキル関連データ
  var activeskilldata: ActiveSkillData = ActiveSkillData()

  private val subHomeMap = HashMap<Int, SubHome>()

  //二つ名解禁フラグ保存用
  var TitleFlags: BitSet = BitSet(10000).apply { set(1) }
  //二つ名関連用にp_vote(投票数)を引っ張る。(予期せぬエラー回避のため名前を複雑化)
  var p_vote_forT = 0
  //二つ名配布予約NOの保存
  var giveachvNo = 0
  //実績ポイント用
  var achievePoint = AchievementPoint(cumulativeTotal = 0, used = 0, conversionCount = 0)

  var buildCount = BuildCount(1, BigDecimal.ZERO, 0)
  // 1周年記念
  var anniversary = false

  //グリッド式保護関連
  private var claimUnit = ClaimUnit(0, 0, 0, 0)
  var templateMap: MutableMap<Int, GridTemplate> = HashMap()

  //投票妖精関連
  var usingVotingFairy = false
  private val dummyDate = GregorianCalendar(2100, 1, 1, 0, 0, 0)

  var voteFairyPeriod = ClosedRangeWithComparator(dummyDate, dummyDate, Comparator { o1, o2 ->
    o1.timeInMillis.compareTo(o2.timeInMillis)
  })
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

  var giganticBerserk = GiganticBerserk(0, 0, 0, false, 0)

  //region calculated
  // TODO many properties here may be inlined and deleted

  var votingFairyStartTime
    get() = voteFairyPeriod.start
    set(value) {
      voteFairyPeriod = ClosedRangeWithComparator(value, voteFairyPeriod.endInclusive, voteFairyPeriod.comparator)
    }

  var votingFairyEndTime
    get() = voteFairyPeriod.endInclusive
    set(value) {
      voteFairyPeriod = ClosedRangeWithComparator(voteFairyPeriod.start, value, voteFairyPeriod.comparator)
    }

  @Deprecated("PlayerDataはPlayerに依存するべきではない。")
  private val player: Player
    get() = Bukkit.getPlayer(uuid)

  //プレイヤー名
  val lowercaseName: String
    get() = Util.getName(player)

  /**
   * スターレベルの合計を返すショートカットフィールド。
   */
  val totalStarLevel
    get() = starLevels.total()

  val subHomeEntries: Set<Map.Entry<Int, SubHome>>
    get() = subHomeMap.toMap().entries

  val unitMap: Map<DirectionType, Int>
    get() {
      val unitMap = EnumMap<DirectionType, Int>(DirectionType::class.java) //HashMap<DirectionType, Int>()

      unitMap[DirectionType.AHEAD] = this.claimUnit.ahead
      unitMap[DirectionType.BEHIND] = this.claimUnit.behind
      unitMap[DirectionType.RIGHT] = this.claimUnit.right
      unitMap[DirectionType.LEFT] = this.claimUnit.left

      return unitMap
    }

  val gridChunkAmount: Int
    get() = (this.claimUnit.ahead + 1 + this.claimUnit.behind) * (this.claimUnit.right + 1 + this.claimUnit.left)

  //オフラインかどうか
  val isOffline: Boolean
    get() = SeichiAssist.instance.server.getPlayer(uuid) == null

  //四次元ポケットのサイズを取得
  private val pocketSize: Int
    get() = when {
      level < 6 -> 9 * 3
      level < 16 -> 9 * 3
      level < 26 -> 9 * 3
      level < 36 -> 9 * 3
      level < 46 -> 9 * 3
      level < 56 -> 9 * 4
      level < 66 -> 9 * 5
      else -> 9 * 6
    }

  var GBexp
    set(value) {
      giganticBerserk = giganticBerserk.copy(exp = value)
    }
    get() = giganticBerserk.exp

  var isGBStageUp
    set(value) {
      giganticBerserk = giganticBerserk.copy(canEvolve = value)
    }
    get() = giganticBerserk.canEvolve

  // FIXME: BAD NAME; not clear meaning
  var GBcd: Int
    set(value) {
      giganticBerserk = giganticBerserk.copy(cd = value)
    }
    get() = giganticBerserk.cd
  //endregion

  //join時とonenable時、プレイヤーデータを最新の状態に更新
  fun updateOnJoin() {
    //破壊量データ(before)を設定
    halfhourblock.before = totalbreaknum
    updateLevel()
    notifySorryForBug()
    activeskilldata.updateOnJoin(player, level)
    //サーバー保管経験値をクライアントに読み込み
    loadTotalExp()
    isVotingFairy()
  }

  @JvmOverloads
  fun updateNickname(id1: Int = settings.nickName.id1, id2: Int = settings.nickName.id2, id3: Int = settings.nickName.id3, style: PlayerNickName.Style = settings.nickName.style) {
    settings.nickName = settings.nickName.copy(id1 = id1, id2 = id2, id3 = id3, style = style)
  }

  //quit時とondisable時、プレイヤーデータを最新の状態に更新
  fun updateOnQuit() {
    //総整地量を更新
    updateAndCalcMinedBlockAmount()
    //総プレイ時間更新
    updatePlayTick()

    activeskilldata.updateOnQuit()
    expbar.remove()
    //クライアント経験値をサーバー保管
    saveTotalExp()
  }

  fun giganticBerserkLevelUp() {
    val currentLevel = giganticBerserk.level
    giganticBerserk = if (currentLevel >= 10) giganticBerserk else giganticBerserk.copy(level = currentLevel + 1, exp = 0)
  }

  fun recalculateAchievePoint() {
    val max = TitleFlags
        .stream() // index
        .filter { it in 1000..9799 }
        .count().toInt() /* Safe Conversation: BitSet indexes -> Int */ * 10
    achievePoint = achievePoint.copy(cumulativeTotal = max)
  }

  fun consumeAchievePoint(amount: Int) {
    achievePoint = achievePoint.copy(used = achievePoint.used + amount)
  }

  fun convertEffectPointToAchievePoint() {
    achievePoint = achievePoint.copy(conversionCount = achievePoint.conversionCount + 1)
    activeskilldata.effectpoint -= 10
  }

  //詫びガチャの通知
  private fun notifySorryForBug() {
    if (unclaimedApologyItems > 0) {
      player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
      player.sendMessage(GREEN.toString() + "運営チームから" + unclaimedApologyItems + "枚の" + GOLD + "ガチャ券" + WHITE + "が届いています！\n木の棒メニューから受け取ってください")
    }
  }

  //エフェクトデータのdurationを60秒引く
  fun calcEffectData() {
    //tmplistを作成
    val tmplist = ArrayList<FastDiggingEffect>()
    //effectdatalistのdurationをすべて60秒（1200tick）引いてtmplistに格納
    for (ed in effectdatalist) {
      ed.duration -= 1200
      tmplist += ed
    }
    //tmplistのdurationが3秒以下（60tick）のものはeffectdatalistから削除
    for (ed in tmplist) {
      if (ed.duration <= 60) {
        effectdatalist.remove(ed)
      }
    }
  }

  //レベルを更新
  fun updateLevel() {
    updatePlayerLevel()
    updateStarLevel()
    setDisplayName()
    expbar.calculate()
  }

  //表示される名前に整地レベルor二つ名を追加
  fun setDisplayName() {
    var displayname = Util.getName(player)
    //放置時に色を変える
    val idleColor = when {
      idleMinute >= 10 -> DARK_GRAY
      idleMinute >= 3 -> GRAY
      else -> ""
    }.toString()

    //表示を追加する処理
    displayname = idleColor + if (settings.nickName.id1 == 0 && settings.nickName.id2 == 0 && settings.nickName.id3 == 0) {
      if (totalStarLevel <= 0) {
        "[ Lv$level ]$displayname$WHITE"
      } else {
        "[Lv$level☆$totalStarLevel]$displayname$WHITE"
      }
    } else {
      val displayTitle1 = SeichiAssist.seichiAssistConfig.getTitle1(settings.nickName.id1)
      val displayTitle2 = SeichiAssist.seichiAssistConfig.getTitle2(settings.nickName.id2)
      val displayTitle3 = SeichiAssist.seichiAssistConfig.getTitle3(settings.nickName.id3)
      "[$displayTitle1$displayTitle2$displayTitle3]$displayname$WHITE"
    }

    player.displayName = displayname
    player.playerListName = displayname
  }


  //プレイヤーレベルを計算し、更新する。
  private fun updatePlayerLevel() {
    //現在のランクを取得
    var i = level
    //既にレベル上限に達していたら終了
    if (i >= LevelThresholds.levelExpThresholds.size) {
      return
    }
    //ランクが上がらなくなるまで処理
    while (LevelThresholds.levelExpThresholds[i] <= totalbreaknum && i + 1 <= LevelThresholds.levelExpThresholds.size) {

      //レベルアップ時のメッセージ
      player.sendMessage(GOLD.toString() + "ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww【Lv(" + i + ")→Lv(" + (i + 1) + ")】")
      //レベルアップイベント着火
      Bukkit.getPluginManager().callEvent(SeichiLevelUpEvent(player, this, i + 1))
      //レベルアップ時の花火の打ち上げ
      val loc = player.location
      Util.launchFireWorks(loc)
      val lvmessage = SeichiAssist.seichiAssistConfig.getLvMessage(i + 1)
      if (lvmessage.isNotEmpty()) {
        player.sendMessage(AQUA.toString() + lvmessage)
      }
      i++
      if (activeskilldata.mana.isLoaded) {
        //マナ最大値の更新
        activeskilldata.mana.onLevelUp(player, i)
      }
      //レベル上限に達したら終了
      if (i >= LevelThresholds.levelExpThresholds.size) {
        break
      }
    }
    level = i
  }

  //スターレベルの計算、更新
  /**
   * スターレベルの計算、更新を行う。
   * このメソッドはスター数が増えたときにメッセージを送信する副作用を持つ。
   */
  fun updateStarLevel() {
    //処理前の各レベルを取得
    val oldStars = starLevels.total()
    val oldBreakStars = starLevels.fromBreakAmount
    val oldTimeStars = starLevels.fromConnectionTime
    //処理後のレベルを保存する入れ物
    val newBreakStars = (totalbreaknum / 87115000).toInt()

    //整地量の確認
    if (oldBreakStars < newBreakStars) {
      player.sendMessage(GOLD.toString() + "ｽﾀｰﾚﾍﾞﾙ(整地量)がﾚﾍﾞﾙｱｯﾌﾟ!!【☆(" + oldBreakStars + ")→☆(" + newBreakStars + ")】")
      starLevels = starLevels.copy(fromBreakAmount = newBreakStars)
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
      player.sendMessage("$GOLD★☆★ｽﾀｰﾚﾍﾞﾙUP!!!★☆★【☆($oldStars)→☆($newStars)】")
    }
  }

  //総プレイ時間を更新する
  fun updatePlayTick() {
    // WARN: 1分毎にupdatePlayTickが呼び出されるというコンテクストに依存している.
    val nowTotalPlayTick = player.getStatistic(Statistic.PLAY_ONE_TICK)
    val diff = nowTotalPlayTick - (totalPlayTick ?: nowTotalPlayTick)

    totalPlayTick = nowTotalPlayTick
    playTick += diff
  }

  //総破壊ブロック数を更新する
  fun updateAndCalcMinedBlockAmount(): Int {
    var sum = 0.0
    for ((i, m) in (MaterialSets.materials - exclude).withIndex()) {
      val materialStatistics = player.getStatistic(Statistic.MINE_BLOCK, m)
      val increase = materialStatistics - statisticsData[i]
      val amount = calcBlockExp(m, increase)
      sum += amount
      if (SeichiAssist.DEBUG) {
        if (amount > 0.0) {
          player.sendMessage("calcの値:$amount($m)")
        }
      }
      statisticsData[i] = materialStatistics
    }
    //double値を四捨五入し、整地量に追加する整数xを出す
    val x = sum.roundToInt()

    //xを整地量に追加
    totalbreaknum += x
    return x
  }

  //ブロック別整地数反映量の調節
  private fun calcBlockExp(m: Material, i: Int): Double {
    val amount = i.toDouble()
    //ブロック別重み分け
    val matMult = when (m) {
      //DIRTとGRASSは二重カウントされているので半分に
      Material.DIRT -> 0.5
      Material.GRASS -> 0.5

      //氷塊とマグマブロックの整地量を2倍
      Material.PACKED_ICE -> 2.0
      Material.MAGMA -> 2.0

      else -> 1.0
    }

    val managedWorld = ManagedWorld.fromBukkitWorld(player.world)
    val swMult = if (managedWorld?.isSeichi == true) 1.0 else 0.0
    val sw01PenaltyMult = if (ManagedWorld.WORLD_SW == managedWorld) 0.8 else 1.0
    return amount * matMult * swMult * sw01PenaltyMult
  }

  //現在の採掘量順位
  fun calcPlayerRank(): Int {
    //ランク用関数
    var i = 0
    val t = totalbreaknum
    if (SeichiAssist.ranklist.size == 0) {
      return 1
    }
    var rankdata = SeichiAssist.ranklist[i]
    //ランクが上がらなくなるまで処理
    while (rankdata.totalbreaknum > t) {
      i++
      rankdata = SeichiAssist.ranklist[i]
    }
    return i + 1
  }

  fun calcPlayerApple(): Int {
    //ランク用関数
    var i = 0
    val t = p_apple
    if (SeichiAssist.ranklist_p_apple.size == 0) {
      return 1
    }
    var rankdata = SeichiAssist.ranklist_p_apple[i]
    //ランクが上がらなくなるまで処理
    while (rankdata.p_apple > t) {
      i++
      rankdata = SeichiAssist.ranklist_p_apple[i]
    }
    return i + 1
  }

  //パッシブスキルの獲得量表示
  fun getPassiveExp(): Double {
    return when {
      level < 8 -> 0.0
      level < 18 -> SeichiAssist.seichiAssistConfig.getDropExplevel(1)
      level < 28 -> SeichiAssist.seichiAssistConfig.getDropExplevel(2)
      level < 38 -> SeichiAssist.seichiAssistConfig.getDropExplevel(3)
      level < 48 -> SeichiAssist.seichiAssistConfig.getDropExplevel(4)
      level < 58 -> SeichiAssist.seichiAssistConfig.getDropExplevel(5)
      level < 68 -> SeichiAssist.seichiAssistConfig.getDropExplevel(6)
      level < 78 -> SeichiAssist.seichiAssistConfig.getDropExplevel(7)
      level < 88 -> SeichiAssist.seichiAssistConfig.getDropExplevel(8)
      level < 98 -> SeichiAssist.seichiAssistConfig.getDropExplevel(9)
      else -> SeichiAssist.seichiAssistConfig.getDropExplevel(10)
    }
  }

  //サブホームの位置をセットする
  fun setSubHomeLocation(location: Location, subHomeIndex: Int) {
    if (subHomeIndex >= 0 && subHomeIndex < SeichiAssist.seichiAssistConfig.subHomeMax) {
      val currentSubHome = this.subHomeMap[subHomeIndex]
      val currentSubHomeName = currentSubHome?.name

      this.subHomeMap[subHomeIndex] = SubHome(location, currentSubHomeName)
    }
  }

  fun setSubHomeName(name: String?, subHomeIndex: Int) {
    if (subHomeIndex >= 0 && subHomeIndex < SeichiAssist.seichiAssistConfig.subHomeMax) {
      val currentSubHome = this.subHomeMap[subHomeIndex]
      if (currentSubHome != null) {
        this.subHomeMap[subHomeIndex] = SubHome(currentSubHome.location, name)
      }
    }
  }

  // サブホームの位置を読み込む
  fun getSubHomeLocation(subHomeIndex: Int): Location? {
    val subHome = this.subHomeMap[subHomeIndex]
    return subHome?.location
  }

  fun getSubHomeName(subHomeIndex: Int): String {
    val subHome = this.subHomeMap[subHomeIndex]
    val subHomeName = subHome?.name
    return subHomeName ?: "サブホームポイント$subHomeIndex"
  }

  private fun saveTotalExp() {
    totalexp = expmanager.currentExp
  }

  private fun loadTotalExp() {
    val internalServerId = SeichiAssist.seichiAssistConfig.serverNum
    //経験値が統合されてない場合は統合する
    if (expmarge.toInt() != 0x07 && internalServerId in 1..3) {
      if (expmarge and (0x01 shl internalServerId - 1).toByte() == 0.toByte()) {
        if (expmarge.toInt() == 0) {
          // 初回は加算じゃなくベースとして代入にする
          totalexp = expmanager.currentExp
        } else {
          totalexp += expmanager.currentExp
        }
        expmarge = expmarge or (0x01 shl internalServerId - 1).toByte()
      }
    }
    expmanager.setExp(totalexp)
  }

  fun canBreakHalfBlock(): Boolean {
    return this.allowBreakingHalfBlocks
  }

  fun canGridExtend(directionType: DirectionType, world: String): Boolean {
    val limit = config.getGridLimitPerWorld(world)
    val chunkMap = unitMap

    //チャンクを拡大すると仮定する
    val assumedAmoont = chunkMap.getValue(directionType) + this.unitPerClick

    //一応すべての拡張値を出しておく
    val ahead = chunkMap.getValue(DirectionType.AHEAD)
    val behind = chunkMap.getValue(DirectionType.BEHIND)
    val right = chunkMap.getValue(DirectionType.RIGHT)
    val left = chunkMap.getValue(DirectionType.LEFT)

    //合計チャンク再計算値
    val assumedUnitAmount = when (directionType) {
      DirectionType.AHEAD -> (assumedAmoont + 1 + behind) * (right + 1 + left)
      DirectionType.BEHIND -> (ahead + 1 + assumedAmoont) * (right + 1 + left)
      DirectionType.RIGHT -> (ahead + 1 + behind) * (assumedAmoont + 1 + left)
      DirectionType.LEFT -> (ahead + 1 + behind) * (right + 1 + assumedAmoont)
    }

    return assumedUnitAmount <= limit

  }

  fun canGridReduce(directionType: DirectionType): Boolean {
    val chunkMap = unitMap

    //減らしたと仮定する
    val assumedAmount = chunkMap.getValue(directionType) - unitPerClick
    return assumedAmount >= 0
  }

  fun setUnitAmount(directionType: DirectionType, amount: Int) {
    when (directionType) {
      DirectionType.AHEAD -> this.claimUnit = this.claimUnit.copy(ahead = amount)
      DirectionType.BEHIND -> this.claimUnit = this.claimUnit.copy(behind = amount)
      DirectionType.RIGHT -> this.claimUnit = this.claimUnit.copy(right = amount)
      DirectionType.LEFT -> this.claimUnit = this.claimUnit.copy(left = amount)
    }
  }

  fun addUnitAmount(directionType: DirectionType, amount: Int) {
    when (directionType) {
      DirectionType.AHEAD -> this.claimUnit = this.claimUnit.copy(ahead = this.claimUnit.ahead + amount)
      DirectionType.BEHIND -> this.claimUnit = this.claimUnit.copy(behind = this.claimUnit.behind + amount)
      DirectionType.RIGHT -> this.claimUnit = this.claimUnit.copy(right = this.claimUnit.right + amount)
      DirectionType.LEFT -> this.claimUnit = this.claimUnit.copy(left = this.claimUnit.left + amount)
    }
  }

  fun toggleUnitPerGrid() {
    when {
      this.unitPerClick == 1 -> this.unitPerClick = 10
      this.unitPerClick == 10 -> this.unitPerClick = 100
      this.unitPerClick == 100 -> this.unitPerClick = 1
    }
  }

  @AntiTypesafe
  fun getVotingFairyStartTimeAsString(): String {
    val cal = this.votingFairyStartTime
    return if (votingFairyStartTime == dummyDate) {
      //設定されてない場合
      ",,,,,"
    } else {
      //設定されてる場合
      val date = cal.time
      val format = SimpleDateFormat("yyyy,MM,dd,HH,mm,")
      format.format(date)
    }
  }

  fun setVotingFairyTime(@AntiTypesafe str: String) {
    val s = str.split(",".toRegex()).toTypedArray()
    if (s.slice(0..4).all(String::isNotEmpty)) {
      val year = s[0].toInt()
      val month = s[1].toInt() - 1
      val dayOfMonth = s[2].toInt()
      val starts = GregorianCalendar(year, month, dayOfMonth, Integer.parseInt(s[3]), Integer.parseInt(s[4]))

      var min = Integer.parseInt(s[4]) + 1
      var hour = Integer.parseInt(s[3])

      min = if (this.toggleVotingFairy % 2 != 0) min + 30 else min
      hour = if (this.toggleVotingFairy == 2 or 3)
        hour + 1
      else if (this.toggleVotingFairy == 4)
        hour + 2
      else
        hour

      val ends = GregorianCalendar(year, month, dayOfMonth, hour, min)

      this.votingFairyStartTime = starts
      this.votingFairyEndTime = ends
    }
  }

  private fun isVotingFairy() {
    //効果は継続しているか
    if (this.usingVotingFairy && !Util.isVotingFairyPeriod(this.votingFairyStartTime, this.votingFairyEndTime)) {
      this.usingVotingFairy = false
      player.sendMessage(LIGHT_PURPLE.toString() + "" + BOLD + "妖精は何処かへ行ってしまったようだ...")
    } else if (this.usingVotingFairy) {
      VotingFairyTask.speak(player, "おかえり！" + player.name, true)
    }
  }

  fun setContributionPoint(addAmount: Int) {
    val mana = Mana()

    //負数(入力ミスによるやり直し中プレイヤーがオンラインだった場合)の時
    if (addAmount < 0) {
      player.sendMessage(GREEN.toString() + "" + BOLD + "入力者のミスによって得た不正なマナを" + -10 * addAmount + "分減少させました.")
      player.sendMessage(GREEN.toString() + "" + BOLD + "申し訳ございません.")
    } else {
      player.sendMessage(GREEN.toString() + "" + BOLD + "運営からあなたの整地鯖への貢献報酬として")
      player.sendMessage(GREEN.toString() + "" + BOLD + "マナの上限値が" + 10 * addAmount + "上昇しました．(永久)")
    }
    this.added_mana += addAmount

    mana.calcAndSetMax(player, this.level)
  }

  @Suppress("RedundantSuspendModifier")
  suspend fun toggleMessageFlag(): TargetedEffect<Player> {
    settings.receiveFastDiggingEffectStats = !settings.receiveFastDiggingEffectStats

    val responseMessage = if (settings.receiveFastDiggingEffectStats) {
      "${GREEN}内訳表示:ON(OFFに戻したい時は再度コマンドを実行します。)"
    } else {
      "${GREEN}内訳表示:OFF"
    }

    return responseMessage.asMessageEffect()
  }

  /**
   * 運営権限により強制的に実績を解除することを試みる。
   * 解除に成功し、このインスタンスが指す[Player]がオンラインであるならばその[Player]に解除の旨がチャットにて通知される。
   *
   * @param number 解除対象の実績番号
   * @return この作用の実行者に向け操作の結果を記述する[MessageToSender]
   */
  @Suppress("RedundantSuspendModifier")
  suspend fun tryForcefullyUnlockAchievement(number: Int): TargetedEffect<CommandSender> =
      if (!TitleFlags[number]) {
        TitleFlags.set(number)
        Bukkit.getPlayer(uuid)?.sendMessage("運営チームよりNo${number}の実績が配布されました。")

        "$lowercaseName に実績No. $number を${GREEN}付与${RESET}しました。".asMessageEffect()
      } else {
        "$GRAY$lowercaseName は既に実績No. $number を獲得しています。".asMessageEffect()
      }

  /**
   * 運営権限により強制的に実績を剥奪することを試みる。
   * 実績剥奪の通知はプレーヤーには行われない。
   *
   * @param number 解除対象の実績番号
   * @return この作用の実行者に向け操作の結果を記述する[TargetedEffect]
   */
  @Suppress("RedundantSuspendModifier")
  suspend fun forcefullyDepriveAchievement(number: Int): TargetedEffect<CommandSender> =
      if (!TitleFlags[number]) {
        TitleFlags[number] = false

        "$lowercaseName から実績No. $number を${RED}剥奪${GREEN}しました。".asMessageEffect()
      } else {
        "$GRAY$lowercaseName は実績No. $number を獲得していません。".asMessageEffect()
      }

  /**
   * プレーヤーに付与されるべき採掘速度上昇効果を適用する[TargetedEffect].
   */
  suspend fun computeFastDiggingEffect(): TargetedEffect<Player> {
    val activeEffects = effectdatalist.toList()

    val amplifierSum = activeEffects.map { it.amplifier }.sum()
    val maxDuration = activeEffects.map { it.duration }.max() ?: 0
    val computedAmplifier = floor(amplifierSum - 1).toInt()

    val maxSpeed: Int = settings.fastDiggingEffectSuppression.maximumAllowedEffectAmplifier()

    // 実際に適用されるeffect量
    val amplifier = min(computedAmplifier, maxSpeed)

    return if (amplifier >= 0) {
      PotionEffect(PotionEffectType.FAST_DIGGING, maxDuration, amplifier, false, false)
    } else {
      // 実際のeffect値が0より小さいときはeffectを適用しない
      PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false)
    }.asTargetedEffect()
  }

  /**
   * 保護申請の番号を更新させる[UnfocusedEffect]
   */
  val incrementRegionNumber: UnfocusedEffect =
      unfocusedEffect {
        this.regionCount += 1
      }

  val toggleExpBarVisibility: TargetedEffect<Player> =
      unfocusedEffect {
        this.expbar.isVisible = !this.expbar.isVisible
      } + deferredEffect {
        when {
          this.expbar.isVisible -> "${GREEN}整地量バー表示"
          else -> "${RED}整地量バー非表示"
        }.asMessageEffect()
      }

  companion object {
    internal var config = SeichiAssist.seichiAssistConfig

    //TODO:もちろんここにあるべきではない
    const val passiveSkillProbability = 10

    val exclude = EnumSet.of(Material.GRASS_PATH, Material.SOIL, Material.MOB_SPAWNER,
        Material.CAULDRON, Material.ENDER_CHEST)
  }
}

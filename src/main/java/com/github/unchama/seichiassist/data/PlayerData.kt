package com.github.unchama.seichiassist.data

import com.github.unchama.messaging.MessageToSender
import com.github.unchama.messaging.asResponseToSender
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.Worlds
import com.github.unchama.seichiassist.data.subhome.SubHome
import com.github.unchama.seichiassist.event.SeichiLevelUpEvent
import com.github.unchama.seichiassist.minestack.MineStackHistoryData
import com.github.unchama.seichiassist.minestack.MineStackObj
import com.github.unchama.seichiassist.task.MebiusTask
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.ExperienceManager
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.util.Util.DirectionType
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.experimental.and
import kotlin.experimental.or


class PlayerData(player: Player) {
    //読み込み済みフラグ
    var loaded: Boolean = false
    //プレイヤー名
    var name: String
    //UUID
    var uuid: UUID
    //エフェクトのフラグ
    var effectflag: Int = 0
    //内訳メッセージを出すフラグ
    var messageflag: Boolean = false
    //1分間のデータを保存するincrease:１分間の採掘量
    //public MineBlock minuteblock;
    //３０分間のデータを保存する．
    var halfhourblock: MineBlock
    //ガチャの基準となるポイント
    var gachapoint: Int = 0
    //最後のガチャポイントデータ
    var lastgachapoint: Int = 0
    //ガチャ受け取りフラグ
    var gachaflag: Boolean = false
    //今回の採掘速度上昇レベルを格納
    var minespeedlv: Int = 0
    //前回の採掘速度上昇レベルを格納
    var lastminespeedlv: Int = 0
    //持ってるポーションエフェクト全てを格納する．
    var effectdatalist: MutableList<EffectData>
    //現在のプレイヤーレベル
    var level: Int = 0
    //詫び券をあげる数
    var numofsorryforbug: Int = 0
    //拡張インベントリ
    var inventory: Inventory
    //ワールドガード保護自動設定用
    var rgnum: Int = 0

    //スターレベル用数値
    //スターレベル(合計値)
    var starlevel: Int = 0
    //各項目別の取得スターレベル
    var starlevel_Break: Int = 0 //整地量
    var starlevel_Time: Int = 0 //参加時間
    var starlevel_Event: Int = 0  //イベント実績

    //MineStack
    //public MineStack minestack;

    var minestack = MineStack()
    //MineStackFlag
    var minestackflag: Boolean = false
    //プレイ時間差分計算用int
    var servertick: Int = 0
    //プレイ時間
    var playtick: Int = 0
    //キルログ表示トグル
    var dispkilllogflag: Boolean = false
    //全体通知音消音トグル
    var everysoundflag: Boolean = false
    //全体メッセージ非表示トグル
    var everymessageflag: Boolean = false
    //ワールドガード保護ログ表示トグル
    var dispworldguardlogflag: Boolean = false
    //複数種類破壊トグル
    var multipleidbreakflag: Boolean = false

    //チェスト破壊トグル
    var chestflag: Boolean = false

    //PvPトグル
    var pvpflag: Boolean = false
    //現在座標
    var loc: Location? = null
    //放置時間
    var idletime: Int = 0
    //トータル破壊ブロック
    var totalbreaknum: Long = 0
    //整地量バー
    var expbar: ExpBar
    //合計経験値
    var totalexp: Int = 0
    //経験値マネージャ
    var expmanager: ExperienceManager
    //合計経験値統合済みフラグ
    var expmarge: Byte = 0
    //各統計値差分計算用配列
    private val staticdata: MutableList<Int>
    //特典受け取り済み投票数
    var p_givenvote: Int = 0
    //投票受け取りボタン連打防止用
    var votecooldownflag: Boolean = false

    //連続・通算ログイン用
    var lastcheckdate: String? = null
    var ChainJoin: Int = 0
    var TotalJoin: Int = 0

    //期間限定ログイン用
    var LimitedLoginCount: Int = 0

    var ChainVote: Int = 0

    //アクティブスキル関連データ
    var activeskilldata: ActiveSkillData

    //MebiusTask
    var mebius: MebiusTask

    //ガチャボタン連打防止用
    var gachacooldownflag: Boolean = false

    //インベントリ共有トグル
    var contentsPresentInSharedInventory: Boolean = false
    //インベントリ共有ボタン連打防止用
    var shareinvcooldownflag: Boolean = false

    var selectHomeNum: Int = 0
    var setHomeNameNum: Int = 0
    private val subHomeMap = HashMap<Int, SubHome>()
    var isSubHomeNameChange: Boolean = false

    //LV・二つ名表示切替用
    var displayTypeLv: Boolean = false
    //表示二つ名の指定用
    var displayTitle1No: Int = 0
    var displayTitle2No: Int = 0
    var displayTitle3No: Int = 0
    //二つ名解禁フラグ保存用
    var TitleFlags: BitSet
    //二つ名関連用にp_vote(投票数)を引っ張る。(予期せぬエラー回避のため名前を複雑化)
    var p_vote_forT: Int = 0
    //二つ名配布予約NOの保存
    var giveachvNo: Int = 0
    //実績ポイント用
    var achvPointMAX: Int = 0//累計取得量
    var achvPointUSE: Int = 0//消費量
    var achvPoint: Int = 0//現在の残量
    var achvChangenum: Int = 0//投票ptからの変換回数
    var titlepage: Int = 0 //実績メニュー用汎用ページ指定
    var samepageflag: Boolean = false//実績ショップ用


    //建築LV
    private var build_lv: Int = 0
    //設置ブロック数
    private var build_count: BigDecimal? = null
    //設置ブロックサーバー統合フラグ
    private var build_count_flg: Byte = 0

    // 1周年記念
    var anniversary: Boolean = false

    //ハーフブロック破壊抑制用
    private var halfBreakFlag: Boolean = false

    //グリッド式保護関連
    private var aheadUnit: Int = 0
    private var behindUnit: Int = 0
    private var rightUnit: Int = 0
    private var leftUnit: Int = 0
    private var canCreateRegion: Boolean = false
    var unitPerClick: Int = 0
        private set
    var templateMap: Map<Int, GridTemplate>? = null

    //投票妖精関連
    var usingVotingFairy: Boolean = false
    var VotingFairyStartTime: Calendar? = null
    var VotingFairyEndTime: Calendar? = null
    var hasVotingFairyMana: Int = 0
    var VotingFairyRecoveryValue: Int = 0
    var toggleGiveApple: Int = 0
    var toggleVotingFairy: Int = 0
    var p_apple: Long = 0
    var toggleVFSound: Boolean = false

    //貢献度pt
    var added_mana: Int = 0
    var contribute_point: Int = 0

    //正月イベント用
    var hasNewYearSobaGive: Boolean = false
    var newYearBagAmount: Int = 0

    //バレンタインイベント用
    var hasChocoGave: Boolean = false

    //MineStackの履歴
    var hisotryData: MineStackHistoryData
    //MineStack検索機能使用中かどうか
    var isSearching: Boolean = false
    //MineStack検索保存用Map
    var indexMap: Map<Int, MineStackObj>

    var GBstage: Int = 0
    var GBexp: Int = 0
    var GBlevel: Int = 0
    var isGBStageUp: Boolean = false
    var GBcd: Int = 0


    //オフラインかどうか
    val isOffline: Boolean
        get() = SeichiAssist.instance.server.getPlayer(uuid) == null
    //四次元ポケットのサイズを取得
    val pocketSize: Int
        get() = if (level < 6) {
            9 * 3
        } else if (level < 16) {
            9 * 3
        } else if (level < 26) {
            9 * 3
        } else if (level < 36) {
            9 * 3
        } else if (level < 46) {
            9 * 3
        } else if (level < 56) {
            9 * 4
        } else if (level < 66) {
            9 * 5
        } else {
            9 * 6
        }

    val subHomeEntries: Set<Map.Entry<Int, SubHome>>
        get() = subHomeMap.toMap().entries

    val unitMap: Map<DirectionType, Int>
        get() {
            val unitMap = HashMap<DirectionType, Int>()

            unitMap[DirectionType.AHEAD] = this.aheadUnit
            unitMap[DirectionType.BEHIND] = this.behindUnit
            unitMap[DirectionType.RIGHT] = this.rightUnit
            unitMap[DirectionType.LEFT] = this.leftUnit

            return unitMap
        }

    val gridChunkAmount: Int
        get() = (this.aheadUnit + 1 + this.behindUnit) * (this.rightUnit + 1 + this.leftUnit)


    init {
        //初期値を設定
        this.loaded = false
        this.name = Util.getName(player)
        this.uuid = player.uniqueId
        this.effectflag = 0
        this.messageflag = false
        //this.minuteblock = new MineBlock();
        this.halfhourblock = MineBlock()
        this.gachapoint = 0
        this.lastgachapoint = 0
        this.gachaflag = true
        this.minespeedlv = 0
        this.lastminespeedlv = 0
        this.effectdatalist = ArrayList()
        this.level = 1
        this.mebius = MebiusTask(this)
        this.numofsorryforbug = 0
        this.inventory = SeichiAssist.instance.server.createInventory(null, 9 * 1, ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "4次元ポケット")
        this.rgnum = 0
        this.minestack = MineStack()
        this.minestackflag = true
        this.servertick = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_TICK)
        this.playtick = 0
        this.dispkilllogflag = false
        this.dispworldguardlogflag = true
        this.multipleidbreakflag = false
        this.pvpflag = false
        this.loc = null
        this.idletime = 0
        this.staticdata = ArrayList()
        this.totalbreaknum = 0
        for (m in SeichiAssist.materiallist) {
            //統計にないため除外
            if (m != Material.GRASS_PATH && m != Material.SOIL && m != Material.MOB_SPAWNER) {
                staticdata.add(player.getStatistic(Statistic.MINE_BLOCK, m))
            }
        }
        this.activeskilldata = ActiveSkillData()
        this.expbar = ExpBar(this, player)
        this.expmanager = ExperienceManager(player)
        this.p_givenvote = 0
        this.votecooldownflag = true
        this.gachacooldownflag = true
        this.shareinvcooldownflag = true
        this.chestflag = true

        this.displayTypeLv = true
        this.displayTitle1No = 0
        this.displayTitle2No = 0
        this.displayTitle3No = 0
        this.TitleFlags = BitSet(10000)
        this.TitleFlags.set(1)
        this.p_vote_forT = 0
        this.giveachvNo = 0
        this.titlepage = 1
        this.LimitedLoginCount = 0

        this.starlevel = 0
        this.starlevel_Break = 0
        this.starlevel_Time = 0
        this.starlevel_Event = 0

        this.build_lv = 1
        this.build_count = BigDecimal.ZERO
        this.build_count_flg = 0
        this.anniversary = false

        this.halfBreakFlag = false

        this.aheadUnit = 0
        this.behindUnit = 0
        this.rightUnit = 0
        this.leftUnit = 0
        this.canCreateRegion = true
        this.unitPerClick = 1
        this.templateMap = HashMap()
        this.usingVotingFairy = false
        this.hasVotingFairyMana = 0
        this.VotingFairyRecoveryValue = 0
        this.toggleGiveApple = 1
        this.VotingFairyStartTime = null
        this.VotingFairyEndTime = null
        this.toggleVotingFairy = 1
        this.p_apple = 0
        this.toggleVFSound = true

        this.added_mana = 0
        this.contribute_point = 0

        this.hasNewYearSobaGive = false
        this.newYearBagAmount = 0

        this.hasChocoGave = false

        this.hisotryData = MineStackHistoryData()
        this.isSearching = false
        this.indexMap = HashMap()

        this.ChainVote = 0

        this.selectHomeNum = 0
        this.setHomeNameNum = 0
        this.isSubHomeNameChange = false

        this.GBstage = 0
        this.GBlevel = 0
        this.GBexp = 0
        this.isGBStageUp = false
        this.GBcd = 0
    }

    //join時とonenable時、プレイヤーデータを最新の状態に更新
    fun updateonJoin(player: Player) {
        //破壊量データ(before)を設定
        //minuteblock.before = totalbreaknum;
        halfhourblock.before = totalbreaknum
        updateLevel(player)
        NotifySorryForBug(player)
        activeskilldata.updateonJoin(player, level)
        //サーバー保管経験値をクライアントに読み込み
        loadTotalExp()
        isVotingFairy(player)
    }


    //quit時とondisable時、プレイヤーデータを最新の状態に更新
    fun updateonQuit(player: Player) {
        //総整地量を更新
        calcMineBlock(player)
        //総プレイ時間更新
        calcPlayTick(player)

        activeskilldata.updateonQuit(player)
        expbar.remove()
        //クライアント経験値をサーバー保管
        saveTotalExp()
    }

    //詫びガチャの通知
    fun NotifySorryForBug(player: Player) {
        if (numofsorryforbug > 0) {
            player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
            player.sendMessage(ChatColor.GREEN.toString() + "運営チームから" + numofsorryforbug + "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "が届いています！\n木の棒メニューから受け取ってください")
        }
    }

    //エフェクトデータのdurationを60秒引く
    fun calcEffectData() {
        //tmplistを作成
        val tmplist = ArrayList<EffectData>()
        //effectdatalistのdurationをすべて60秒（1200tick）引いてtmplistに格納
        for (ed in effectdatalist) {
            ed.duration -= 1200
            tmplist.add(ed)
        }
        //tmplistのdurationが3秒以下（60tick）のものはeffectdatalistから削除
        for (ed in tmplist) {
            if (ed.duration <= 60) {
                effectdatalist.remove(ed)
            }
        }
    }


    //レベルを更新
    fun updateLevel(p: Player) {
        calcPlayerLevel(p)
        calcStarLevel(p)
        setDisplayName(p)
        expbar.calculate()
    }


    //プレイヤーのレベルからレベルと総整地量を指定された値に設定
    /**
     * @param _level
     * レベル
     *
     * ※レベルと総整地量を変更します(取扱注意)
     */
    fun setLevelandTotalbreaknum(_level: Int) {
        level = _level
        totalbreaknum = SeichiAssist.levellist[_level - 1].toLong()
    }


    //表示される名前に整地レベルor二つ名を追加
    fun setDisplayName(p: Player) {
        var displayname = Util.getName(p)

        //表示を追加する処理
        if (displayTitle1No == 0 && displayTitle2No == 0 && displayTitle3No == 0) {
            if (starlevel <= 0) {
                displayname = "[ Lv" + level + " ]" + displayname + ChatColor.WHITE
            } else {
                displayname = "[Lv" + level + "☆" + starlevel + "]" + displayname + ChatColor.WHITE
            }
        } else {
            val displayTitle1 = SeichiAssist.config.getTitle1(displayTitle1No)
            val displayTitle2 = SeichiAssist.config.getTitle2(displayTitle2No)
            val displayTitle3 = SeichiAssist.config.getTitle3(displayTitle3No)
            displayname = "[" + displayTitle1 + displayTitle2 + displayTitle3 + "]" + displayname + ChatColor.WHITE
        }
        //放置時に色を変える
        if (idletime >= 10) {
            displayname = ChatColor.DARK_GRAY.toString() + displayname
        } else if (idletime >= 3) {
            displayname = ChatColor.GRAY.toString() + displayname
        }

        p.displayName = displayname
        p.playerListName = displayname
    }


    //プレイヤーレベルを計算し、更新する。
    private fun calcPlayerLevel(p: Player) {
        //現在のランクを取得
        var i = level
        //既にレベル上限に達していたら終了
        if (i >= SeichiAssist.levellist.size) {
            return
        }
        //ランクが上がらなくなるまで処理
        while (SeichiAssist.levellist[i] <= totalbreaknum && i + 1 <= SeichiAssist.levellist.size) {

            //レベルアップ時のメッセージ
            p.sendMessage(ChatColor.GOLD.toString() + "ﾑﾑｯwwwwwwwﾚﾍﾞﾙｱｯﾌﾟwwwwwww【Lv(" + i + ")→Lv(" + (i + 1) + ")】")
            //レベルアップイベント着火
            Bukkit.getPluginManager().callEvent(SeichiLevelUpEvent(p, this, i + 1))
            //レベルアップ時の花火の打ち上げ
            val loc = p.location
            Util.launchFireWorks(loc)
            val lvmessage = SeichiAssist.config.getLvMessage(i + 1)
            if (!lvmessage.isEmpty()) {
                p.sendMessage(ChatColor.AQUA.toString() + lvmessage)
            }
            i++
            if (activeskilldata.mana.isLoaded) {
                //マナ最大値の更新
                activeskilldata.mana.onLevelUp(p, i)
            }
            //レベル上限に達したら終了
            if (i >= SeichiAssist.levellist.size) {
                break
            }
        }
        level = i
    }

    //スターレベルの計算、更新
    fun calcStarLevel(p: Player) {
        //処理前の各レベルを取得
        var i = starlevel
        val iB = starlevel_Break
        val iT = starlevel_Time
        val iE = starlevel_Event
        //処理後のレベルを保存する入れ物
        val i2: Int
        val iB2 = (totalbreaknum / 87115000).toInt()
        val iT2 = 0
        val iE2 = 0

        //整地量の確認
        if (iB < iB2) {
            p.sendMessage(ChatColor.GOLD.toString() + "ｽﾀｰﾚﾍﾞﾙ(整地量)がﾚﾍﾞﾙｱｯﾌﾟ!!【☆(" + iB + ")→☆(" + iB2 + ")】")
            starlevel_Break = iB2
        }

        //参加時間の確認(19/4/3撤廃)
        if (iT > 0) {
            starlevel -= iT
            i = starlevel
            starlevel_Time = 0
        }

        //イベント入手分の確認

        //今後実装予定。


        //合計値の確認
        i2 = iB2 + iT2 + iE2
        if (i < i2) {
            p.sendMessage(ChatColor.GOLD.toString() + "★☆★ｽﾀｰﾚﾍﾞﾙUP!!!★☆★【☆(" + i + ")→☆(" + i2 + ")】")
            starlevel = i2
        }
    }

    //総プレイ時間を更新する
    fun calcPlayTick(p: Player) {
        val getservertick = p.getStatistic(org.bukkit.Statistic.PLAY_ONE_TICK)
        //前回との差分を算出
        val getincrease = getservertick - servertick
        servertick = getservertick
        //総プレイ時間に追加
        playtick += getincrease
    }

    //総破壊ブロック数を更新する
    fun calcMineBlock(p: Player): Int {
        var i = 0
        var sum = 0.0
        for (m in SeichiAssist.materiallist) {
            if (m != Material.GRASS_PATH && m != Material.SOIL && m != Material.MOB_SPAWNER) {
                val getstat = p.getStatistic(Statistic.MINE_BLOCK, m)
                val getincrease = getstat - staticdata[i]
                sum += calcBlockExp(m, getincrease, p)
                if (SeichiAssist.DEBUG) {
                    if (calcBlockExp(m, getincrease, p) > 0.0) {
                        p.sendMessage("calcの値:" + calcBlockExp(m, getincrease, p) + "(" + m + ")")
                    }
                }
                staticdata[i] = getstat
                i++
            }
        }
        //double値を四捨五入し、整地量に追加する整数xを出す
        val x = (if (sum < 0.0) sum - 0.5 else sum + 0.5).toInt()

        //xを整地量に追加
        totalbreaknum += x.toLong()
        return x
    }

    //ブロック別整地数反映量の調節
    private fun calcBlockExp(m: Material, i: Int, p: Player): Double {
        var result = i.toDouble()
        //ブロック別重み分け
        when (m) {
            Material.DIRT ->
                //DIRTとGRASSは二重カウントされているので半分に
                result *= 0.5
            Material.GRASS ->
                //DIRTとGRASSは二重カウントされているので半分に
                result *= 0.5

            Material.NETHERRACK ->
                //ネザーラックの重み分け
                result *= 1.0

            Material.ENDER_STONE ->
                //エンドストーンの重み分け
                result *= 1.0

            //氷塊とマグマブロックの整地量を2倍
            Material.PACKED_ICE -> result *= 2.0

            Material.MAGMA -> result *= 2.0


            else -> {
            }
        }

        if (!Util.isSeichiWorld(p)) {
            //整地ワールド外では整地数が反映されない
            result *= 0.0
        } else {
            val worldName = p.world.name
            val sw_mining_coefficient = 0.8
            if (worldName.equals(Worlds.WORLD_SW.alphabetName, ignoreCase = true)) {
                result *= sw_mining_coefficient
            }
        }
        return result
    }

    //現在の採掘量順位を表示する
    fun calcPlayerRank(p: Player): Int {
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

    fun calcPlayerApple(p: Player): Int {
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
    fun dispPassiveExp(): Double {
        return if (level < 8) {
            0.0
        } else if (level < 18) {
            SeichiAssist.config.getDropExplevel(1)
        } else if (level < 28) {
            SeichiAssist.config.getDropExplevel(2)
        } else if (level < 38) {
            SeichiAssist.config.getDropExplevel(3)
        } else if (level < 48) {
            SeichiAssist.config.getDropExplevel(4)
        } else if (level < 58) {
            SeichiAssist.config.getDropExplevel(5)
        } else if (level < 68) {
            SeichiAssist.config.getDropExplevel(6)
        } else if (level < 78) {
            SeichiAssist.config.getDropExplevel(7)
        } else if (level < 88) {
            SeichiAssist.config.getDropExplevel(8)
        } else if (level < 98) {
            SeichiAssist.config.getDropExplevel(9)
        } else {
            SeichiAssist.config.getDropExplevel(10)
        }
    }

    //サブホームの位置をセットする
    fun setSubHomeLocation(location: Location, subHomeIndex: Int) {
        if ((subHomeIndex >= 0) and (subHomeIndex < SeichiAssist.config.subHomeMax)) {
            val currentSubHome = this.subHomeMap[subHomeIndex]
            val currentSubHomeName = currentSubHome?.name

            this.subHomeMap[subHomeIndex] = SubHome(location, currentSubHomeName)
        }
    }

    fun setSubHomeName(name: String?, subHomeIndex: Int) {
        if ((subHomeIndex >= 0) and (subHomeIndex < SeichiAssist.config.subHomeMax)) {
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

    fun build_count_flg_set(x: Byte) {
        build_count_flg = x
    }

    fun build_count_flg_get(): Byte {
        return build_count_flg
    }

    fun build_lv_set(lv: Int) {
        build_lv = lv
    }

    fun build_lv_get(): Int {
        return build_lv
    }

    fun build_count_set(count: BigDecimal) {
        build_count = count
    }

    fun build_count_get(): BigDecimal? {
        return build_count
    }

    private fun saveTotalExp() {
        totalexp = expmanager.currentExp
    }

    private fun loadTotalExp() {
        val server_num = SeichiAssist.config.serverNum
        //経験値が統合されてない場合は統合する
        if (expmarge.toInt() != 0x07 && server_num >= 1 && server_num <= 3) {
            if (expmarge and (0x01 shl server_num - 1).toByte() == 0.toByte()) {
                if (expmarge.toInt() == 0) {
                    // 初回は加算じゃなくベースとして代入にする
                    totalexp = expmanager.currentExp
                } else {
                    totalexp += expmanager.currentExp
                }
                expmarge = expmarge or (0x01 shl server_num - 1).toByte()
            }
        }
        expmanager.setExp(totalexp)
    }

    fun canBreakHalfBlock(): Boolean {
        return this.halfBreakFlag
    }

    fun canGridExtend(directionType: DirectionType, world: String): Boolean {
        val LIMIT = config.getGridLimitPerWorld(world)
        val chunkMap = unitMap

        //チャンクを拡大すると仮定する
        val assumedAmoont = chunkMap[directionType]!! + this.unitPerClick

        //一応すべての拡張値を出しておく
        val ahead = chunkMap[DirectionType.AHEAD]!!
        val behind = chunkMap[DirectionType.BEHIND]!!
        val right = chunkMap[DirectionType.RIGHT]!!
        val left = chunkMap[DirectionType.LEFT]!!

        //合計チャンク再計算値
        val assumedUnitAmount = when (directionType) {
            DirectionType.AHEAD -> (assumedAmoont + 1 + behind) * (right + 1 + left)
            DirectionType.BEHIND -> (ahead + 1 + assumedAmoont) * (right + 1 + left)
            DirectionType.RIGHT -> (ahead + 1 + behind) * (assumedAmoont + 1 + left)
            DirectionType.LEFT -> (ahead + 1 + behind) * (right + 1 + assumedAmoont)
        }

        return assumedUnitAmount <= LIMIT

    }

    fun canGridReduce(directionType: DirectionType): Boolean {
        val chunkMap = unitMap

        //減らしたと仮定する
        val assumedAmount = chunkMap[directionType]!! - unitPerClick
        return assumedAmount >= 0
    }

    fun setUnitAmount(directionType: DirectionType, amount: Int) {
        when (directionType) {
            DirectionType.AHEAD -> this.aheadUnit = amount
            DirectionType.BEHIND -> this.behindUnit = amount
            DirectionType.RIGHT -> this.rightUnit = amount
            DirectionType.LEFT -> this.leftUnit = amount
        }//わざと何もしない
    }

    fun addUnitAmount(directionType: DirectionType, addAmount: Int) {
        when (directionType) {
            DirectionType.AHEAD -> this.aheadUnit += addAmount
            DirectionType.BEHIND -> this.behindUnit += addAmount
            DirectionType.RIGHT -> this.rightUnit += addAmount
            DirectionType.LEFT -> this.leftUnit += addAmount
        }//わざと何もしない
    }

    fun setCanCreateRegion(flag: Boolean) {
        this.canCreateRegion = flag
    }

    fun canCreateRegion(): Boolean {
        return this.canCreateRegion
    }

    fun toggleUnitPerGrid() {
        if (this.unitPerClick == 1) {
            this.unitPerClick = 10
        } else if (this.unitPerClick == 10) {
            this.unitPerClick = 100
        } else if (this.unitPerClick == 100) {
            this.unitPerClick = 1
        }
    }

    fun VotingFairyTimeToString(): String {
        val cal = this.VotingFairyStartTime
        var s = ""
        if (this.VotingFairyStartTime == null) {
            //設定されてない場合
            s += ",,,,,"
        } else {
            //設定されてる場合
            val date = cal!!.time
            val format = SimpleDateFormat("yyyy,MM,dd,HH,mm,")
            s += format.format(date)
        }
        return s
    }

    fun SetVotingFairyTime(str: String, p: Player) {
        val s = str.split(",".toRegex()).toTypedArray()
        if (s[0].length > 0 && s[1].length > 0 && s[2].length > 0 && s[3].length > 0 && s[4].length > 0) {
            val startTime = GregorianCalendar(Integer.parseInt(s[0]), Integer.parseInt(s[1]) - 1, Integer.parseInt(s[2]), Integer.parseInt(s[3]), Integer.parseInt(s[4]))

            var min = Integer.parseInt(s[4]) + 1
            var hour = Integer.parseInt(s[3])

            min = if (this.toggleVotingFairy % 2 != 0) min + 30 else min
            hour = if (this.toggleVotingFairy == 2 or 3)
                hour + 1
            else if (this.toggleVotingFairy == 4)
                hour + 2
            else
                hour

            val EndTime = GregorianCalendar(Integer.parseInt(s[0]), Integer.parseInt(s[1]) - 1, Integer.parseInt(s[2]), hour, min)

            this.VotingFairyStartTime = startTime
            this.VotingFairyEndTime = EndTime
        }
    }

    fun isVotingFairy(p: Player) {
        //効果は継続しているか
        if (this.usingVotingFairy && Util.isVotingFairyPeriod(this.VotingFairyStartTime, this.VotingFairyEndTime) == false) {
            this.usingVotingFairy = false
            p.sendMessage(ChatColor.LIGHT_PURPLE.toString() + "" + ChatColor.BOLD + "妖精は何処かへ行ってしまったようだ...")
        } else if (this.usingVotingFairy) {
            VotingFairyTask.speak(p, "おかえり！" + p.name, true)
        }
    }

    fun isContribute(p: Player, addMana: Int) {
        val mana = Mana()

        //負数(入力ミスによるやり直し中プレイヤーがオンラインだった場合)の時
        if (addMana < 0) {
            p.sendMessage(ChatColor.GREEN.toString() + "" + ChatColor.BOLD + "入力者のミスによって得た不正なマナを" + -10 * addMana + "分減少させました.")
            p.sendMessage(ChatColor.GREEN.toString() + "" + ChatColor.BOLD + "申し訳ございません.")
        } else {
            p.sendMessage(ChatColor.GREEN.toString() + "" + ChatColor.BOLD + "運営からあなたの整地鯖への貢献報酬として")
            p.sendMessage(ChatColor.GREEN.toString() + "" + ChatColor.BOLD + "マナの上限値が" + 10 * addMana + "上昇しました．(永久)")
        }
        this.added_mana += addMana

        mana.calcAndSetMax(p, this.level)
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun toggleEffect(): MessageToSender {
        effectflag = (effectflag + 1) % 6

        val responseMessage = when (effectflag) {
            0 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(無制限)"
            1 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(127制限)"
            2 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(200制限)"
            3 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(400制限)"
            4 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(600制限)"
            else -> "${ChatColor.GREEN}採掘速度上昇効果:OFF"
        }

        return responseMessage.asResponseToSender()
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun toggleMessageFlag(): MessageToSender {
        messageflag = !messageflag

        val responseMessage = if (messageflag) {
            "${ChatColor.GREEN}内訳表示:ON(OFFに戻したい時は再度コマンドを実行します。)"
        } else {
            "${ChatColor.GREEN}内訳表示:OFF"
        }

        return responseMessage.asResponseToSender()
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun toggleHalfBreakFlag(): MessageToSender {
        halfBreakFlag = !halfBreakFlag

        val newStatus = if (halfBreakFlag) "${ChatColor.GREEN}破壊可能" else "${ChatColor.RED}破壊不可能"
        val responseMessage = "現在ハーフブロックは$newStatus${ChatColor.RESET}です."

        return responseMessage.asResponseToSender()
    }

    /**
     * 運営権限により強制的に実績を解除することを試みる。
     * 解除に成功し、このインスタンスが指す[Player]がオンラインであるならばその[Player]に解除の旨がチャットにて通知される。
     *
     * @param number 解除対象の実績番号
     * @return この作用の実行者に向け操作の結果を記述する[MessageToSender]
     */
    @Suppress("RedundantSuspendModifier")
    suspend fun tryForcefullyUnlockAchievement(number: Int): MessageToSender =
        if (!TitleFlags.get(number)) {
            TitleFlags.set(number)
            Bukkit.getPlayer(uuid)?.sendMessage("運営チームよりNo${number}の実績が配布されました。")

            "$name に実績No. $number を${ChatColor.GREEN}付与${ChatColor.RESET}しました。".asResponseToSender()
        } else {
            "${ChatColor.GRAY}$name は既に実績No. $number を獲得しています。".asResponseToSender()
        }

    /**
     * 運営権限により強制的に実績を剥奪することを試みる。
     * 実績剥奪の通知はプレーヤーには行われない。
     *
     * @param number 解除対象の実績番号
     * @return この作用の実行者に向け操作の結果を記述する[MessageToSender]
     */
    @Suppress("RedundantSuspendModifier")
    suspend fun forcefullyDepriveAchievement(number: Int): MessageToSender =
        if (!TitleFlags.get(number)) {
            TitleFlags.set(number, false)

            "$name から実績No. $number を${ChatColor.RED}剥奪${ChatColor.GREEN}しました。".asResponseToSender()
        } else {
            "${ChatColor.GRAY}$name は実績No. $number を獲得していません。".asResponseToSender()
        }

    companion object {
        internal var config = SeichiAssist.config
    }
}

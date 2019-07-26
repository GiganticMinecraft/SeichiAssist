package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.ActiveSkillEffect
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect
import com.github.unchama.seichiassist.MineStackObjectList
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.GridTemplate
import com.github.unchama.seichiassist.data.LimitedLoginEvent
import com.github.unchama.seichiassist.data.MineStack
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.data.playerdata.GiganticBerserk
import com.github.unchama.seichiassist.data.playerdata.PlayerNickName
import com.github.unchama.seichiassist.data.playerdata.StarLevel
import com.github.unchama.seichiassist.database.DatabaseConstants
import com.github.unchama.seichiassist.minestack.MineStackObj
import com.github.unchama.seichiassist.util.BukkitSerialization
import com.github.unchama.util.MillisecondTimer
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.io.IOException
import java.math.BigDecimal
import java.sql.SQLException
import java.sql.Statement
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

/**
 * プレイヤーデータロードを実施する処理(非同期で実行すること)
 * DBから読み込みたい値が増えた/減った場合は更新すること
 * @author unchama
 */
class PlayerDataLoadTask(internal var playerdata: PlayerData) : BukkitRunnable() {

  private val plugin = SeichiAssist.instance
  private val playermap = SeichiAssist.playermap
  private val databaseGateway = SeichiAssist.databaseGateway

  private val LLE = LimitedLoginEvent()

  private val p: Player
  internal val uuid: UUID
  private val stringUuid: String
  private var flag: Boolean = false
  private var i: Int = 0
  private val db: String
  private val timer: MillisecondTimer

  init {
    timer = MillisecondTimer.getInitializedTimerInstance()
    db = SeichiAssist.seichiAssistConfig.db
    p = Bukkit.getPlayer(playerdata.uuid)
    uuid = playerdata.uuid
    stringUuid = uuid.toString().toLowerCase()
    flag = true
    i = 0
  }

  @Throws(SQLException::class)
  private fun updateLoginInfo(stmt: Statement) {
    val loginInfoUpdateCommand = ("update "
        + db + "." + DatabaseConstants.PLAYERDATA_TABLENAME + " "
        + "set loginflag = true, "
        + "lastquit = cast(now() as datetime) "
        + "where uuid like '" + stringUuid + "'")

    stmt.executeUpdate(loginInfoUpdateCommand)
  }

  @Throws(SQLException::class)
  private fun loadSubHomeData(stmt: Statement) {
    val subHomeDataQuery = ("select * from "
        + db + "." + DatabaseConstants.SUB_HOME_TABLENAME + " where "
        + "player_uuid like '" + stringUuid + "' and "
        + "server_id = " + config.serverNum)

    stmt.executeQuery(subHomeDataQuery).use { resultSet ->
      while (resultSet.next()) {
        val subHomeId = resultSet.getInt("id")
        val subHomeName = resultSet.getString("name")
        val locationX = resultSet.getInt("location_x")
        val locationY = resultSet.getInt("location_y")
        val locationZ = resultSet.getInt("location_z")
        val worldName = resultSet.getString("world_name")

        val world = Bukkit.getWorld(worldName)
        val location = Location(world, locationX.toDouble(), locationY.toDouble(), locationZ.toDouble())

        playerdata.setSubHomeLocation(location, subHomeId)
        playerdata.setSubHomeName(subHomeName, subHomeId)
      }
    }
  }

  @Throws(SQLException::class)
  private fun loadMineStack(stmt: Statement) {
    val mineStackDataQuery = ("select * from "
        + db + "." + DatabaseConstants.MINESTACK_TABLENAME + " where "
        + "player_uuid like '" + stringUuid + "'")

    /* TODO これはここにあるべきではない
         * 格納可能なアイテムのリストはプラグインインスタンスの中に動的に持たれるべきで、
         * そのリストをラップするオブジェクトに同期された形でこのオブジェクトがもたれるべきであり、
         * ロードされるたびに再計算されるべきではない
         */
    val nameObjectMappings: Map<String, MineStackObj>
    run {
      val resultMap = HashMap<String, MineStackObj>()

      MineStackObjectList.minestacklist!!
          .forEach { `object` -> resultMap[`object`.mineStackObjName] = `object` }

      nameObjectMappings = resultMap
    }

    stmt.executeQuery(mineStackDataQuery).use { resultSet ->
      val objectAmounts = HashMap<MineStackObj, Long>()
      while (resultSet.next()) {
        val objectName = resultSet.getString("object_name")
        val objectAmount = resultSet.getLong("amount")
        val mineStackObj = nameObjectMappings[objectName]

        if (mineStackObj != null) {
          objectAmounts[mineStackObj] = objectAmount
        } else {
          val message = "プレーヤー " + p.name + " のMineStackオブジェクト " + objectName + " は収納可能リストに見つかりませんでした。"
          Bukkit.getLogger().warning(message)
        }
      }

      playerdata.minestack = MineStack(objectAmounts)
    }
  }

  @Throws(SQLException::class)
  private fun loadGridTemplate(stmt: Statement) {
    val gridTemplateDataQuery = ("select * from "
        + db + "." + DatabaseConstants.GRID_TEMPLATE_TABLENAME + " where "
        + "designer_uuid like '" + stringUuid + "'")

    stmt.executeQuery(gridTemplateDataQuery).use { resultSet ->
      val templateMap = HashMap<Int, GridTemplate>()

      while (resultSet.next()) {
        val templateId = resultSet.getInt("id")

        val aheadLength = resultSet.getInt("ahead_length")
        val behindLength = resultSet.getInt("behind_length")
        val rightLength = resultSet.getInt("right_length")
        val leftLength = resultSet.getInt("left_length")

        val template = GridTemplate(aheadLength, behindLength, rightLength, leftLength)

        templateMap[templateId] = template
      }

      playerdata.templateMap = templateMap
    }
  }

  @Throws(SQLException::class)
  private fun loadSkillEffectUnlockState(stmt: Statement) {
    val unlockedSkillEffectQuery = ("select * from "
        + db + "." + DatabaseConstants.SKILL_EFFECT_TABLENAME + " where "
        + "player_uuid like '" + stringUuid + "'")

    stmt.executeQuery(unlockedSkillEffectQuery).use { resultSet ->
      while (resultSet.next()) {
        val effectName = resultSet.getString("effect_name")

        val effect = ActiveSkillEffect.fromSqlName(effectName)
        playerdata.activeskilldata.obtainedSkillEffects.add(effect)
      }
    }
  }

  @Throws(SQLException::class)
  private fun loadSkillPremiumEffectUnlockState(stmt: Statement) {
    val unlockedSkillEffectQuery = ("select * from "
        + db + "." + DatabaseConstants.SKILL_PREMIUM_EFFECT_TABLENAME + " where "
        + "player_uuid like '" + stringUuid + "'")

    stmt.executeQuery(unlockedSkillEffectQuery).use { resultSet ->
      while (resultSet.next()) {
        val effectName = resultSet.getString("effect_name")

        val effect = ActiveSkillPremiumEffect.fromSqlName(effectName)
        playerdata.activeskilldata.obtainedSkillPremiumEffects.add(effect)
      }
    }
  }

  @Throws(SQLException::class, IOException::class)
  private fun loadPlayerData(stmt: Statement) {
    //playerdataをsqlデータから得られた値で更新
    val command = ("select * from " + db + "." + DatabaseConstants.PLAYERDATA_TABLENAME
        + " where uuid like '" + stringUuid + "'")

    stmt.executeQuery(command).use { rs ->
      while (rs.next()) {
        //各種数値
        playerdata.loaded = true
        runBlocking {
          playerdata.fastDiggingEffectSuppressor.setStateFromSerializedValue(rs.getInt("effectflag"))
        }
        playerdata.minestackflag = rs.getBoolean("minestackflag")
        playerdata.messageflag = rs.getBoolean("messageflag")
        playerdata.activeskilldata.mineflagnum = rs.getInt("activemineflagnum")
        playerdata.activeskilldata.assaultflag = rs.getBoolean("assaultflag")
        playerdata.activeskilldata.skilltype = rs.getInt("activeskilltype")
        playerdata.activeskilldata.skillnum = rs.getInt("activeskillnum")
        playerdata.activeskilldata.assaulttype = rs.getInt("assaultskilltype")
        playerdata.activeskilldata.assaultnum = rs.getInt("assaultskillnum")
        playerdata.activeskilldata.arrowskill = rs.getInt("arrowskill")
        playerdata.activeskilldata.multiskill = rs.getInt("multiskill")
        playerdata.activeskilldata.breakskill = rs.getInt("breakskill")
        playerdata.activeskilldata.fluidcondenskill = rs.getInt("fluidcondenskill")
        playerdata.activeskilldata.watercondenskill = rs.getInt("watercondenskill")
        playerdata.activeskilldata.lavacondenskill = rs.getInt("lavacondenskill")
        playerdata.activeskilldata.effectnum = rs.getInt("effectnum")
        playerdata.gachapoint = rs.getInt("gachapoint")
        playerdata.gachaflag = rs.getBoolean("gachaflag")
        playerdata.level = rs.getInt("level")
        playerdata.numofsorryforbug = rs.getInt("numofsorryforbug")
        playerdata.rgnum = rs.getInt("rgnum")
        playerdata.inventory = BukkitSerialization.fromBase64forPocket(rs.getString("inventory"))
        playerdata.dispkilllogflag = rs.getBoolean("killlogflag")
        playerdata.dispworldguardlogflag = rs.getBoolean("worldguardlogflag")

        playerdata.multipleidbreakflag = rs.getBoolean("multipleidbreakflag")

        playerdata.pvpflag = rs.getBoolean("pvpflag")
        playerdata.totalbreaknum = rs.getLong("totalbreaknum")
        playerdata.playtick = rs.getInt("playtick")
        playerdata.p_givenvote = rs.getInt("p_givenvote")
        playerdata.activeskilldata.effectpoint = rs.getInt("effectpoint")
        playerdata.activeskilldata.premiumeffectpoint = rs.getInt("premiumeffectpoint")
        //マナの情報
        playerdata.activeskilldata.mana.mana = rs.getDouble("mana")
        playerdata.expbar.isVisible = rs.getBoolean("expvisible")

        playerdata.totalexp = rs.getInt("totalexp")

        playerdata.expmarge = rs.getByte("expmarge")
        playerdata.contentsPresentInSharedInventory = "" != rs.getString("shareinv") && rs.getString("shareinv") != null
        playerdata.everysoundflag = rs.getBoolean("everysound")
        playerdata.everymessageflag = rs.getBoolean("everymessage")

        playerdata.selectHomeNum = 0
        playerdata.setHomeNameNum = 0
        playerdata.isSubHomeNameChange = false

        //実績、二つ名の情報
        playerdata.nickName = PlayerNickName(
            PlayerNickName.Style.marshal(rs.getBoolean("displayTypeLv")),
            rs.getInt("displayTitle1No"),
            rs.getInt("displayTitle2No"),
            rs.getInt("displayTitle3No")
        )
        playerdata.p_vote_forT = rs.getInt("p_vote")
        playerdata.giveachvNo = rs.getInt("giveachvNo")
        playerdata.achvPointMAX = rs.getInt("achvPointMAX")
        playerdata.achvPointUSE = rs.getInt("achvPointUSE")
        playerdata.achvChangenum = rs.getInt("achvChangenum")
        playerdata.achvPoint = playerdata.achvPointMAX + playerdata.achvChangenum * 3 - playerdata.achvPointUSE

        //スターレベルの情報
        playerdata.starLevels = StarLevel(
            rs.getInt("starlevel_Break"),
            rs.getInt("starlevel_Time"),
            rs.getInt("starlevel_Event")
        )

        //期間限定ログインイベント専用の累計ログイン日数
        playerdata.LimitedLoginCount = rs.getInt("LimitedLoginCount")

        //連続・通算ログインの情報、およびその更新
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy/MM/dd")
        if (rs.getString("lastcheckdate") == "" || rs.getString("lastcheckdate") == null) {
          playerdata.lastcheckdate = sdf.format(cal.time)
        } else {
          playerdata.lastcheckdate = rs.getString("lastcheckdate")
        }
        playerdata.ChainJoin = rs.getInt("ChainJoin")
        playerdata.TotalJoin = rs.getInt("TotalJoin")
        if (playerdata.ChainJoin == 0) {
          playerdata.ChainJoin = 1
        }
        if (playerdata.TotalJoin == 0) {
          playerdata.TotalJoin = 1
        }

        try {
          val TodayDate = sdf.parse(sdf.format(cal.time))
          val LastDate = sdf.parse(playerdata.lastcheckdate)
          val TodayLong = TodayDate.time
          val LastLong = LastDate.time

          val datediff = (TodayLong - LastLong) / (1000 * 60 * 60 * 24)
          if (datediff > 0) {
            LLE.getLastcheck(playerdata.lastcheckdate)
            playerdata.TotalJoin = playerdata.TotalJoin + 1
            if (datediff == 1L) {
              playerdata.ChainJoin = playerdata.ChainJoin + 1
            } else {
              playerdata.ChainJoin = 1
            }
          }
        } catch (e: ParseException) {
          e.printStackTrace()
        }

        playerdata.lastcheckdate = sdf.format(cal.time)

        //連続投票の更新
        val lastvote = rs.getString("lastvote")
        if ("" == lastvote || lastvote == null) {
          playerdata.ChainVote = 0
        } else {
          try {
            val TodayDate = sdf.parse(sdf.format(cal.time))
            val LastDate = sdf.parse(lastvote)
            val TodayLong = TodayDate.time
            val LastLong = LastDate.time

            val datediff = (TodayLong - LastLong) / (1000 * 60 * 60 * 24)
            if (datediff <= 1 || datediff >= 0) {
              playerdata.ChainVote = rs.getInt("chainvote")
            } else {
              playerdata.ChainVote = 0
            }
          } catch (e: ParseException) {
            e.printStackTrace()
          }

        }

        //実績解除フラグのBitSet型への復元処理
        //初回nullエラー回避のための分岐
        try {
          val Titlenums = rs.getString("TitleFlags").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
          val Titlearray = Arrays.stream(Titlenums).mapToLong { x -> java.lang.Long.parseUnsignedLong(x, 16) }.toArray()
          val TitleFlags = BitSet.valueOf(Titlearray)
          playerdata.TitleFlags = TitleFlags
        } catch (e: NullPointerException) {
          playerdata.TitleFlags = BitSet(10000)
          playerdata.TitleFlags.set(1)
        }

        //建築
        playerdata.build_lv_set(rs.getInt("build_lv"))
        playerdata.build_count_set(BigDecimal(rs.getString("build_count")))
        playerdata.build_count_flg_set(rs.getByte("build_count_flg"))

        //マナ妖精
        playerdata.usingVotingFairy = rs.getBoolean("canVotingFairyUse")
        playerdata.VotingFairyRecoveryValue = rs.getInt("VotingFairyRecoveryValue")
        playerdata.hasVotingFairyMana = rs.getInt("hasVotingFairyMana")
        playerdata.toggleGiveApple = rs.getInt("toggleGiveApple")
        playerdata.toggleVotingFairy = rs.getInt("toggleVotingFairy")
        playerdata.SetVotingFairyTime(rs.getString("newVotingFairyTime"), p)
        playerdata.p_apple = rs.getLong("p_apple")


        playerdata.contribute_point = rs.getInt("contribute_point")
        playerdata.added_mana = rs.getInt("added_mana")

        playerdata.giganticBerserk = GiganticBerserk(
            rs.getInt("GBlevel"),
            rs.getInt("GBexp"),
            rs.getInt("GBstage"),
            rs.getBoolean("isGBStageUp")
        )
        playerdata.anniversary = rs.getBoolean("anniversary")

        // 1周年記念
        if (playerdata.anniversary) {
          p.sendMessage("整地サーバー1周年を記念してアイテムを入手出来ます。詳細はwikiをご確認ください。http://seichi.click/wiki/anniversary")
          p.playSound(p.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
        }

        //正月イベント用
        playerdata.hasNewYearSobaGive = rs.getBoolean("hasNewYearSobaGive")
        playerdata.newYearBagAmount = rs.getInt("newYearBagAmount")

        //バレンタインイベント用
        playerdata.hasChocoGave = rs.getBoolean("hasChocoGave")
      }
    }
  }

  override fun run() {
    //対象プレイヤーがオフラインなら処理終了
    if (SeichiAssist.instance.server.getPlayer(uuid) == null) {
      plugin.server.consoleSender.sendMessage(ChatColor.RED.toString() + p.name + "はオフラインの為取得処理を中断")
      cancel()
      return
    }
    //sqlコネクションチェック
    databaseGateway.ensureConnection()

    val stmt: Statement
    //同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
    try {
      stmt = databaseGateway.con.createStatement()
    } catch (e1: SQLException) {
      e1.printStackTrace()
      cancel()
      return
    }

    //ログインフラグの確認を行う
    val table = SeichiAssist.PLAYERDATA_TABLENAME
    val loginFlagSelectionQuery = "select loginflag from " +
        db + "." + table + " " +
        "where uuid = '" + stringUuid + "'"
    try {
      stmt.executeQuery(loginFlagSelectionQuery).use { rs ->
        while (rs.next()) {
          flag = rs.getBoolean("loginflag")
        }
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      cancel()
      return
    }

    if (i >= 4 && flag) {
      //強制取得実行
      plugin.server.consoleSender.sendMessage(ChatColor.RED.toString() + p.name + "のplayerdata強制取得実行")
      cancel()
    } else if (!flag) {
      //flagが折れてたので普通に取得実行
      cancel()
    } else {
      //再試行
      plugin.server.consoleSender.sendMessage(ChatColor.YELLOW.toString() + p.name + "のloginflag=false待機…(" + (i + 1) + "回目)")
      i++
      return
    }

    try {
      loadPlayerData(stmt)
      updateLoginInfo(stmt)
      loadGridTemplate(stmt)
      loadMineStack(stmt)
      loadSkillEffectUnlockState(stmt)
      loadSkillPremiumEffectUnlockState(stmt)
      loadSubHomeData(stmt)
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()

      //コネクション復活後にnewインスタンスのデータで上書きされるのを防止する為削除しておく
      playermap.remove(uuid)

      return
    } catch (e: IOException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      playermap.remove(uuid)
      return
    }

    //念のためstatement閉じておく
    try {
      stmt.close()
    } catch (e: SQLException) {
      e.printStackTrace()
    }

    if (SeichiAssist.DEBUG) {
      p.sendMessage("sqlデータで更新しました")
    }
    //更新したplayerdataをplayermapに追加
    playermap[uuid] = playerdata

    //期間限定ログインイベント判別処理
    LLE.TryGetItem(p)

    //貢献度pt増加によるマナ増加があるかどうか
    if (playerdata.added_mana < playerdata.contribute_point) {
      val addMana: Int
      addMana = playerdata.contribute_point - playerdata.added_mana
      playerdata.isContribute(p, addMana)
    }
    timer.sendLapTimeMessage(ChatColor.GREEN.toString() + p.name + "のプレイヤーデータ読込完了")
  }

  companion object {
    private val config = SeichiAssist.seichiAssistConfig
  }
}

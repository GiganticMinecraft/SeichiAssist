package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.ActiveSkillEffect
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.GridTemplate
import com.github.unchama.seichiassist.data.LimitedLoginEvent
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.database.DatabaseConstants
import com.github.unchama.seichiassist.minestack.MineStackObj
import com.github.unchama.seichiassist.util.BukkitSerialization
import com.github.unchama.util.MillisecondTimer
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

import java.io.IOException
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

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

  private val p: Player = Bukkit.getPlayer(playerdata.uuid)
  internal val uuid: UUID = playerdata.uuid
  private val stringUuid: String
  private var flag: Boolean = false
  private var i: Int = 0
  private val db: String = SeichiAssist.config.db
  private val timer: MillisecondTimer = MillisecondTimer.getInitializedTimerInstance()

  init {
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

    stmt.executeQuery(subHomeDataQuery).recordIteration {
      val resultSet = this
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

      SeichiAssist
          .minestacklist
          .forEach { `object` -> resultMap[`object`.mineStackObjName] = `object` }

      nameObjectMappings = resultMap
    }

    stmt.executeQuery(mineStackDataQuery).recordIteration {
      val resultSet = this
      val objectName = resultSet.getString("object_name")
      val objectAmount = resultSet.getLong("amount")

      val mineStackObj = nameObjectMappings[objectName]

      playerdata.minestack.setStackedAmountOf(mineStackObj!!, objectAmount)
    }
  }

  @Throws(SQLException::class)
  private fun loadGridTemplate(stmt: Statement) {
    val gridTemplateDataQuery = ("select * from "
        + db + "." + DatabaseConstants.GRID_TEMPLATE_TABLENAME + " where "
        + "designer_uuid like '" + stringUuid + "'")

    stmt.executeQuery(gridTemplateDataQuery).recordIteration {
      val resultSet = this
      val templateId = resultSet.getInt("id")

      val aheadLength = resultSet.getInt("ahead_length")
      val behindLength = resultSet.getInt("behind_length")
      val rightLength = resultSet.getInt("right_length")
      val leftLength = resultSet.getInt("left_length")

      val template = GridTemplate(aheadLength, behindLength, rightLength, leftLength)

      playerdata.templateMap!![templateId] = template
    }
  }

  @Throws(SQLException::class)
  private fun loadSkillEffectUnlockState(stmt: Statement) {
    val unlockedSkillEffectQuery = ("select * from "
        + db + "." + DatabaseConstants.SKILL_EFFECT_TABLENAME + " where "
        + "player_uuid like '" + stringUuid + "'")

    stmt.executeQuery(unlockedSkillEffectQuery).recordIteration {
      val effectName = getString("effect_name")

      val effect = ActiveSkillEffect.fromSqlName(effectName)
      playerdata.activeskilldata.obtainedSkillEffects.add(effect)
    }
  }

  @Throws(SQLException::class)
  private fun loadSkillPremiumEffectUnlockState(stmt: Statement) {
    val unlockedSkillEffectQuery = ("select * from "
        + db + "." + DatabaseConstants.SKILL_PREMIUM_EFFECT_TABLENAME + " where "
        + "player_uuid like '" + stringUuid + "'")

    stmt.executeQuery(unlockedSkillEffectQuery).recordIteration {
        val effectName = getString("effect_name")

        val effect = ActiveSkillPremiumEffect.fromSqlName(effectName)
        playerdata.activeskilldata.obtainedSkillPremiumEffects.add(effect)
    }
  }

  @Throws(SQLException::class, IOException::class)
  private fun loadPlayerData(stmt: Statement) {
    //playerdataをsqlデータから得られた値で更新
    val command = ("select * from " + db + "." + DatabaseConstants.PLAYERDATA_TABLENAME
        + " where uuid like '" + stringUuid + "'")

    stmt.executeQuery(command).recordIteration {
      val rs = this
      //各種数値
      with (playerdata) {
        loaded = true
        effectflag = rs.getInt("effectflag")
        minestackflag = rs.getBoolean("minestackflag")
        messageflag = rs.getBoolean("messageflag")
        with (activeskilldata) {
          mineflagnum = rs.getInt("activemineflagnum")
          assaultflag = rs.getBoolean("assaultflag")
          skilltype = rs.getInt("activeskilltype")
          skillnum = rs.getInt("activeskillnum")
          assaulttype = rs.getInt("assaultskilltype")
          assaultnum = rs.getInt("assaultskillnum")
          arrowskill = rs.getInt("arrowskill")
          multiskill = rs.getInt("multiskill")
          breakskill = rs.getInt("breakskill")
          fluidcondenskill = rs.getInt("fluidcondenskill")
          watercondenskill = rs.getInt("watercondenskill")
          lavacondenskill = rs.getInt("lavacondenskill")
          effectnum = rs.getInt("effectnum")
          effectpoint = rs.getInt("effectpoint")
          premiumeffectpoint = rs.getInt("premiumeffectpoint")
          //マナの情報
          mana.mana = rs.getDouble("mana")
        }

        gachapoint = rs.getInt("gachapoint")
        gachaflag = rs.getBoolean("gachaflag")
        level = rs.getInt("level")
        numofsorryforbug = rs.getInt("numofsorryforbug")
        rgnum = rs.getInt("rgnum")
        inventory = BukkitSerialization.fromBase64forPocket(rs.getString("inventory"))
        dispkilllogflag = rs.getBoolean("killlogflag")
        dispworldguardlogflag = rs.getBoolean("worldguardlogflag")

        multipleidbreakflag = rs.getBoolean("multipleidbreakflag")

        pvpflag = rs.getBoolean("pvpflag")
        totalbreaknum = rs.getLong("totalbreaknum")
        playtick = rs.getInt("playtick")
        p_givenvote = rs.getInt("p_givenvote")
        expbar.isVisible = rs.getBoolean("expvisible")

        totalexp = rs.getInt("totalexp")

        expmarge = rs.getByte("expmarge")
        contentsPresentInSharedInventory = "" != rs.getString("shareinv") && rs.getString("shareinv") != null
        everysoundflag = rs.getBoolean("everysound")
        everymessageflag = rs.getBoolean("everymessage")

        selectHomeNum = 0
        setHomeNameNum = 0
        isSubHomeNameChange = false

        //実績、二つ名の情報
        displayTypeLv = rs.getBoolean("displayTypeLv")
        displayTitle1No = rs.getInt("displayTitle1No")
        displayTitle2No = rs.getInt("displayTitle2No")
        displayTitle3No = rs.getInt("displayTitle3No")
        p_vote_forT = rs.getInt("p_vote")
        giveachvNo = rs.getInt("giveachvNo")
        achvPointMAX = rs.getInt("achvPointMAX")
        achvPointUSE = rs.getInt("achvPointUSE")
        achvChangenum = rs.getInt("achvChangenum")
        achvPoint = playerdata.achvPointMAX + playerdata.achvChangenum * 3 - playerdata.achvPointUSE

        //スターレベルの情報
        starlevel = rs.getInt("starlevel")
        starlevel_Break = rs.getInt("starlevel_Break")
        starlevel_Time = rs.getInt("starlevel_Time")
        starlevel_Event = rs.getInt("starlevel_Event")

        //期間限定ログインイベント専用の累計ログイン日数
        LimitedLoginCount = rs.getInt("LimitedLoginCount")
      }


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
        val todayDate = sdf.parse(sdf.format(cal.time))
        val lastDate = sdf.parse(playerdata.lastcheckdate)
        val todayLong = todayDate.time
        val lastLong = lastDate.time

        val datediff = (todayLong - lastLong) / (1000 * 60 * 60 * 24)
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
          val todayDate = sdf.parse(sdf.format(cal.time))
          val lastDate = sdf.parse(lastvote)
          val todayLong = todayDate.time
          val lastLong = lastDate.time

          val datediff = (todayLong - lastLong) / (1000 * 60 * 60 * 24)
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

      with (playerdata) {
        //建築
        build_lv_set(rs.getInt("build_lv"))
        build_count_set(BigDecimal(rs.getString("build_count")))
        build_count_flg_set(rs.getByte("build_count_flg"))

        //マナ妖精
        usingVotingFairy = rs.getBoolean("canVotingFairyUse")
        VotingFairyRecoveryValue = rs.getInt("VotingFairyRecoveryValue")
        hasVotingFairyMana = rs.getInt("hasVotingFairyMana")
        toggleGiveApple = rs.getInt("toggleGiveApple")
        toggleVotingFairy = rs.getInt("toggleVotingFairy")
        SetVotingFairyTime(rs.getString("newVotingFairyTime"), p)
        p_apple = rs.getLong("p_apple")


        contribute_point = rs.getInt("contribute_point")
        added_mana = rs.getInt("added_mana")

        GBstage = rs.getInt("GBstage")
        GBexp = rs.getInt("GBexp")
        GBlevel = rs.getInt("GBlevel")
        isGBStageUp = rs.getBoolean("isGBStageUp")
        anniversary = rs.getBoolean("anniversary")

        // 1周年記念
        if (playerdata.anniversary) {
          p.sendMessage("整地サーバー1周年を記念してアイテムを入手出来ます。詳細はwikiをご確認ください。http://seichi.click/wiki/anniversary")
          p.playSound(p.location, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
        }

        //正月イベント用
        hasNewYearSobaGive = rs.getBoolean("hasNewYearSobaGive")
        newYearBagAmount = rs.getInt("newYearBagAmount")

        //バレンタインイベント用
        hasChocoGave = rs.getBoolean("hasChocoGave")
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
      stmt.executeQuery(loginFlagSelectionQuery).recordIteration {
        flag = getBoolean("loginflag")
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
      val addMana: Int = playerdata.contribute_point - playerdata.added_mana
      playerdata.isContribute(p, addMana)
    }
    timer.sendLapTimeMessage(ChatColor.GREEN.toString() + p.name + "のプレイヤーデータ読込完了")
  }

  companion object {
    private val config = SeichiAssist.config
  }
}

inline fun ResultSet.recordIteration(operation: ResultSet.() -> Unit) {
  use {
    while (next()) {
      operation()
    }
  }
}

package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.ActiveSkillEffect
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect
import com.github.unchama.seichiassist.MineStackObjectList
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.GridTemplate
import com.github.unchama.seichiassist.data.LimitedLoginEvent
import com.github.unchama.seichiassist.data.MineStack
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.data.playerdata.AchievePoint
import com.github.unchama.seichiassist.data.playerdata.BuildCount
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
import org.jetbrains.annotations.NotNull
import java.io.IOException
import java.math.BigDecimal
import java.sql.ResultSet
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

    stmt.executeQuery(subHomeDataQuery).recordIteration {
      val subHomeId = getInt("id")
      val subHomeName = getString("name")
      val locationX = getInt("location_x")
      val locationY = getInt("location_y")
      val locationZ = getInt("location_z")
      val worldName = getString("world_name")

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

    /**
     * TODO これはここにあるべきではない
     * 格納可能なアイテムのリストはプラグインインスタンスの中に動的に持たれるべきで、
     * そのリストをラップするオブジェクトに同期された形でこのオブジェクトがもたれるべきであり、
     * ロードされるたびに再計算されるべきではない
     */
    val nameObjectMappings: Map<String, MineStackObj> =
        MineStackObjectList.minestacklist!!
            .map { it.mineStackObjName to it }
            .toMap()

    val objectAmounts = HashMap<MineStackObj, Long>()

    stmt.executeQuery(mineStackDataQuery).recordIteration {
      val objectName = getString("object_name")
      val objectAmount = getLong("amount")
      val mineStackObj = nameObjectMappings[objectName]

      if (mineStackObj != null) {
        objectAmounts[mineStackObj] = objectAmount
      } else {
        Bukkit
            .getLogger()
            .warning("プレーヤー ${p.name} のMineStackオブジェクト $objectName は収納可能リストに見つかりませんでした。")
      }
    }

    playerdata.minestack = MineStack(objectAmounts)
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

    stmt.executeQuery(command).recordIteration {
      val rs = this
      //各種数値
      playerdata.loaded = true
      runBlocking {
        playerdata.fastDiggingEffectSuppressor.setStateFromSerializedValue(rs.getInt("effectflag"))
      }
      playerdata.minestackflag = rs.getBoolean("minestackflag")
      playerdata.messageflag = rs.getBoolean("messageflag")
      playerdata.activeskilldata.apply {
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
      }

      playerdata.gachapoint = rs.getInt("gachapoint")
      playerdata.tookGachaTicket = rs.getBoolean("gachaflag")
      playerdata.level = rs.getInt("level")
      playerdata.wabiGacha = rs.getInt("numofsorryforbug")
      playerdata.regionCount = rs.getInt("rgnum")
      playerdata.inventory = BukkitSerialization.fromBase64forPocket(rs.getString("inventory"))
      playerdata.dispkilllogflag = rs.getBoolean("killlogflag")
      playerdata.dispworldguardlogflag = rs.getBoolean("worldguardlogflag")

      playerdata.multipleidbreakflag = rs.getBoolean("multipleidbreakflag")

      playerdata.pvpflag = rs.getBoolean("pvpflag")
      playerdata.totalbreaknum = rs.getLong("totalbreaknum")
      playerdata.playTick = rs.getInt("playtick")
      playerdata.p_givenvote = rs.getInt("p_givenvote")
      playerdata.activeskilldata.effectpoint = rs.getInt("effectpoint")
      playerdata.activeskilldata.premiumeffectpoint = rs.getInt("premiumeffectpoint")
      //マナの情報
      playerdata.activeskilldata.mana.mana = rs.getDouble("mana")
      playerdata.expbar.isVisible = rs.getBoolean("expvisible")

      playerdata.totalexp = rs.getInt("totalexp")

      playerdata.expmarge = rs.getByte("expmarge")
      playerdata.contentsPresentInSharedInventory = !rs.getString("shareinv").isNullOrEmpty()
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
      playerdata.achievePoint = AchievePoint(
          rs.getInt("achvPointMAX"),
          rs.getInt("achvPointUSE"),
          rs.getInt("achvChangenum")
      )

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
      val lastIn = rs.getString("lastcheckdate")
      playerdata.lastcheckdate = if (lastIn.isNullOrEmpty()) {
        sdf.format(cal.time)
      } else {
        lastIn
      }
      val chain = rs.getInt("ChainJoin")
      playerdata.loginStatus = playerdata.loginStatus.copy(chainLoginDay = if (chain == 0) {
  1
} else {
  chain
})
      val total = rs.getInt("TotalJoin")

      playerdata.loginStatus = playerdata.loginStatus.copy(totalLoginDay = if (total == 0) {
  1
} else {
  total
})

      try {
        val TodayDate = sdf.parse(sdf.format(cal.time))
        val LastDate = sdf.parse(playerdata.lastcheckdate)
        val TodayLong = TodayDate.time
        val LastLong = LastDate.time

        val datediff = (TodayLong - LastLong) / (1000 * 60 * 60 * 24)
        if (datediff > 0) {
          LLE.getLastcheck(playerdata.lastcheckdate)
          playerdata.loginStatus = playerdata.loginStatus.copy(totalLoginDay = playerdata.loginStatus.totalLoginDay + 1)
          if (datediff == 1L) {
            playerdata.loginStatus = playerdata.loginStatus.copy(chainLoginDay = playerdata.loginStatus.chainLoginDay + 1)
          } else {
            playerdata.loginStatus = playerdata.loginStatus.copy(chainLoginDay = 1)
          }
        }
      } catch (e: ParseException) {
        e.printStackTrace()
      }

      playerdata.lastcheckdate = sdf.format(cal.time)

      //連続投票の更新
      val lastvote = rs.getString("lastvote")
      if (lastvote.isNullOrEmpty()) {
        playerdata.ChainVote = 0
      } else {
        try {
          val TodayDate = sdf.parse(sdf.format(cal.time))
          val LastDate = sdf.parse(lastvote)
          val TodayLong = TodayDate.time
          val LastLong = LastDate.time

          val datediff = (TodayLong - LastLong) / (1000 * 60 * 60 * 24)
          playerdata.ChainVote =if (datediff <= 1 || datediff >= 0) {
             rs.getInt("chainvote")
          } else {
             0
          }
        } catch (e: ParseException) {
          e.printStackTrace()
        }

      }

      //実績解除フラグのBitSet型への復元処理
      //初回nullエラー回避のための分岐
      try {
        val Titlenums = rs.getString("TitleFlags").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val Titlearray = Arrays.stream(Titlenums).mapToLong { x: String -> java.lang.Long.parseUnsignedLong(x, 16) }.toArray()
        @NotNull
        val TitleFlags = BitSet.valueOf(Titlearray)
        playerdata.TitleFlags = TitleFlags
      } catch (e: NullPointerException) {
        playerdata.TitleFlags = BitSet(10000)
        playerdata.TitleFlags.set(1)
      }

      //建築
      playerdata.buildCount = BuildCount(
          rs.getInt("build_lv"),
          BigDecimal(rs.getString("build_count")),
          rs.getByte("build_count_flg")
      )

      //マナ妖精
      playerdata.usingVotingFairy = rs.getBoolean("canVotingFairyUse")
      playerdata.VotingFairyRecoveryValue = rs.getInt("VotingFairyRecoveryValue")
      playerdata.hasVotingFairyMana = rs.getInt("hasVotingFairyMana")
      playerdata.toggleGiveApple = rs.getInt("toggleGiveApple")
      playerdata.toggleVotingFairy = rs.getInt("toggleVotingFairy")
      playerdata.setVotingFairyTime(rs.getString("newVotingFairyTime"))
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
    val table = DatabaseConstants.PLAYERDATA_TABLENAME
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
      playerdata.setContributionPoint(addMana)
    }
    timer.sendLapTimeMessage(ChatColor.GREEN.toString() + p.name + "のプレイヤーデータ読込完了")
  }

  companion object {
    private val config = SeichiAssist.seichiAssistConfig
  }
}

inline fun ResultSet.recordIteration(operation: ResultSet.() -> Unit) {
  use {
    while (next()) {
      operation()
    }
  }
}

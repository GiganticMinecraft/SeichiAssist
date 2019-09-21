package com.github.unchama.seichiassist.task

import java.sql.{ResultSet, SQLException}
import java.util
import java.util.UUID

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.player.{AchievementPoint, PlayerNickName, StarLevel}
import com.github.unchama.seichiassist.util.BukkitSerialization
import kotlin.jvm.Throws
import org.bukkit.ChatColor._

object PlayerDataLoading {
  /**
   * プレイヤーデータロードを実施する処理(非同期で実行すること)
   *
   * @author unchama
   */
  @Deprecated("Should be inlined.")
  def loadExistingPlayerData(playerUUID: UUID, playerName: String): PlayerData = {
    val config = SeichiAssist.seichiAssistConfig
    val databaseGateway = SeichiAssist.databaseGateway

    val uuid: UUID = playerUUID
    val stringUuid: String = uuid.toString().toLowerCase()
    val db: String = SeichiAssist.seichiAssistConfig.db
    val timer: MillisecondTimer = MillisecondTimer.initializedTimerInstance()

    val playerData = PlayerData(playerUUID, playerName)

    @Throws(SQLException::class)
    def updateLoginInfo(stmt: Statement) {
    val loginInfoUpdateCommand = ("update "
    + db + "." + DatabaseConstants.PLAYERDATA_TABLENAME + " "
    + "set loginflag = true, "
    + "lastquit = cast(now() as datetime) "
    + "where uuid like '" + stringUuid + "'")

    stmt.executeUpdate(loginInfoUpdateCommand)
  }

    @Throws(SQLException::class)
    def loadSubHomeData(stmt: Statement) {
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

    if (world != null) {
    val location = Location(world, locationX.toDouble(), locationY.toDouble(), locationZ.toDouble())

    playerData.setSubHomeLocation(location, subHomeId)
    playerData.setSubHomeName(subHomeName, subHomeId)
  } else {
    println(s"Resetting ${playerName}'s subhome ${subHomeName}(${subHomeId}) in $worldName - world name not found.")
  }
  }
  }

    @Throws(SQLException::class)
    def loadMineStack(stmt: Statement) {
    val mineStackDataQuery = ("select * from "
    + db + "." + DatabaseConstants.MINESTACK_TABLENAME + " where "
    + "player_uuid like '" + stringUuid + "'")

    /**
     * TODO これはここにあるべきではない
     * 格納可能なアイテムのリストはプラグインインスタンスの中に動的に持たれるべきで、
     * そのリストをラップするオブジェクトに同期された形でこのオブジェクトがもたれるべきであり、
     * ロードされるたびに再計算されるべきではない
     */
    val nameObjectMappings: Map[String, MineStackObj] =
    MineStackObjectList.minestacklist
    .map { it.mineStackObjName to it }
    .toMap()

    val objectAmounts = HashMap[MineStackObj, Long]()

    stmt.executeQuery(mineStackDataQuery).recordIteration {
    val objectName = getString("object_name")
    val objectAmount = getLong("amount")
    val mineStackObj = nameObjectMappings[objectName]

    if (mineStackObj != null) {
    objectAmounts[mineStackObj] = objectAmount
  } else {
    Bukkit
    .getLogger()
    .warning(s"プレーヤー $playerName のMineStackオブジェクト $objectName は収納可能リストに見つかりませんでした。")
  }
  }

    playerData.minestack = MineStack(objectAmounts)
  }

    @Throws(SQLException::class)
    def loadGridTemplate(stmt: Statement) {
    val gridTemplateDataQuery = ("select * from "
    + db + "." + DatabaseConstants.GRID_TEMPLATE_TABLENAME + " where "
    + "designer_uuid like '" + stringUuid + "'")

    stmt.executeQuery(gridTemplateDataQuery).use { resultSet =>
    val templateMap = HashMap[Int, GridTemplate]()

    while (resultSet.next()) {
    val templateId = resultSet.getInt("id")

    val aheadLength = resultSet.getInt("ahead_length")
    val behindLength = resultSet.getInt("behind_length")
    val rightLength = resultSet.getInt("right_length")
    val leftLength = resultSet.getInt("left_length")

    val template = GridTemplate(aheadLength, behindLength, rightLength, leftLength)

    templateMap[templateId] = template
  }

    playerData.templateMap = templateMap
  }
  }

    @Throws(SQLException::class)
    def loadSkillEffectUnlockState(stmt: Statement) {
    val unlockedSkillEffectQuery = ("select * from "
    + db + "." + DatabaseConstants.SKILL_EFFECT_TABLENAME + " where "
    + "player_uuid like '" + stringUuid + "'")

    stmt.executeQuery(unlockedSkillEffectQuery).use { resultSet =>
    while (resultSet.next()) {
    val effectName = resultSet.getString("effect_name")

    val effect = ActiveSkillEffect.fromSqlName(effectName)
    playerData.activeskilldata.obtainedSkillEffects.add(effect)
  }
  }
  }

    @Throws(SQLException::class)
    def loadSkillPremiumEffectUnlockState(stmt: Statement) {
    val unlockedSkillEffectQuery = ("select * from "
    + db + "." + DatabaseConstants.SKILL_PREMIUM_EFFECT_TABLENAME + " where "
    + "player_uuid like '" + stringUuid + "'")

    stmt.executeQuery(unlockedSkillEffectQuery).use { resultSet =>
    while (resultSet.next()) {
    val effectName = resultSet.getString("effect_name")

    val effect = ActiveSkillPremiumEffect.fromSqlName(effectName)
    playerData.activeskilldata.obtainedSkillPremiumEffects.add(effect)
  }
  }
  }

    @Throws(SQLException::class, IOException::class)
    def loadPlayerData(stmt: Statement) {
    //playerdataをsqlデータから得られた値で更新
    val command = ("select * from " + db + "." + DatabaseConstants.PLAYERDATA_TABLENAME
    + " where uuid like '" + stringUuid + "'")

    stmt.executeQuery(command).recordIteration {
    val rs = this

    //各種数値
    runBlocking {
    playerData.settings.fastDiggingEffectSuppression.setStateFromSerializedValue(rs.getInt("effectflag"))
  }
    playerData.settings.autoMineStack = rs.getBoolean("minestackflag")
    playerData.settings.receiveFastDiggingEffectStats = rs.getBoolean("messageflag")
    playerData.activeskilldata.apply {
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

    playerData.gachapoint = rs.getInt("gachapoint")
    playerData.settings.receiveGachaTicketEveryMinute = rs.getBoolean("gachaflag")
    playerData.level = rs.getInt("level")
    playerData.unclaimedApologyItems = rs.getInt("numofsorryforbug")
    playerData.regionCount = rs.getInt("rgnum")
    playerData.pocketInventory = BukkitSerialization.fromBase64forPocket(rs.getString("inventory"))
    playerData.settings.shouldDisplayDeathMessages = rs.getBoolean("killlogflag")
    playerData.settings.shouldDisplayWorldGuardLogs = rs.getBoolean("worldguardlogflag")

    playerData.settings.multipleidbreakflag = rs.getBoolean("multipleidbreakflag")

    playerData.settings.pvpflag = rs.getBoolean("pvpflag")
    playerData.totalbreaknum = rs.getLong("totalbreaknum")
    playerData.playTick = rs.getInt("playtick")
    playerData.p_givenvote = rs.getInt("p_givenvote")
    playerData.activeskilldata.effectpoint = rs.getInt("effectpoint")
    playerData.activeskilldata.premiumeffectpoint = rs.getInt("premiumeffectpoint")
    //マナの情報
    playerData.activeskilldata.mana.mana = rs.getDouble("mana")
    playerData.settings.isExpBarVisible = rs.getBoolean("expvisible")

    playerData.totalexp = rs.getInt("totalexp")

    playerData.expmarge = rs.getByte("expmarge")
    playerData.contentsPresentInSharedInventory = !rs.getString("shareinv").isNullOrEmpty()
    playerData.settings.broadcastMutingSettings = BroadcastMutingSettings.fromBooleanSettings(rs.getBoolean("everymessage"), rs.getBoolean("everysound"))

    playerData.selectHomeNum = 0
    playerData.setHomeNameNum = 0
    playerData.isSubHomeNameChange = false

    //実績、二つ名の情報
    playerData.settings.nickName = PlayerNickName(
    PlayerNickName.Style.marshal(rs.getBoolean("displayTypeLv")),
    rs.getInt("displayTitle1No"),
    rs.getInt("displayTitle2No"),
    rs.getInt("displayTitle3No")
    )
    playerData.p_vote_forT = rs.getInt("p_vote")
    playerData.giveachvNo = rs.getInt("giveachvNo")
    playerData.achievePoint = AchievementPoint(
    rs.getInt("achvPointMAX"),
    rs.getInt("achvPointUSE"),
    rs.getInt("achvChangenum")
    )

    //スターレベルの情報
    playerData.starLevels = StarLevel(
    rs.getInt("starlevel_Break"),
    rs.getInt("starlevel_Time"),
    rs.getInt("starlevel_Event")
    )

    //期間限定ログインイベント専用の累計ログイン日数
    playerData.LimitedLoginCount = rs.getInt("LimitedLoginCount")

    //連続・通算ログインの情報、およびその更新
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy/MM/dd")
    val lastIn = rs.getString("lastcheckdate")
    playerData.lastcheckdate = if (lastIn.isNullOrEmpty()) {
    sdf.format(cal.time)
  } else {
    lastIn
  }
    val chain = rs.getInt("ChainJoin")
    playerData.loginStatus = playerData.loginStatus.copy(consecutiveLoginDays = if (chain == 0) {
    1
  } else {
    chain
  })
    val total = rs.getInt("TotalJoin")

    playerData.loginStatus = playerData.loginStatus.copy(totalLoginDay = if (total == 0) {
    1
  } else {
    total
  })

    try {
    val TodayDate = sdf.parse(sdf.format(cal.time))
    val LastDate = sdf.parse(playerData.lastcheckdate)
    val TodayLong = TodayDate.time
    val LastLong = LastDate.time

    val dateDiff = (TodayLong - LastLong) / (1000 * 60 * 60 * 24)
    if (dateDiff >= 1L) {
    val newTotalLoginDay = playerData.loginStatus.totalLoginDay + 1
    val newConsecutiveLoginDays =
    if (dateDiff <= 2L)
    playerData.loginStatus.consecutiveLoginDays + 1
    else
    1

    playerData.loginStatus =
    playerData.loginStatus.copy(totalLoginDay = newTotalLoginDay, consecutiveLoginDays = newConsecutiveLoginDays)
  }
  } catch (e: ParseException) {
    e.printStackTrace()
  }

    playerData.lastcheckdate = sdf.format(cal.time)

    playerData.ChainVote = rs.getInt("chainvote")

    //実績解除フラグのBitSet型への復元処理
    //初回nullエラー回避のための分岐
    try {
    val Titlenums = rs.getString("TitleFlags").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      val Titlearray = util.Arrays.stream(Titlenums).mapToLong { x: String => java.lang.Long.parseUnsignedLong(x, 16) }.toArray()
    @NotNull
    val TitleFlags = BitSet.valueOf(Titlearray)
    playerData.TitleFlags = TitleFlags
  } catch (e: Exception) {
    playerData.TitleFlags = BitSet(10000)
    playerData.TitleFlags.set(1)
  }

    //建築
    playerData.buildCount = BuildCount(
    rs.getInt("build_lv"),
    BigDecimal(rs.getString("build_count")),
    rs.getByte("build_count_flg")
    )

    //マナ妖精
    playerData.usingVotingFairy = rs.getBoolean("canVotingFairyUse")
    playerData.VotingFairyRecoveryValue = rs.getInt("VotingFairyRecoveryValue")
    playerData.hasVotingFairyMana = rs.getInt("hasVotingFairyMana")
    playerData.toggleGiveApple = rs.getInt("toggleGiveApple")
    playerData.toggleVotingFairy = rs.getInt("toggleVotingFairy")
    playerData.setVotingFairyTime(rs.getString("newVotingFairyTime"))
    playerData.p_apple = rs.getLong("p_apple")


    playerData.contribute_point = rs.getInt("contribute_point")
    playerData.added_mana = rs.getInt("added_mana")

    playerData.giganticBerserk = GiganticBerserk(
    rs.getInt("GBlevel"),
    rs.getInt("GBexp"),
    rs.getInt("GBstage"),
    rs.getBoolean("isGBStageUp")
    )
    playerData.anniversary = rs.getBoolean("anniversary")

    //正月イベント用
    playerData.hasNewYearSobaGive = rs.getBoolean("hasNewYearSobaGive")
    playerData.newYearBagAmount = rs.getInt("newYearBagAmount")

    //バレンタインイベント用
    playerData.hasChocoGave = rs.getBoolean("hasChocoGave")
  }
  }

    //sqlコネクションチェック
    databaseGateway.ensureConnection()

    //同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
    val stmt: Statement = databaseGateway.con.createStatement()

    loadPlayerData(stmt)
    updateLoginInfo(stmt)
    loadGridTemplate(stmt)
    loadMineStack(stmt)
    loadSkillEffectUnlockState(stmt)
    loadSkillPremiumEffectUnlockState(stmt)
    loadSubHomeData(stmt)

    //念のためstatement閉じておく
    try {
    stmt.close()
  } catch (e: SQLException) {
    e.printStackTrace()
  }

    //貢献度pt増加によるマナ増加があるかどうか
    if (playerData.added_mana < playerData.contribute_point) {
    val addMana: Int = playerData.contribute_point - playerData.added_mana
    playerData.setContributionPoint(addMana)
  }

    timer.sendLapTimeMessage(s"$GREEN${playerName}のプレイヤーデータ読込完了")

    return playerData
  }

  inline def ResultSet.recordIteration(operation: ResultSet.() => Unit) {
    use {
      while (next()) {
        operation()
      }
    }
  }
}

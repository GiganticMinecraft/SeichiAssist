package com.github.unchama.seichiassist.task

import java.sql.{ResultSet, Statement}
import java.text.{ParseException, SimpleDateFormat}
import java.util.{Calendar, UUID}

import com.github.unchama.seichiassist.data.GridTemplate
import com.github.unchama.seichiassist.data.player._
import com.github.unchama.seichiassist.data.player.settings.BroadcastMutingSettings
import com.github.unchama.seichiassist.database.DatabaseConstants
import com.github.unchama.seichiassist.activeskill.effect.{ActiveSkillNormalEffect, ActiveSkillPremiumEffect}
import com.github.unchama.seichiassist.minestack.MineStackObj
import com.github.unchama.seichiassist.util.BukkitSerialization
import com.github.unchama.seichiassist.{MineStackObjectList, SeichiAssist}
import com.github.unchama.util.MillisecondTimer
import org.bukkit.ChatColor._
import org.bukkit.{Bukkit, Location}

import scala.collection.mutable
import scala.util.Using

object PlayerDataLoading {

  import com.github.unchama.util.syntax._

  /**
   * プレイヤーデータロードを実施する処理(非同期で実行すること)
   *
   * @deprecated Should be inlined.
   * @author unchama
   */
  @Deprecated()
  def loadExistingPlayerData(playerUUID: UUID, playerName: String): PlayerData = {
    val config = SeichiAssist.seichiAssistConfig
    val databaseGateway = SeichiAssist.databaseGateway

    val uuid: UUID = playerUUID
    val stringUuid: String = uuid.toString.toLowerCase()
    val db: String = SeichiAssist.seichiAssistConfig.getDB
    val timer: MillisecondTimer = MillisecondTimer.getInitializedTimerInstance

    val playerData = new PlayerData(playerUUID, playerName)

    def updateLoginInfo(stmt: Statement): Unit = {
      val loginInfoUpdateCommand = ("update "
        + db + "." + DatabaseConstants.PLAYERDATA_TABLENAME + " "
        + "set loginflag = true, "
        + "lastquit = cast(now() as datetime) "
        + "where uuid = '" + stringUuid + "'")

      stmt.executeUpdate(loginInfoUpdateCommand)
    }

    def loadSubHomeData(stmt: Statement): Unit = {
      val subHomeDataQuery = ("select * from "
        + db + "." + DatabaseConstants.SUB_HOME_TABLENAME + " where "
        + s"player_uuid = '$stringUuid' and "
        + "server_id = " + config.getServerNum)

      stmt.executeQuery(subHomeDataQuery).recordIteration { lrs: ResultSet =>
        import lrs._
        val subHomeId = getInt("id")
        val subHomeName = getString("name")
        val locationX = getInt("location_x")
        val locationY = getInt("location_y")
        val locationZ = getInt("location_z")
        val worldName = getString("world_name")

        val world = Bukkit.getWorld(worldName)

        if (world != null) {
          val location = new Location(world, locationX.toDouble, locationY.toDouble, locationZ.toDouble)

          playerData.setSubHomeLocation(location, subHomeId)
          playerData.setSubHomeName(subHomeName, subHomeId)
        } else {
          println(s"Resetting $playerName's subhome $subHomeName($subHomeId) in $worldName - world name not found.")
        }
      }
    }

    def loadMineStack(stmt: Statement): Unit = {
      val mineStackDataQuery = ("select * from "
        + db + "." + DatabaseConstants.MINESTACK_TABLENAME + " where "
        + "player_uuid = '" + stringUuid + "'")

      /**
       * TODO:これはここにあるべきではない
       * 格納可能なアイテムのリストはプラグインインスタンスの中に動的に持たれるべきで、
       * そのリストをラップするオブジェクトに同期された形でこのオブジェクトがもたれるべきであり、
       * ロードされるたびに再計算されるべきではない
       */
      val nameObjectMappings: Map[String, MineStackObj] =
        MineStackObjectList.minestacklist.map(obj => obj.mineStackObjName -> obj).toMap

      val objectAmounts = mutable.HashMap[MineStackObj, Long]()

      stmt.executeQuery(mineStackDataQuery).recordIteration { lrs: ResultSet =>
        import lrs._
        val objectName = getString("object_name")
        val objectAmount = getLong("amount")

        nameObjectMappings.get(objectName) match {
          case Some(mineStackObj) =>
            objectAmounts(mineStackObj) = objectAmount
          case None =>
            Bukkit
              .getLogger
              .warning(s"プレーヤー $playerName のMineStackオブジェクト $objectName は収納可能リストに見つかりませんでした。")
        }
      }

      playerData.minestack = new MineStack(objectAmounts)
    }

    def loadGridTemplate(stmt: Statement): Unit = {
      // TODO: 本当にStarSelectじゃなきゃだめ?
      val gridTemplateDataQuery = ("select * from "
        + db + "." + DatabaseConstants.GRID_TEMPLATE_TABLENAME + " where "
        + s"designer_uuid = '$stringUuid'")

      stmt.executeQuery(gridTemplateDataQuery).recordIteration { resultSet: ResultSet =>
        val templateMap = mutable.HashMap[Int, GridTemplate]()

        while (resultSet.next()) {
          val templateId = resultSet.getInt("id")

          val aheadLength = resultSet.getInt("ahead_length")
          val behindLength = resultSet.getInt("behind_length")
          val rightLength = resultSet.getInt("right_length")
          val leftLength = resultSet.getInt("left_length")

          val template = new GridTemplate(aheadLength, behindLength, rightLength, leftLength)

          templateMap(templateId) = template
        }

        playerData.templateMap = templateMap
      }
    }

    def loadSkillEffectUnlockState(stmt: Statement): Unit = {
      // TODO: 本当にStarSelectじゃなきゃだめ?
      val unlockedSkillEffectQuery = ("select * from "
        + db + "." + DatabaseConstants.SKILL_EFFECT_TABLENAME + " where "
        + "player_uuid = '" + stringUuid + "'")

      stmt.executeQuery(unlockedSkillEffectQuery).recordIteration { resultSet: ResultSet =>
        val effectName = resultSet.getString("effect_name")

        val effect = ActiveSkillNormalEffect.fromSqlName(effectName).get
        playerData.activeskilldata.obtainedSkillEffects.add(effect)
      }
    }

    def loadSkillPremiumEffectUnlockState(stmt: Statement): Unit = {
      val unlockedSkillEffectQuery = ("select * from "
        + db + "." + DatabaseConstants.SKILL_PREMIUM_EFFECT_TABLENAME + " where "
        + "player_uuid = '" + stringUuid + "'")

      stmt.executeQuery(unlockedSkillEffectQuery).recordIteration { resultSet: ResultSet =>
        val effectName = resultSet.getString("effect_name")

        val effect = ActiveSkillPremiumEffect.fromSqlName(effectName).get
        playerData.activeskilldata.obtainedSkillPremiumEffects.add(effect)
      }
    }

    def loadPlayerData(stmt: Statement): Unit = {
      //playerdataをsqlデータから得られた値で更新
      // TODO: 本当にStarSelectじゃなきゃだめ?
      val command = ("select * from " + db + "." + DatabaseConstants.PLAYERDATA_TABLENAME
        + " where uuid = '" + stringUuid + "'")

      stmt.executeQuery(command).recordIteration { rs: ResultSet =>

        //各種数値
        playerData.settings.fastDiggingEffectSuppression.setStateFromSerializedValue(rs.getInt("effectflag")).unsafeRunSync()
        playerData.settings.autoMineStack = rs.getBoolean("minestackflag")
        playerData.settings.receiveFastDiggingEffectStats = rs.getBoolean("messageflag")
        playerData.activeskilldata.modify { d =>
          import d._
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
        playerData.activeskilldata.mana.setMana(rs.getDouble("mana"))
        playerData.settings.isExpBarVisible = rs.getBoolean("expvisible")

        playerData.totalexp = rs.getInt("totalexp")

        playerData.expmarge = rs.getByte("expmarge")
        playerData.contentsPresentInSharedInventory = {
          val serializedInventory = rs.getString("shareinv")
          serializedInventory != null && serializedInventory != ""
        }

        playerData.settings.broadcastMutingSettings = BroadcastMutingSettings.fromBooleanSettings(rs.getBoolean("everymessage"), rs.getBoolean("everysound"))

        playerData.selectHomeNum = 0

        //実績、二つ名の情報
        playerData.settings.nickname = PlayerNickname(
          NicknameStyle.marshal(rs.getBoolean("displayTypeLv")),
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
        val sdf = new SimpleDateFormat("yyyy/MM/dd")
        val lastIn = rs.getString("lastcheckdate")
        playerData.lastcheckdate = if (lastIn == null || lastIn == "") {
          sdf.format(cal.getTime)
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
          val TodayDate = sdf.parse(sdf.format(cal.getTime))
          val LastDate = sdf.parse(playerData.lastcheckdate)
          val TodayLong = TodayDate.getTime
          val LastLong = LastDate.getTime

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
        } catch {
          case e: ParseException => e.printStackTrace()
        }

        playerData.lastcheckdate = sdf.format(cal.getTime)

        playerData.ChainVote = rs.getInt("chainvote")

        //実績解除フラグのBitSet型への復元処理
        //初回nullエラー回避のための分岐
        try {
          val Titlenums = rs.getString("TitleFlags").split(",").reverse.dropWhile(_.isEmpty).reverse

          val Titlearray = Titlenums.map { x: String => java.lang.Long.parseUnsignedLong(x, 16) }
          val TitleFlags = mutable.BitSet.fromBitMask(Titlearray)
          playerData.TitleFlags = TitleFlags
        } catch {
          case e: Exception =>
            playerData.TitleFlags = new mutable.BitSet(10000)
            playerData.TitleFlags.addOne(1)
        }

        //建築
        playerData.buildCount = BuildCount(
          rs.getInt("build_lv"),
          new java.math.BigDecimal(rs.getString("build_count")),
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
    Using(databaseGateway.con.createStatement()) { newStmt =>
      loadPlayerData(newStmt)
      updateLoginInfo(newStmt)
      loadGridTemplate(newStmt)
      loadMineStack(newStmt)
      loadSkillEffectUnlockState(newStmt)
      loadSkillPremiumEffectUnlockState(newStmt)
      loadSubHomeData(newStmt)
    }

    //貢献度pt増加によるマナ増加があるかどうか
    if (playerData.added_mana < playerData.contribute_point) {
      val addMana: Int = playerData.contribute_point - playerData.added_mana
      playerData.setContributionPoint(addMana)
    }

    timer.sendLapTimeMessage(s"$GREEN${playerName}のプレイヤーデータ読込完了")

    playerData
  }
}
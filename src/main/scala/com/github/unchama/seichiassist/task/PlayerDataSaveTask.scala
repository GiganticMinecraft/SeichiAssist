package com.github.unchama.seichiassist.task

import java.sql.{SQLException, Statement}

import com.github.unchama.seichiassist.data.player.{NicknameStyle, PlayerData, PlayerSkillState}
import com.github.unchama.seichiassist.seichiskill.effect.{ActiveSkillNormalEffect, ActiveSkillPremiumEffect}
import com.github.unchama.seichiassist.util.BukkitSerialization
import com.github.unchama.seichiassist.{MineStackObjectList, SeichiAssist}
import com.github.unchama.util.ActionStatus
import org.bukkit.ChatColor._

import scala.util.Using

object PlayerDataSaveTask {
  /**
   * プレイヤーデータをDBに同期的に保存する処理
   * DBにセーブしたい値が増えた/減った場合は更新すること
   *
   * @param playerdata 保存するプレーヤーデータ
   * @author unchama
   */
  def savePlayerData(playerdata: PlayerData): Unit = {
    val databaseGateway = SeichiAssist.databaseGateway
    val serverId = SeichiAssist.seichiAssistConfig.getServerNum

    def updatePlayerMineStack(stmt: Statement): Unit = {
      val playerUuid = playerdata.uuid.toString
      MineStackObjectList.minestacklist.foreach { mineStackObj =>
        val iThObjectName = mineStackObj.mineStackObjName
        val iThObjectAmount = playerdata.minestack.getStackedAmountOf(mineStackObj)

        val updateCommand = ("insert into seichiassist.mine_stack"
          + "(player_uuid, object_name, amount) values "
          + "('" + playerUuid + "', '" + iThObjectName + "', '" + iThObjectAmount + "') "
          + "on duplicate key update amount = values(amount)")

        stmt.executeUpdate(updateCommand)
      }
    }

    def updateSubHome(): Unit = {
      val playerUuid = playerdata.uuid.toString
      playerdata.subHomeEntries.foreach { case (subHomeId, subHome) =>
        val subHomeLocation = subHome.getLocation

        val template = ("insert into seichiassist.sub_home"
          + "(player_uuid,server_id,id,name,location_x,location_y,location_z,world_name) values "
          + "(?,?,?,?,?,?,?,?) "
          + "on duplicate key update "
          + "name = values(name), "
          + "location_x = values(location_x), "
          + "location_y = values(location_y), "
          + "location_z = values(location_z), "
          + "world_name = values(world_name)")

        Using(databaseGateway.con.prepareStatement(template)) { statement =>
          statement.setString(1, playerUuid)
          statement.setInt(2, serverId)
          statement.setInt(3, subHomeId)
          statement.setString(4, subHome.name)
          statement.setInt(5, subHomeLocation.getX.toInt)
          statement.setInt(6, subHomeLocation.getY.toInt)
          statement.setInt(7, subHomeLocation.getZ.toInt)
          statement.setString(8, subHomeLocation.getWorld.getName)

          statement.executeUpdate()
        }
      }
    }

    def updateGridTemplate(stmt: Statement): Unit = {
      val playerUuid = playerdata.uuid.toString

      // 既存データをすべてクリアする
      stmt.executeUpdate(s"delete from seichiassist.grid_template where designer_uuid = '$playerUuid'")

      // 各グリッドテンプレートについてデータを保存する
      playerdata.templateMap.toList.map { case (gridTemplateId, gridTemplate) =>
        val updateCommand = "insert into seichiassist.grid_template set " +
          "id = " + gridTemplateId + ", " +
          "designer_uuid = '" + playerUuid + "', " +
          "ahead_length = " + gridTemplate.getAheadAmount + ", " +
          "behind_length = " + gridTemplate.getBehindAmount + ", " +
          "right_length = " + gridTemplate.getRightAmount + ", " +
          "left_length = " + gridTemplate.getLeftAmount

        stmt.executeUpdate(updateCommand)
      }
    }

    def updateActiveSkillEffectUnlockState(stmt: Statement): Unit = {
      val playerUuid = playerdata.uuid.toString
      val activeSkillEffects = ActiveSkillNormalEffect.values ++ ActiveSkillPremiumEffect.values
      val obtainedEffects = playerdata.skillEffectState.obtainedEffects

      val removeCommand = s"delete from seichiassist.unlocked_active_skill_effect where player_uuid = '$playerUuid'"
      stmt.executeUpdate(removeCommand)

      activeSkillEffects.foreach { activeSkillEffect =>
        val effectName = activeSkillEffect.nameOnDatabase
        val isEffectUnlocked = obtainedEffects.contains(activeSkillEffect)

        if (isEffectUnlocked) {
          // TODO クエリを結合したほうが良い
          val updateCommand =
            "insert into seichiassist.unlocked_active_skill_effect(player_uuid, effect_name) " +
              s"values ('$playerUuid', '$effectName')"

          stmt.executeUpdate(updateCommand)
        }
      }
    }

    def updatePlayerDataColumns(stmt: Statement): Unit = {
      val playerUuid = playerdata.uuid.toString

      //実績のフラグ(BitSet)保存用変換処理
      val flagString = playerdata.TitleFlags.toBitMask.map(_.toHexString).mkString(",")

      val legacySkillState = PlayerSkillState.Migration.toLegacyState(playerdata.skillState)

      val command = {
        ("update seichiassist.playerdata set"
          //名前更新処理
          + " name = '" + playerdata.lowercaseName + "'"

          //各種数値更新処理
          + ",effectflag = " + playerdata.settings.fastDiggingEffectSuppression.serialized().unsafeRunSync()
          + ",minestackflag = " + playerdata.settings.autoMineStack
          + ",messageflag = " + playerdata.settings.receiveFastDiggingEffectStats
          + ",activemineflagnum = " + legacySkillState.mineflagnum
          + ",activeskilltype = " + legacySkillState.skilltype
          + ",activeskillnum = " + legacySkillState.skillnum
          + ",assaultskilltype = " + legacySkillState.assaulttype
          + ",assaultskillnum = " + legacySkillState.assaultnum
          + ",arrowskill = " + legacySkillState.arrowskill
          + ",multiskill = " + legacySkillState.multiskill
          + ",breakskill = " + legacySkillState.breakskill
          + ",fluidcondenskill = " + legacySkillState.fluidcondenskill
          + ",watercondenskill = " + legacySkillState.watercondenskill
          + ",lavacondenskill = " + legacySkillState.lavacondenskill
          + ",effectnum = " + legacySkillState.effectnum
          + ",gachapoint = " + playerdata.gachapoint
          + ",gachaflag = " + playerdata.settings.receiveGachaTicketEveryMinute
          + ",level = " + playerdata.level
          + ",rgnum = " + playerdata.regionCount
          + ",totalbreaknum = " + playerdata.totalbreaknum
          + ",inventory = '" + BukkitSerialization.toBase64(playerdata.pocketInventory) + "'"
          + ",playtick = " + playerdata.playTick
          + ",lastquit = cast( now() as datetime )"
          + ",killlogflag = " + playerdata.settings.shouldDisplayDeathMessages
          + ",worldguardlogflag = " + playerdata.settings.shouldDisplayWorldGuardLogs

          + ",multipleidbreakflag = " + playerdata.settings.multipleidbreakflag

          + ",pvpflag = " + playerdata.settings.pvpflag
          + ",effectpoint = " + playerdata.effectPoint
          + ",premiumeffectpoint = " + playerdata.premiumEffectPoint
          + ",mana = " + playerdata.manaState.getMana
          + ",expvisible = " + playerdata.settings.isExpBarVisible
          + ",totalexp = " + playerdata.totalexp
          + ",expmarge = " + playerdata.expmarge
          + ",everysound = " + playerdata.settings.getBroadcastMutingSettings.unsafeRunSync().shouldMuteSounds
          + ",everymessage = " + playerdata.settings.getBroadcastMutingSettings.unsafeRunSync().shouldMuteMessages

          + ",displayTypeLv = " + (playerdata.settings.nickname.style == NicknameStyle.Level)
          + ",displayTitle1No = " + playerdata.settings.nickname.id1
          + ",displayTitle2No = " + playerdata.settings.nickname.id2
          + ",displayTitle3No = " + playerdata.settings.nickname.id3
          + ",giveachvNo = " + playerdata.giveachvNo
          + ",achvPointMAX = " + playerdata.achievePoint.fromUnlockedAchievements
          + ",achvPointUSE = " + playerdata.achievePoint.used
          + ",achvChangenum = " + playerdata.achievePoint.conversionCount
          + ",starlevel = " + playerdata.totalStarLevel
          + ",starlevel_Break = " + playerdata.starLevels.fromBreakAmount
          + ",starlevel_Time = " + playerdata.starLevels.fromConnectionTime
          + ",starlevel_Event = " + playerdata.starLevels.fromEventAchievement

          + ",lastcheckdate = '" + playerdata.lastcheckdate + "'"
          + ",ChainJoin = " + playerdata.loginStatus.consecutiveLoginDays
          + ",TotalJoin = " + playerdata.loginStatus.totalLoginDay
          + ",LimitedLoginCount = " + playerdata.LimitedLoginCount

          //建築
          + ",build_lv = " + playerdata.buildCount.lv
          + ",build_count = " + playerdata.buildCount.count //.toString()
          + ",build_count_flg = " + playerdata.buildCount.migrationFlag

          //投票
          + ",canVotingFairyUse = " + playerdata.usingVotingFairy
          + ",newVotingFairyTime = '" + playerdata.getVotingFairyStartTimeAsString + "'"
          + ",VotingFairyRecoveryValue = " + playerdata.VotingFairyRecoveryValue
          + ",hasVotingFairyMana = " + playerdata.hasVotingFairyMana
          + ",toggleGiveApple = " + playerdata.toggleGiveApple
          + ",toggleVotingFairy = " + playerdata.toggleVotingFairy
          + ",p_apple = " + playerdata.p_apple

          //貢献度pt
          + ",added_mana = " + playerdata.added_mana

          + ",GBstage = " + playerdata.giganticBerserk.stage
          + ",GBexp = " + playerdata.giganticBerserk.exp
          + ",GBlevel = " + playerdata.giganticBerserk.level
          + ",isGBStageUp = " + playerdata.giganticBerserk.canEvolve
          + ",TitleFlags = '" + flagString + "'"

          //正月イベント
          + ",hasNewYearSobaGive = " + playerdata.hasNewYearSobaGive
          + ",newYearBagAmount = " + playerdata.newYearBagAmount

          //バレンタインイベント
          + ",hasChocoGave = " + playerdata.hasChocoGave

          + " where uuid = '" + playerUuid + "'")
      }

      stmt.executeUpdate(command)
    }

    def executeUpdate(): ActionStatus = {
      try {
        //sqlコネクションチェック
        databaseGateway.ensureConnection()

        //同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
        val localStatement = databaseGateway.con.createStatement()
        updateActiveSkillEffectUnlockState(localStatement)
        updatePlayerDataColumns(localStatement)
        updatePlayerMineStack(localStatement)
        updateGridTemplate(localStatement)
        updateSubHome()
        ActionStatus.Ok
      } catch {
        case exception: SQLException =>
          println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
          exception.printStackTrace()
          ActionStatus.Fail
      }
    }

    (0 until 3).foreach { _ =>
      val result = executeUpdate()
      if (result == ActionStatus.Ok) {
        println(s"$GREEN${playerdata.lowercaseName}のプレイヤーデータ保存完了")
        return
      }
    }

    println(s"$RED${playerdata.lowercaseName}のプレイヤーデータ保存失敗")
  }
}

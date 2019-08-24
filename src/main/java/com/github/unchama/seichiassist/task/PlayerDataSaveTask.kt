package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.ActiveSkillEffect
import com.github.unchama.seichiassist.ActiveSkillPremiumEffect
import com.github.unchama.seichiassist.MineStackObjectList
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.PlayerData
import com.github.unchama.seichiassist.util.BukkitSerialization
import com.github.unchama.util.ActionStatus
import com.github.unchama.util.ActionStatus.Fail
import com.github.unchama.util.ActionStatus.Ok
import kotlinx.coroutines.runBlocking
import org.bukkit.ChatColor
import org.bukkit.scheduler.BukkitRunnable
import java.sql.SQLException
import java.sql.Statement

/**
 * プレイヤーデータをDBに保存する処理(非同期で実行すること)
 * DBにセーブしたい値が増えた/減った場合は更新すること

 * @param _playerdata 保存するプレーヤーデータ
 * @param _isondisable ondisableからの呼び出し時のみtrueにしておくフラグ
 * @param _logoutflag loginflag折る時にtrueにしておくフラグ

 * @author unchama
 */
class PlayerDataSaveTask(internal val playerdata: PlayerData,
                         private val isOnDisable: Boolean,
                         private val logoutflag: Boolean) : BukkitRunnable() {
  private val plugin = SeichiAssist.instance
  private val databaseGateway = SeichiAssist.databaseGateway
  private val serverId = SeichiAssist.seichiAssistConfig.serverNum

  @Throws(SQLException::class)
  private fun updatePlayerMineStack(stmt: Statement) {
    val playerUuid = playerdata.uuid.toString()
    for (mineStackObj in MineStackObjectList.minestacklist!!) {
      val iThObjectName = mineStackObj.mineStackObjName
      val iThObjectAmount = playerdata.minestack.getStackedAmountOf(mineStackObj)

      val updateCommand = ("insert into seichiassist.mine_stack"
          + "(player_uuid, object_name, amount) values "
          + "('" + playerUuid + "', '" + iThObjectName + "', '" + iThObjectAmount + "') "
          + "on duplicate key update amount = values(amount)")

      stmt.executeUpdate(updateCommand)
    }
  }

  @Throws(SQLException::class)
  private fun updateSubHome() {
    val playerUuid = playerdata.uuid.toString()
    for ((subHomeId, subHome) in playerdata.subHomeEntries) {
      val subHomeLocation = subHome.location

      val template = ("insert into seichiassist.sub_home"
          + "(player_uuid,server_id,id,name,location_x,location_y,location_z,world_name) values "
          + "(?,?,?,?,?,?,?,?) "
          + "on duplicate key update "
          + "name = values(name), "
          + "location_x = values(location_x), "
          + "location_y = values(location_y), "
          + "location_z = values(location_z), "
          + "world_name = values(world_name)")

      databaseGateway.con.prepareStatement(template).use { statement ->
        statement.setString(1, playerUuid)
        statement.setInt(2, serverId)
        statement.setInt(3, subHomeId)
        statement.setString(4, subHome.name)
        statement.setInt(5, subHomeLocation.x.toInt())
        statement.setInt(6, subHomeLocation.y.toInt())
        statement.setInt(7, subHomeLocation.z.toInt())
        statement.setString(8, subHomeLocation.world.name)

        statement.executeUpdate()
      }
    }
  }

  @Throws(SQLException::class)
  private fun updateGridTemplate(stmt: Statement) {
    val playerUuid = playerdata.uuid.toString()

    // 既存データをすべてクリアする
    stmt.executeUpdate("delete from seichiassist.grid_template where designer_uuid = '$playerUuid'")

    // 各グリッドテンプレートについてデータを保存する
    for ((gridTemplateId, gridTemplate) in playerdata.templateMap!!) {

      val updateCommand = "insert into seichiassist.grid_template set " +
          "id = " + gridTemplateId + ", " +
          "designer_uuid = '" + playerUuid + "', " +
          "ahead_length = " + gridTemplate.aheadAmount + ", " +
          "behind_length = " + gridTemplate.behindAmount + ", " +
          "right_length = " + gridTemplate.rightAmount + ", " +
          "left_length = " + gridTemplate.leftAmount

      stmt.executeUpdate(updateCommand)
    }
  }

  @Throws(SQLException::class)
  private fun updateActiveSkillEffectUnlockState(stmt: Statement) {
    val playerUuid = playerdata.uuid.toString()
    val activeSkillEffects = ActiveSkillEffect.values()
    val obtainedEffects = playerdata.activeskilldata.obtainedSkillEffects

    val removeCommand = ("delete from "
        + "seichiassist.unlocked_active_skill_effect "
        + "where player_uuid like '" + playerUuid + "'")
    stmt.executeUpdate(removeCommand)

    for (activeSkillEffect in activeSkillEffects) {
      val effectName = activeSkillEffect.nameOnDatabase
      val isEffectUnlocked = obtainedEffects.contains(activeSkillEffect)

      if (isEffectUnlocked) {
        val updateCommand = ("insert into "
            + "seichiassist.unlocked_active_skill_effect(player_uuid, effect_name) "
            + "values ('" + playerUuid + "', '" + effectName + "')")

        stmt.executeUpdate(updateCommand)
      }
    }
  }

  @Throws(SQLException::class)
  private fun updateActiveSkillPremiumEffectUnlockState(stmt: Statement) {
    val playerUuid = playerdata.uuid.toString()
    val activeSkillPremiumEffects = ActiveSkillPremiumEffect.values()
    val obtainedEffects = playerdata.activeskilldata.obtainedSkillPremiumEffects

    val removeCommand = ("delete from "
        + "seichiassist.unlocked_active_skill_premium_effect where "
        + "player_uuid like '" + playerUuid + "'")
    stmt.executeUpdate(removeCommand)

    for (activeSkillPremiumEffect in activeSkillPremiumEffects) {
      val effectName = activeSkillPremiumEffect.getsqlName()
      val isEffectUnlocked = obtainedEffects.contains(activeSkillPremiumEffect)

      if (isEffectUnlocked) {
        val updateCommand = ("insert into "
            + "seichiassist.unlocked_active_skill_premium_effect(player_uuid, effect_name) "
            + "values ('" + playerUuid + "', '" + effectName + "')")

        stmt.executeUpdate(updateCommand)
      }
    }
  }

  @ExperimentalUnsignedTypes
  @Throws(SQLException::class)
  private fun updatePlayerDataColumns(stmt: Statement) {
    val playerUuid = playerdata.uuid.toString()

    //実績のフラグ(BitSet)保存用変換処理
    val titleArray = playerdata.TitleFlags.toLongArray()
    val flagString = titleArray.joinToString(",") { it.toULong().toString(16) }

    val command = runBlocking {
      ("update seichiassist.playerdata set"
          //名前更新処理
          + " name = '" + playerdata.lowercaseName + "'"

          //各種数値更新処理
          + ",effectflag = " + runBlocking { playerdata.fastDiggingEffectSuppression.serialized() }
          + ",minestackflag = " + playerdata.minestackflag
          + ",messageflag = " + playerdata.messageflag
          + ",activemineflagnum = " + playerdata.activeskilldata.mineflagnum
          + ",assaultflag = " + playerdata.activeskilldata.assaultflag
          + ",activeskilltype = " + playerdata.activeskilldata.skilltype
          + ",activeskillnum = " + playerdata.activeskilldata.skillnum
          + ",assaultskilltype = " + playerdata.activeskilldata.assaulttype
          + ",assaultskillnum = " + playerdata.activeskilldata.assaultnum
          + ",arrowskill = " + playerdata.activeskilldata.arrowskill
          + ",multiskill = " + playerdata.activeskilldata.multiskill
          + ",breakskill = " + playerdata.activeskilldata.breakskill
          + ",fluidcondenskill = " + playerdata.activeskilldata.fluidcondenskill
          + ",watercondenskill = " + playerdata.activeskilldata.watercondenskill
          + ",lavacondenskill = " + playerdata.activeskilldata.lavacondenskill
          + ",effectnum = " + playerdata.activeskilldata.effectnum
          + ",gachapoint = " + playerdata.gachapoint
          + ",gachaflag = " + playerdata.receiveGachaTicketEveryMinute
          + ",level = " + playerdata.level
          + ",rgnum = " + playerdata.regionCount
          + ",totalbreaknum = " + playerdata.totalbreaknum
          + ",inventory = '" + BukkitSerialization.toBase64(playerdata.inventory) + "'"
          + ",playtick = " + playerdata.playTick
          + ",lastquit = cast( now() as datetime )"
          + ",killlogflag = " + playerdata.dispkilllogflag
          + ",worldguardlogflag = " + playerdata.dispworldguardlogflag

          + ",multipleidbreakflag = " + playerdata.multipleidbreakflag

          + ",pvpflag = " + playerdata.pvpflag
          + ",effectpoint = " + playerdata.activeskilldata.effectpoint
          + ",mana = " + playerdata.activeskilldata.mana.mana
          + ",expvisible = " + playerdata.expbar.isVisible
          + ",totalexp = " + playerdata.totalexp
          + ",expmarge = " + playerdata.expmarge
          + ",everysound = " + playerdata.getBroadcastMutingSettings().shouldMuteSounds()
          + ",everymessage = " + playerdata.getBroadcastMutingSettings().shouldMuteMessages()

          + ",displayTypeLv = " + playerdata.nickName.style.displayLevel
          + ",displayTitle1No = " + playerdata.nickName.id1
          + ",displayTitle2No = " + playerdata.nickName.id2
          + ",displayTitle3No = " + playerdata.nickName.id3
          + ",giveachvNo = " + playerdata.giveachvNo
          + ",achvPointMAX = " + playerdata.achievePoint.cumulativeTotal
          + ",achvPointUSE = " + playerdata.achievePoint.used
          + ",achvChangenum = " + playerdata.achievePoint.conversionCount
          + ",starlevel = " + playerdata.totalStarLevel
          + ",starlevel_Break = " + playerdata.starLevels.fromBreakAmount
          + ",starlevel_Time = " + playerdata.starLevels.fromConnectionTime
          + ",starlevel_Event = " + playerdata.starLevels.fromEventAchievement

          + ",lastcheckdate = '" + playerdata.lastcheckdate + "'"
          + ",ChainJoin = " + playerdata.loginStatus.chainLoginDay
          + ",TotalJoin = " + playerdata.loginStatus.totalLoginDay
          + ",LimitedLoginCount = " + playerdata.LimitedLoginCount

          //建築
          + ",build_lv = " + playerdata.buildCount.lv
          + ",build_count = " + playerdata.buildCount.count//.toString()
          + ",build_count_flg = " + playerdata.buildCount.migrationFlag

          //投票
          + ",canVotingFairyUse = " + playerdata.usingVotingFairy
          + ",newVotingFairyTime = '" + playerdata.getVotingFairyStartTimeAsString() + "'"
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

          //loginflagを折る
          + ", loginflag = " + !logoutflag

          + " where uuid like '" + playerUuid + "'")
    }

    stmt.executeUpdate(command)
  }

  private fun executeUpdate(): ActionStatus {
    try {
      //sqlコネクションチェック
      databaseGateway.ensureConnection()

      //同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
      val localStatement = databaseGateway.con.createStatement()
      updatePlayerDataColumns(localStatement)
      updatePlayerMineStack(localStatement)
      updateGridTemplate(localStatement)
      updateSubHome()
      updateActiveSkillEffectUnlockState(localStatement)
      updateActiveSkillPremiumEffectUnlockState(localStatement)
      return Ok
    } catch (exception: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      exception.printStackTrace()
      return Fail
    }

  }

  override fun run() {
    val resultMessage = if (executeUpdate() === Ok)
      ChatColor.GREEN.toString() + playerdata.lowercaseName + "のプレイヤーデータ保存完了"
    else
      ChatColor.RED.toString() + playerdata.lowercaseName + "のプレイヤーデータ保存失敗"
    plugin.server.consoleSender.sendMessage(resultMessage)
    if (!isOnDisable) cancel()
  }
}

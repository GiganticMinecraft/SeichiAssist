package com.github.unchama.buildassist

import java.math.BigDecimal
import java.util.UUID

import com.github.unchama.buildassist.domain.explevel.{BuildAssistExpTable, BuildExpAmount}
import com.github.unchama.seichiassist.{PackagePrivate, SeichiAssist}
import com.github.unchama.seichiassist.data.player.BuildCount
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

final class PlayerData(val player: Player) {
  //初期値を設定

  var name: String = Util.getName(player)
  var uuid: UUID = player.getUniqueId
  var level = 1
  /**
   * トータル設置ブロック数
   */
  var totalbuildnum: BigDecimal = BigDecimal.ZERO
  var flyflag = false
  var flytime = 0
  var endlessfly = false
  var ZoneSetSkillFlag = false
  var zsSkillDirtFlag = false
  // TODO: こいつは殺す
  var AREAint = 2
  /**
   * ブロックを並べるスキル設定フラグ
   */
  var line_up_flg = 0
  var line_up_step_flg = 0
  var line_up_des_flg = 0
  var line_up_minestack_flg = 0
  /**
   * ブロック範囲設置スキル設定フラグ
   */
  var zs_minestack_flag = false

  /**
   * 1分のブロック設置数
   */
  @PackagePrivate private[buildassist] var build_num_1min = BigDecimal.ZERO

  /**
   * プレイヤーレベルを計算し、更新する。
   */
  private[buildassist] def updateLevel(player: Player): Unit = {
    val oldLevel = level
    val newLevel = BuildAssistExpTable.levelAt(BuildExpAmount.ofNonNegative(totalbuildnum))

    for {
      transitionTarget <- (oldLevel + 1) to newLevel.level
      message = {
        if (transitionTarget != BuildAssistExpTable.maxLevel.level)
          s"${GOLD}ﾑﾑｯﾚﾍﾞﾙｱｯﾌﾟ∩( ・ω・)∩【建築Lv(${transitionTarget - 1})→建築Lv($transitionTarget)】"
        else
          s"${GOLD}最大Lvに到達したよ(`･ω･´)"
      }
    } {
      player.sendMessage(message)
    }

    level = newLevel.level
  }

  /**
   * オフラインかどうか
   */
  private[buildassist] def isOffline = Bukkit.getServer.getPlayer(uuid) == null

  /**
   * 建築系データを読み込む
   *
   * @param player
   * @return true:読み込み成功　false:読み込み失敗
   */
  private[buildassist] def buildload(player: Player): Boolean = {
    val playerdata_s = SeichiAssist.playermap.getOrElse(player.getUniqueId, return false)

    totalbuildnum = playerdata_s.buildCount.count
    level = playerdata_s.buildCount.lv
    updateLevel(player)

    true
  }

  /**
   * 建築系データを保存
   */
  def buildsave(player: Player): Unit = {
    val playerData = SeichiAssist.playermap.getOrElse(uuid, {
      player.sendMessage(s"${RED}建築系データ保存失敗しました")
      return
    })

    val oldBuildCount = playerData.buildCount

    //1分制限の判断
    val newBuildCount = {
      if (build_num_1min.doubleValue <= BuildAssist.config.getBuildNum1minLimit) {
        totalbuildnum.add(build_num_1min)
      } else {
        totalbuildnum.add(new BigDecimal(BuildAssist.config.getBuildNum1minLimit))
      }
    }

    playerData.buildCount = BuildCount(level, newBuildCount, oldBuildCount.migrationFlag)
  }
}

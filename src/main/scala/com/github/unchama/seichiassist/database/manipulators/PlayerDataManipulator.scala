package com.github.unchama.seichiassist.database.manipulators

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.database.{DatabaseConstants, DatabaseGateway}
import com.github.unchama.seichiassist.task.PlayerDataLoading
import com.github.unchama.util.ActionStatus
import org.bukkit.Bukkit
import org.bukkit.ChatColor._

import java.sql.SQLException
import java.util.UUID
import scala.collection.mutable

class PlayerDataManipulator(private val gateway: DatabaseGateway) {

  import com.github.unchama.util.syntax.ResultSetSyntax._

  private val tableReference: String =
    s"${gateway.databaseName}.${DatabaseConstants.PLAYERDATA_TABLENAME}"

  // anniversary変更
  def setAnniversary(anniversary: Boolean, uuid: Option[UUID]): Boolean = {
    val command = s"UPDATE $tableReference SET anniversary = $anniversary" +
      uuid.map(u => s" WHERE uuid = '$u'").getOrElse("")

    if (gateway.executeUpdate(command) == ActionStatus.Fail) {
      Bukkit.getLogger.warning("sql failed. => setAnniversary")
      return false
    }
    true
  }

  // TODO IO-nize
  def selectLeaversUUIDs(days: Int): List[UUID] = {
    val command = s"select name, uuid from $tableReference " +
      s"where ((lastquit <= date_sub(curdate(), interval $days day)) " +
      "or (lastquit is null)) and (name != '') and (uuid != '')"
    val uuidList = mutable.ArrayBuffer[UUID]()

    try {
      gateway.executeQuery(command).recordIteration { lrs =>
        try {
          uuidList += UUID.fromString(lrs.getString("uuid"))
        } catch {
          case _: IllegalArgumentException =>
            println(s"不適切なUUID: ${lrs.getString("name")}: ${lrs.getString("uuid")}")
        }
      }
    } catch {
      case e: SQLException =>
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return null
    }
    uuidList.toList
  }

  def loadPlayerData(playerUUID: UUID, playerName: String): PlayerData = {
    val databaseGateway = SeichiAssist.databaseGateway
    val table = DatabaseConstants.PLAYERDATA_TABLENAME
    val db = SeichiAssist.seichiAssistConfig.getDB

    // sqlコネクションチェック
    databaseGateway.ensureConnection()

    // 同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
    val stmt = databaseGateway.con.createStatement()

    val stringUuid: String = playerUUID.toString.toLowerCase()

    // uuidがsqlデータ内に存在するか検索
    val count = {
      val command = s"select count(*) as count from $db.$table where uuid = '$stringUuid'"

      stmt.executeQuery(command).recordIteration(_.getInt("count")).headOption
    }

    count match {
      case Some(0) =>
        // uuidが存在しない時
        SeichiAssist.instance.getLogger.info(s"$YELLOW${playerName}は完全初見です。プレイヤーデータを作成します")

        // 新しくuuidとnameを設定し行を作成
        val command =
          s"insert into $db.$table (name,uuid,loginflag) values('$playerName','$stringUuid','1')"
        stmt.executeUpdate(command)

        new PlayerData(playerUUID, playerName)
      case _ =>
        PlayerDataLoading.loadExistingPlayerData(playerUUID, playerName)
    }
  }
}

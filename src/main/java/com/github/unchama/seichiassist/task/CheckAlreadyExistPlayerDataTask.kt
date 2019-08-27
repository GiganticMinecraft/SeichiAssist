package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.database.DatabaseConstants
import org.bukkit.Bukkit
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.YELLOW
import org.bukkit.scheduler.BukkitRunnable
import java.sql.SQLException
import java.util.*

/**
 * 初見確認とプレイヤーデータのロードを行うタスク(非同期で実行すること)
 * ログイン時に1回のみ処理されることを想定している
 * @author unchama coolpoco
 */
class CheckAlreadyExistPlayerDataTask(private val playerData: PlayerData) : BukkitRunnable() {
  private val databaseGateway = SeichiAssist.databaseGateway
  private val table = DatabaseConstants.PLAYERDATA_TABLENAME
  private val db = SeichiAssist.seichiAssistConfig.db

  private val playermap = SeichiAssist.playermap
  private val name: String = playerData.lowercaseName
  private val uuid: UUID = playerData.uuid

  override fun run() {
    //対象プレイヤーがオフラインなら処理終了
    if (Bukkit.getPlayer(uuid) == null) {
      Bukkit.getConsoleSender().sendMessage("$RED${name}はオフラインの為取得処理を中断")
      return
    }

    //sqlコネクションチェック
    databaseGateway.ensureConnection()

    //同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
    val stmt = databaseGateway.con.createStatement()

    val stringUuid: String = uuid.toString().toLowerCase()

    //uuidがsqlデータ内に存在するか検索
    val count = try {
      val command = ("select count(*) as count from $db.$table where uuid = '$stringUuid'")

      stmt.executeQuery(command).use { resultSet ->
        resultSet.next()
        resultSet.getInt("count")
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      return
    }

    when (count) {
      0 -> {
        //uuidが存在しない時の処理
        SeichiAssist.instance.server.consoleSender.sendMessage("$YELLOW${name}は完全初見です。プレイヤーデータを作成します")

        //新しくuuidとnameを設定し行を作成
        val command = "insert into $db.$table (name,uuid,loginflag) values('$name','$stringUuid','1')"

        try {
          if (stmt!!.executeUpdate(command) <= 0) return
        } catch (e: SQLException) {
          println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
          e.printStackTrace()
          return
        }

        //PlayerDataをplayermapへ格納
        playermap[uuid] = playerData

        //ログイン時init処理
        PlayerDataUpdateOnJoin(playerData).runTaskTimer(SeichiAssist.instance, 0, 20)
      }
      else -> {
        //uuidが存在するときの処理
        //非同期でPlayerDataの読み込みを行う
        PlayerDataLoadTask(playerData).runTaskTimerAsynchronously(SeichiAssist.instance, 0, 20)
        PlayerDataUpdateOnJoin(playerData).runTaskTimer(SeichiAssist.instance, 0, 20)
      }
    }
  }
}

package com.github.unchama.seichiassist.database.manipulators

import cats.data.EitherT
import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.ResponseEffectOrResult
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.RankData
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.database.{DatabaseConstants, DatabaseGateway}
import com.github.unchama.seichiassist.task.{CoolDownTask, PlayerDataLoading}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.util.ActionStatus
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.{Calendar, UUID}
import scala.collection.mutable

class PlayerDataManipulator(private val gateway: DatabaseGateway) {

  import com.github.unchama.util.syntax.ResultSetSyntax._

  private val plugin = SeichiAssist.instance

  private val tableReference: String =
    s"${gateway.databaseName}.${DatabaseConstants.PLAYERDATA_TABLENAME}"

  // 投票特典配布時の処理(p_givenvoteの値の更新もココ)
  def compareVotePoint(player: Player, playerdata: PlayerData): Int = {
    ifCoolDownDoneThenGet(player, playerdata) {
      val struuid = playerdata.uuid.toString

      var p_vote = 0
      var p_givenvote = 0

      var command = s"select p_vote,p_givenvote from $tableReference where uuid = '$struuid'"
      try {
        gateway.executeQuery(command).recordIteration { lrs =>
          p_vote = lrs.getInt("p_vote")
          p_givenvote = lrs.getInt("p_givenvote")
        }
      } catch {
        case e: SQLException =>
          println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
          e.printStackTrace()
          player.sendMessage(RED.toString + "投票特典の受け取りに失敗しました")
          return 0
      }

      // 比較して差があればその差の値を返す(同時にp_givenvoteも更新しておく)
      if (p_vote > p_givenvote) {
        command = ("update " + tableReference
          + " set p_givenvote = " + p_vote
          + s" where uuid = '$struuid'")
        if (gateway.executeUpdate(command) == ActionStatus.Fail) {
          player.sendMessage(RED.toString + "投票特典の受け取りに失敗しました")
          return 0
        }

        return p_vote - p_givenvote
      }
      player.sendMessage(YELLOW.toString + "投票特典は全て受け取り済みのようです")
      0
    }
  }

  @inline private def ifCoolDownDoneThenGet(player: Player, playerdata: PlayerData)(
    supplier: => Int
  ): Int = {
    // 連打による負荷防止の為クールダウン処理
    if (!playerdata.votecooldownflag) {
      player.sendMessage(RED.toString + "しばらく待ってからやり直してください")
      return 0
    }
    new CoolDownTask(player, true, false).runTaskLater(plugin, 1200)

    supplier
  }

  /**
   * 投票ポイントをインクリメントするメソッド。
   *
   * @param playerName
   *   プレーヤー名
   */
  def incrementVotePoint(playerName: String): Unit = {
    DB.localTx { implicit session =>
      sql"update playerdata set p_vote = p_vote + 1 where name = $playerName".update().apply()
    }
  }

  def addChainVote(name: String): Unit =
    DB.localTx { implicit session =>
      val calendar = Calendar.getInstance()
      val dateFormat = new SimpleDateFormat("yyyy/MM/dd")

      val lastVote =
        sql"SELECT lastvote FROM playerdata WHERE name = $name"
          .map(_.string("lastvote"))
          .single()
          .apply()
          .getOrElse(dateFormat.format(calendar.getTime))

      sql"UPDATE playerdata SET lastvote = ${dateFormat.format(calendar.getTime)} WHERE name = $name"
        .update()
        .apply()

      val TodayDate = dateFormat.parse(dateFormat.format(calendar.getTime))
      val LastDate = dateFormat.parse(lastVote)
      val TodayLong = TodayDate.getTime
      val LastLong = LastDate.getTime

      val dateDiff = (TodayLong - LastLong) / (1000 * 60 * 60 * 24)
      val shouldIncrementChainVote = dateDiff <= 4L

      val newCount = if (shouldIncrementChainVote) {
        sql"""select chainvote from playerdata where name = $name"""
          .map(_.int("chainvote"))
          .first()
          .apply()
          .get + 1
      } else 1

      sql"""update playerdata set chainvote = $newCount where name = $name"""
    }

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

  /**
   * 全ランキングリストの更新処理
   *
   * @return
   *   成否…true: 成功、false: 失敗 TODO この処理はDB上と通信を行う為非同期にすべき
   */
  def successRankingUpdate(): Boolean = {
    if (!successPlayTickRankingUpdate()) return false
    if (!successVoteRankingUpdate()) return false
    successAppleNumberRankingUpdate()
  }

  // ランキング表示用にプレイ時間のカラムだけ全員分引っ張る
  private def successPlayTickRankingUpdate(): Boolean = {
    val ranklist = mutable.ArrayBuffer[RankData]()
    val command = ("select name,playtick from " + tableReference
      + " order by playtick desc")
    try {
      gateway.executeQuery(command).recordIteration { lrs =>
        val rankdata = new RankData()
        rankdata.name = lrs.getString("name")
        rankdata.playtick = lrs.getLong("playtick")
        ranklist += rankdata
      }
    } catch {
      case e: SQLException =>
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return false
    }

    SeichiAssist.ranklist_playtick.clear()
    SeichiAssist.ranklist_playtick.addAll(ranklist)
    true
  }

  // ランキング表示用に投票数のカラムだけ全員分引っ張る
  private def successVoteRankingUpdate(): Boolean = {
    val ranklist = mutable.ArrayBuffer[RankData]()
    val command = ("select name,p_vote from " + tableReference
      + " order by p_vote desc")
    try {
      gateway.executeQuery(command).recordIteration { lrs =>
        val rankdata = new RankData()
        rankdata.name = lrs.getString("name")
        rankdata.p_vote = lrs.getInt("p_vote")
        ranklist += rankdata
      }
    } catch {
      case e: SQLException =>
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return false
    }

    SeichiAssist.ranklist_p_vote.clear()
    SeichiAssist.ranklist_p_vote.addAll(ranklist)
    true
  }

  // ランキング表示用に上げたりんご数のカラムだけ全員分引っ張る
  private def successAppleNumberRankingUpdate(): Boolean = {
    val ranklist = mutable.ArrayBuffer[RankData]()
    SeichiAssist.allplayergiveapplelong = 0
    val command = s"select name,p_apple from $tableReference order by p_apple desc"
    try {
      gateway.executeQuery(command).recordIteration { lrs =>
        val rankdata = new RankData()
        rankdata.name = lrs.getString("name")
        rankdata.p_apple = lrs.getInt("p_apple")
        ranklist += rankdata
        SeichiAssist.allplayergiveapplelong += rankdata.p_apple.toLong
      }
    } catch {
      case e: SQLException =>
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return false
    }

    SeichiAssist.ranklist_p_apple.clear()
    SeichiAssist.ranklist_p_apple.addAll(ranklist)
    true
  }

  private def catchingDatabaseErrors[R](
    targetName: String,
    program: IO[Either[TargetedEffect[CommandSender], R]]
  ): IO[Either[TargetedEffect[CommandSender], R]] = {
    program.attempt.flatMap {
      case Left(error) =>
        IO {
          Bukkit.getLogger.warning(s"database failure for $targetName.")
          error.printStackTrace()

          Left(MessageEffect(s"${RED}データベースアクセスに失敗しました。"))
        }
      case Right(result) => IO.pure(result)
    }
  }

  def inquireLastQuitOf(playerName: String): IO[TargetedEffect[CommandSender]] = {
    val fetchLastQuitData: IO[ResponseEffectOrResult[CommandSender, String]] = EitherT
      .right(IO {
        import scalikejdbc._
        DB.readOnly { implicit session =>
          sql"""select lastquit from playerdata where name = $playerName"""
            .map(rs => rs.string("lastquit"))
            .single()
            .apply()
        }.get
      })
      .value

    catchingDatabaseErrors(playerName, fetchLastQuitData).map {
      case Left(errorEffect) =>
        import com.github.unchama.generic.syntax._

        val messages = List(
          s"${RED}最終ログアウト日時の照会に失敗しました。",
          s"${RED}プレイヤー名が変更されていないか確認してください。",
          s"${RED}プレイヤー名が正しいのにこのエラーが出る場合、最終ログイン時間が古い可能性があります。"
        )

        errorEffect.followedBy(MessageEffect(messages))
      case Right(lastQuit) =>
        MessageEffect(s"${playerName}の最終ログアウト日時：$lastQuit")
    }
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

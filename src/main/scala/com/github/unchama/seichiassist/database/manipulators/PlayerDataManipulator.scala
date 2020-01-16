package com.github.unchama.seichiassist.database.manipulators

import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.{Calendar, UUID}

import cats.data.EitherT
import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.TypeAliases.ResponseEffectOrResult
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.RankData
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.database.{DatabaseConstants, DatabaseGateway}
import com.github.unchama.seichiassist.task.{CoolDownTask, PlayerDataLoading}
import com.github.unchama.seichiassist.util.{BukkitSerialization, Util}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.util.ActionStatus
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

import scala.collection.mutable

class PlayerDataManipulator(private val gateway: DatabaseGateway) {

  import com.github.unchama.targetedeffect.syntax._
  import com.github.unchama.util.syntax.ResultSetSyntax._

  private val plugin = SeichiAssist.instance

  private val tableReference: String = s"${gateway.databaseName}.${DatabaseConstants.PLAYERDATA_TABLENAME}"

  //投票特典配布時の処理(p_givenvoteの値の更新もココ)
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

      //比較して差があればその差の値を返す(同時にp_givenvoteも更新しておく)
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

  //最新のnumofsorryforbug値を返してmysqlのnumofsorrybug値を初期化する処理
  def givePlayerBug(player: Player, playerdata: PlayerData): Int = {
    ifCoolDownDoneThenGet(player, playerdata) {
      val struuid = playerdata.uuid.toString
      var numofsorryforbug = 0

      var command = s"select numofsorryforbug from $tableReference where uuid = '$struuid'"
      try {
        gateway.executeQuery(command).recordIteration { lrs =>
          numofsorryforbug = lrs.getInt("numofsorryforbug")
        }
      } catch {
        case e: SQLException =>
          println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
          e.printStackTrace()
          player.sendMessage(RED.toString + "ガチャ券の受け取りに失敗しました")
          return 0
      }

      if (numofsorryforbug > 576) {
        // 576より多い場合はその値を返す(同時にnumofsorryforbugから-576)
        command = ("update " + tableReference
          + " set numofsorryforbug = numofsorryforbug - 576"
          + s" where uuid = '$struuid'")
        if (gateway.executeUpdate(command) == ActionStatus.Fail) {
          player.sendMessage(RED.toString + "ガチャ券の受け取りに失敗しました")
          return 0
        }

        return 576
      } else if (numofsorryforbug > 0) {
        // 0より多い場合はその値を返す(同時にnumofsorryforbug初期化)
        command = ("update " + tableReference
          + " set numofsorryforbug = 0"
          + s" where uuid = '$struuid'")
        if (gateway.executeUpdate(command) == ActionStatus.Fail) {
          player.sendMessage(RED.toString + "ガチャ券の受け取りに失敗しました")
          return 0
        }

        return numofsorryforbug
      }

      player.sendMessage(YELLOW.toString + "ガチャ券は全て受け取り済みのようです")
      0
    }
  }

  @inline private def ifCoolDownDoneThenGet(player: Player, playerdata: PlayerData)(supplier: => Int): Int = {
    //連打による負荷防止の為クールダウン処理
    if (!playerdata.votecooldownflag) {
      player.sendMessage(RED.toString + "しばらく待ってからやり直してください")
      return 0
    }
    new CoolDownTask(player, true, false, false).runTaskLater(plugin, 1200)

    supplier
  }

  /**
   * 投票ポイントをインクリメントするメソッド。
   *
   * @param playerName プレーヤー名
   * @return 処理の成否
   */
  def incrementVotePoint(playerName: String): ActionStatus = {
    val command = ("update " + tableReference
      + " set p_vote = p_vote + 1" //1加算

      + s" where name = '$playerName'")

    gateway.executeUpdate(command)
  }

  /**
   * プレミアムエフェクトポイントを加算するメソッド。
   *
   * @param playerName プレーヤーネーム
   * @param num        足す整数
   * @return 処理の成否
   */
  def addPremiumEffectPoint(playerName: String, num: Int): ActionStatus = {
    val command = ("update " + tableReference
      + " set premiumeffectpoint = premiumeffectpoint + " + num //引数で来たポイント数分加算

      + s" where name = '$playerName'")

    gateway.executeUpdate(command)
  }


  //指定されたプレイヤーにガチャ券を送信する
  def addPlayerBug(playerName: String, num: Int): ActionStatus = {
    val command = ("update " + tableReference
      + " set numofsorryforbug = numofsorryforbug + " + num
      + s" where name = '$playerName'")

    gateway.executeUpdate(command)
  }

  def addChainVote(name: String): Boolean = {
    val calendar = Calendar.getInstance()
    val dateFormat = new SimpleDateFormat("yyyy/MM/dd")
    var lastVote: String = null

    try {
      val readLastVote = gateway.executeQuery(s"SELECT lastvote FROM $tableReference WHERE name = '$name'")
        .recordIteration { lrs =>
          lrs.getString("lastvote")
        }.headOption.getOrElse(return false)

      lastVote =
        if (readLastVote == null || readLastVote == "")
          dateFormat.format(calendar.getTime)
        else
          readLastVote

      val update = s"UPDATE $tableReference  SET lastvote = '${dateFormat.format(calendar.getTime)}' WHERE name = '$name'"

      gateway.executeUpdate(update)
    } catch {
      case e: SQLException =>
        Bukkit.getLogger.warning(s"${Util.getName(name)} sql failed. => lastvote")
        e.printStackTrace()
        return false
    }

    try {
      gateway.executeQuery(s"SELECT chainvote FROM $tableReference WHERE name = '$name'")
        .recordIteration { lrs =>
          val TodayDate = dateFormat.parse(dateFormat.format(calendar.getTime))
          val LastDate = dateFormat.parse(lastVote)
          val TodayLong = TodayDate.getTime
          val LastLong = LastDate.getTime

          val dateDiff = (TodayLong - LastLong) / (1000 * 60 * 60 * 24)
          val count =
            if (dateDiff <= 2L)
              lrs.getInt("chainvote") + 1
            else
              1

          //プレイヤーがオンラインの時即時反映させる
          val player = Bukkit.getServer.getPlayer(name)
          if (player != null) {
            val playerData = SeichiAssist.playermap(player.getUniqueId)

            playerData.ChainVote = count
          }

          (count, name)
        }
        .map { case (count, name) => s"UPDATE $tableReference SET chainvote = $count WHERE name = '$name'" }
        .foreach(gateway.executeUpdate)
    } catch {
      case e: SQLException =>
        Bukkit.getLogger.warning(Util.getName(name) + " sql failed. => chainvote")
        e.printStackTrace()
        return false
    }

    true
  }

  def addContributionPoint(targetPlayerName: String, point: Int): IO[ResponseEffectOrResult[CommandSender, Unit]] = {
    val executeUpdate: IO[ResponseEffectOrResult[CommandSender, Unit]] = IO {
      val updateCommand = s"UPDATE $tableReference SET contribute_point = contribute_point + $point WHERE name = '$targetPlayerName'"

      if (gateway.executeUpdate(updateCommand) == ActionStatus.Fail) {
        Bukkit.getLogger.warning(s"sql failed on updating $targetPlayerName's contribute_point")
        Left(s"${RED}貢献度ptの変更に失敗しました。".asMessageEffect())
      } else {
        Right(())
      }
    }

    val updatePlayerDataMemoryCache: IO[Unit] = IO {
      val targetPlayer = Bukkit.getServer.getPlayer(targetPlayerName)
      if (targetPlayer != null) {
        val targetPlayerData = SeichiAssist.playermap(targetPlayer.getUniqueId)

        targetPlayerData.contribute_point += point
        targetPlayerData.setContributionPoint(point)
      }
    }

    for {
      _ <- EitherT(assertPlayerDataExistenceFor(targetPlayerName))
      _ <- EitherT(executeUpdate)
      _ <- EitherT.right[TargetedEffect[CommandSender]](updatePlayerDataMemoryCache)
    } yield ()
    }.value

  private def assertPlayerDataExistenceFor(playerName: String): IO[ResponseEffectOrResult[CommandSender, Unit]] =
    IO {
      try {
        // TODO: 本当にStarSelectじゃなきゃだめ?
        val resultSet = gateway.executeQuery(s"select * from $tableReference where name = $playerName")

        if (!resultSet.next()) {
          Left(s"$RED$playerName はデータベースに登録されていません。".asMessageEffect())
        } else {
          Right(())
        }
      } catch {
        case e: SQLException =>
          Bukkit.getLogger.warning(s"sql failed on checking data existence of $playerName")
          e.printStackTrace()

          Left(s"${RED}プレーヤーデータへのアクセスに失敗しました。".asMessageEffect())
      }
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

  // TODO remove `playerData` from argument
  def saveSharedInventory(player: Player, playerData: PlayerData, serializedInventory: String): IO[ResponseEffectOrResult[Player, Unit]] = {
    val assertSharedInventoryBeEmpty: EitherT[IO, TargetedEffect[CommandSender], Unit] =
      for {
        sharedInventorySerialized <- EitherT(loadShareInv(player, playerData))
        _ <- EitherT.fromEither[IO] {
          if (sharedInventorySerialized != null && sharedInventorySerialized != "")
            Left(s"${RED}既にアイテムが収納されています".asMessageEffect())
          else
            Right(())
        }
      } yield ()

    val writeInventoryData = IO {
      // シリアル化されたインベントリデータを書き込む
      val updateCommand = s"UPDATE $tableReference SET shareinv = '$serializedInventory' WHERE uuid = '${player.getUniqueId}'"

      if (gateway.executeUpdate(updateCommand) == ActionStatus.Fail) {
        Bukkit.getLogger.warning(s"${player.getName} database failure.")
        Left(s"$${RED}アイテムの収納に失敗しました".asMessageEffect())
      } else {
        Right(())
      }
    }

    for {
      _ <- EitherT(checkInventoryOperationCoolDown(player))
      _ <- assertSharedInventoryBeEmpty
      _ <- EitherT(writeInventoryData)
    } yield ()
    }.value

  def loadShareInv(player: Player, playerData: PlayerData): IO[ResponseEffectOrResult[CommandSender, String]] = {
    val loadInventoryData: IO[Either[Nothing, String]] = EitherT.right(IO {
      val command = s"SELECT shareinv FROM $tableReference WHERE uuid = '${player.getUniqueId}'"

      gateway.executeQuery(command).recordIteration(_.getString("shareinv")).headOption.get
    }).value

    for {
      _ <- EitherT(checkInventoryOperationCoolDown(player))
      serializedInventory <- EitherT(catchingDatabaseErrors(player.getName, loadInventoryData))
    } yield serializedInventory
    }.value

  private def catchingDatabaseErrors[R](targetName: String,
                                        program: IO[Either[TargetedEffect[CommandSender], R]]): IO[Either[TargetedEffect[CommandSender], R]] = {
    program.attempt.flatMap {
      case Left(error) => IO {
        Bukkit.getLogger.warning(s"database failure for $targetName.")
        error.printStackTrace()

        Left(s"${RED}データベースアクセスに失敗しました。".asMessageEffect())
      }
      case Right(result) => IO.pure(result)
    }
  }

  private def checkInventoryOperationCoolDown(player: Player): IO[Either[TargetedEffect[CommandSender], Unit]] = {
    val playerData = SeichiAssist.playermap(player.getUniqueId)
    IO {
      //連打による負荷防止
      if (!playerData.shareinvcooldownflag)
        Left(s"${RED}しばらく待ってからやり直してください".asMessageEffect())
      else {
        new CoolDownTask(player, CoolDownTask.SHAREINV).runTaskLater(plugin, 200)
        Right(())
      }
    }
  }

  def clearShareInv(player: Player, playerdata: PlayerData): IO[ResponseEffectOrResult[CommandSender, Unit]] = IO {
    val command = s"UPDATE $tableReference SET shareinv = '' WHERE uuid = '${playerdata.uuid}'"

    if (gateway.executeUpdate(command) == ActionStatus.Fail) {
      Bukkit.getLogger.warning(s"${player.getName} sql failed. => clearShareInv")
      Left(s"${RED}アイテムのクリアに失敗しました".asMessageEffect())
    } else
      Right(())
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
   * @return 成否…true: 成功、false: 失敗
   *         TODO この処理はDB上と通信を行う為非同期にすべき
   */
  def successRankingUpdate(): Boolean = {
    if (!successBlockRankingUpdate()) return false
    if (!successPlayTickRankingUpdate()) return false
    if (!successVoteRankingUpdate()) return false
    if (!successPremiumEffectPointRanking()) return false
    successAppleNumberRankingUpdate()

  }

  //ランキング表示用に総破壊ブロック数のカラムだけ全員分引っ張る
  private def successBlockRankingUpdate(): Boolean = {
    val ranklist = mutable.ArrayBuffer[RankData]()
    SeichiAssist.allplayerbreakblockint = 0
    val command = ("select name,level,totalbreaknum from " + tableReference
      + " order by totalbreaknum desc")
    try {
      gateway.executeQuery(command).recordIteration { lrs =>
        val rankdata = new RankData()
        rankdata.name = lrs.getString("name")
        rankdata.level = lrs.getInt("level")
        rankdata.totalbreaknum = lrs.getLong("totalbreaknum")
        ranklist += rankdata
        SeichiAssist.allplayerbreakblockint += rankdata.totalbreaknum
      }
    } catch {
      case e: SQLException =>
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return false
    }

    SeichiAssist.ranklist.clear()
    SeichiAssist.ranklist.addAll(ranklist)
    true
  }

  //ランキング表示用にプレイ時間のカラムだけ全員分引っ張る
  private def successPlayTickRankingUpdate(): Boolean = {
    val ranklist = mutable.ArrayBuffer[RankData]()
    val command = ("select name,playtick from " + tableReference
      + " order by playtick desc")
    try {
      gateway.executeQuery(command).recordIteration { lrs =>
        val rankdata = new RankData()
        rankdata.name = lrs.getString("name")
        rankdata.playtick = lrs.getInt("playtick")
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

  //ランキング表示用に投票数のカラムだけ全員分引っ張る
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

  //ランキング表示用にプレミアムエフェクトポイントのカラムだけ全員分引っ張る
  private def successPremiumEffectPointRanking(): Boolean = {
    val ranklist = mutable.ArrayBuffer[RankData]()
    val command = ("select name,premiumeffectpoint from " + tableReference
      + " order by premiumeffectpoint desc")
    try {
      gateway.executeQuery(command).recordIteration { lrs =>
        val rankdata = new RankData()
        rankdata.name = lrs.getString("name")
        rankdata.premiumeffectpoint = lrs.getInt("premiumeffectpoint")
        ranklist += rankdata
      }
    } catch {
      case e: SQLException =>
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return false
    }

    SeichiAssist.ranklist_premiumeffectpoint.clear()
    SeichiAssist.ranklist_premiumeffectpoint.addAll(ranklist)
    true
  }

  //ランキング表示用に上げたりんご数のカラムだけ全員分引っ張る
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

  //全員に詫びガチャの配布
  def addAllPlayerBug(amount: Int): ActionStatus = {
    val command = s"update $tableReference set numofsorryforbug = numofsorryforbug + $amount"
    gateway.executeUpdate(command)
  }

  /**
   * プレイヤーのガチャ券枚数を変更します
   * @param playerName プレイヤーの名前
   * @param amount 変更後のプレーヤーのガチャ券の枚数
   * @return [ActionStatus]
   */
  def changeGachaAmountOf(playerName: String, amount: Int): ActionStatus = {
    val command = s"update $tableReference set gachapoint = ${1000 * amount} where name = '$playerName'"
    gateway.executeUpdate(command)
  }

  def selectPocketInventoryOf(uuid: UUID): IO[ResponseEffectOrResult[CommandSender, Inventory]] = {
    val command = s"select inventory from $tableReference where uuid = '$uuid'"

    val executeQuery = IO {
      gateway.executeQuery(command).recordIteration { lrs =>
        BukkitSerialization.fromBase64(lrs.getString("inventory"))
      }.head
    }

    catchingDatabaseErrors(uuid.toString, EitherT.right(executeQuery).value)
  }

  def inquireLastQuitOf(playerName: String): IO[TargetedEffect[CommandSender]] = {
    val fetchLastQuitData: IO[ResponseEffectOrResult[CommandSender, String]] = EitherT.right(IO {
      val command = s"select lastquit from $tableReference where name = '$playerName'"

      gateway.executeQuery(command)
        .recordIteration(_.getString("lastquit"))
        .head
    }).value

    catchingDatabaseErrors(playerName, fetchLastQuitData).map {
      case Left(errorEffect) =>
        import com.github.unchama.generic.syntax._

        val messages = List(
          s"${RED}最終ログアウト日時の照会に失敗しました。",
          s"${RED}プレイヤー名やプレイヤー名が変更されていないか確認してください。",
          s"${RED}プレイヤー名が正しいのにこのエラーが出る場合、最終ログイン時間が古い可能性があります。"
        )

        errorEffect.followedBy(messages.asMessageEffect())
      case Right(lastQuit) =>
        s"${playerName}の最終ログアウト日時：$lastQuit".asMessageEffect()
    }
  }

  def loadPlayerData(playerUUID: UUID, playerName: String): PlayerData = {
    val databaseGateway = SeichiAssist.databaseGateway
    val table = DatabaseConstants.PLAYERDATA_TABLENAME
    val db = SeichiAssist.seichiAssistConfig.getDB

    //sqlコネクションチェック
    databaseGateway.ensureConnection()

    //同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
    val stmt = databaseGateway.con.createStatement()

    val stringUuid: String = playerUUID.toString.toLowerCase()

    //uuidがsqlデータ内に存在するか検索
    val count = {
      val command = s"select count(*) as count from $db.$table where uuid = '$stringUuid'"

      stmt.executeQuery(command).recordIteration(_.getInt("count")).headOption
    }

    count match {
      case Some(0) =>
        //uuidが存在しない時
        SeichiAssist.instance.getLogger.info(s"$YELLOW${playerName}は完全初見です。プレイヤーデータを作成します")

        //新しくuuidとnameを設定し行を作成
        val command = s"insert into $db.$table (name,uuid,loginflag) values('$playerName','$stringUuid','1')"
        stmt.executeUpdate(command)

        new PlayerData(playerUUID, playerName)
      case _ =>
        PlayerDataLoading.loadExistingPlayerData(playerUUID, playerName)
    }
  }
}

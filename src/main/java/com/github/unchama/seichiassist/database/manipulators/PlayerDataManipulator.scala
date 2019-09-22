package com.github.unchama.seichiassist.database.manipulators

import java.sql.SQLException
import java.util
import java.util.{Calendar, UUID}

import com.github.unchama.contextualexecutor.builder.TypeAliases.ResponseEffectOrResult
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.RankData
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.database.{DatabaseConstants, DatabaseGateway}
import com.github.unchama.seichiassist.util.{BukkitSerialization, Util}
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.util.ActionStatus
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import kotlin.Suppress
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.junit.internal.runners.statements.Fail

class PlayerDataManipulator(private val gateway: DatabaseGateway) {
  private val plugin = SeichiAssist.instance

  private val tableReference: String = gateway.databaseName + "." + DatabaseConstants.PLAYERDATA_TABLENAME

  private @inline def ifCoolDownDoneThenGet(player: Player,
                                           playerdata: PlayerData,
                                           supplier: () => Int): Int = {
    //連打による負荷防止の為クールダウン処理
    if (!playerdata.votecooldownflag) {
      player.sendMessage(RED.toString() + "しばらく待ってからやり直してください")
      return 0
    }
    CoolDownTask(player, true, false, false).runTaskLater(plugin, 1200)

    return supplier()
  }

  //投票特典配布時の処理(p_givenvoteの値の更新もココ)
  def compareVotePoint(player: Player, playerdata: PlayerData): Int = {
    return ifCoolDownDoneThenGet(player, playerdata) {
      val struuid = playerdata.uuid.toString()

      var p_vote = 0
      var p_givenvote = 0

      var command = s"select p_vote,p_givenvote from $tableReference where uuid = '$struuid'"
      try {
        gateway.executeQuery(command).recordIteration {
          p_vote = getInt("p_vote")
          p_givenvote = getInt("p_givenvote")
        }
      } catch (e: SQLException) {
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        player.sendMessage(RED.toString() + "投票特典の受け取りに失敗しました")
        return@ifCoolDownDoneThenGet 0
      }

      //比較して差があればその差の値を返す(同時にp_givenvoteも更新しておく)
      if (p_vote > p_givenvote) {
        command = ("update " + tableReference
            + " set p_givenvote = " + p_vote
            + " where uuid like '" + struuid + "'")
        if (gateway.executeUpdate(command) === Fail) {
          player.sendMessage(RED.toString() + "投票特典の受け取りに失敗しました")
          return@ifCoolDownDoneThenGet 0
        }

        return p_vote - p_givenvote
      }
      player.sendMessage(YELLOW.toString() + "投票特典は全て受け取り済みのようです")
      0
    }
  }

  //最新のnumofsorryforbug値を返してmysqlのnumofsorrybug値を初期化する処理
  def givePlayerBug(player: Player, playerdata: PlayerData): Int = {
    return ifCoolDownDoneThenGet(player, playerdata) {
      val struuid = playerdata.uuid.toString()
      var numofsorryforbug = 0

      var command = s"select numofsorryforbug from $tableReference where uuid = '$struuid'"
      try {
        gateway.executeQuery(command).recordIteration {
          numofsorryforbug = getInt("numofsorryforbug")
        }
      } catch (e: SQLException) {
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        player.sendMessage(RED.toString() + "ガチャ券の受け取りに失敗しました")
        return@ifCoolDownDoneThenGet 0
      }

      if (numofsorryforbug > 576) {
        // 576より多い場合はその値を返す(同時にnumofsorryforbugから-576)
        command = ("update " + tableReference
            + " set numofsorryforbug = numofsorryforbug - 576"
            + " where uuid like '" + struuid + "'")
        if (gateway.executeUpdate(command) === Fail) {
          player.sendMessage(RED.toString() + "ガチャ券の受け取りに失敗しました")
          return@ifCoolDownDoneThenGet 0
        }

        return@ifCoolDownDoneThenGet 576
      } else if (numofsorryforbug > 0) {
        // 0より多い場合はその値を返す(同時にnumofsorryforbug初期化)
        command = ("update " + tableReference
            + " set numofsorryforbug = 0"
            + " where uuid like '" + struuid + "'")
        if (gateway.executeUpdate(command) === Fail) {
          player.sendMessage(RED.toString() + "ガチャ券の受け取りに失敗しました")
          return@ifCoolDownDoneThenGet 0
        }

        return@ifCoolDownDoneThenGet numofsorryforbug
      }

      player.sendMessage(YELLOW.toString() + "ガチャ券は全て受け取り済みのようです")
      0
    }
  }

  /**
   * 投票ポイントをインクリメントするメソッド。
   * @param playerName プレーヤー名
   * @return 処理の成否
   */
  def incrementVotePoint(playerName: String): ActionStatus = {
    val command = ("update " + tableReference
        + " set p_vote = p_vote + 1" //1加算

        + " where name like '" + playerName + "'")

    return gateway.executeUpdate(command)
  }

  /**
   * プレミアムエフェクトポイントを加算するメソッド。
   * @param playerName プレーヤーネーム
   * @param num 足す整数
   * @return 処理の成否
   */
  def addPremiumEffectPoint(playerName: String, num: Int): ActionStatus = {
    val command = ("update " + tableReference
        + " set premiumeffectpoint = premiumeffectpoint + " + num //引数で来たポイント数分加算

        + " where name like '" + playerName + "'")

    return gateway.executeUpdate(command)
  }


  //指定されたプレイヤーにガチャ券を送信する
  def addPlayerBug(playerName: String, num: Int): ActionStatus = {
    val command = ("update " + tableReference
        + " set numofsorryforbug = numofsorryforbug + " + num
        + " where name like '" + playerName + "'")

    return gateway.executeUpdate(command)
  }

  def addChainVote(name: String): Boolean = {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy/MM/dd")
    val lastVote: String

    try {
      val readLastVote = gateway.executeQuery(s"SELECT lastvote FROM $tableReference WHERE name LIKE '$name'").use { lrs =>
        // 初回のnextがnull→データが1件も無い場合
        if (!lrs.next()) return false

        lrs.getString("lastvote")
      }

      lastVote =
          if (readLastVote == null || readLastVote == "")
            dateFormat.format(calendar.time)
          else
            readLastVote

      val update = s"UPDATE $tableReference  SET lastvote = '${dateFormat.format(calendar.time)}' WHERE name LIKE '$name'"

      gateway.executeUpdate(update)
    } catch (e: SQLException) {
      Bukkit.getLogger().warning(s"${Util.name(name)} sql failed. => lastvote")
      e.printStackTrace()
      return false
    }

    try {
      gateway.executeQuery(s"SELECT chainvote FROM $tableReference WHERE name LIKE '$name'").use { lrs =>
        // 初回のnextがnull→データが1件も無い場合
        if (!lrs.next()) return false

        try {
          val TodayDate = dateFormat.parse(dateFormat.format(calendar.time))
          val LastDate = dateFormat.parse(lastVote)
          val TodayLong = TodayDate.time
          val LastLong = LastDate.time

          val dateDiff = (TodayLong - LastLong) / (1000 * 60 * 60 * 24)
          val count =
              if (dateDiff <= 2L)
                lrs.getInt("chainvote") + 1
              else
                1

          //プレイヤーがオンラインの時即時反映させる
          Bukkit.getServer().getPlayer(name)?.let { player =>
            val playerData = SeichiAssist.playermap(player.uniqueId)

            playerData.ChainVote = count
          }

          gateway.executeUpdate(s"UPDATE $tableReference SET chainvote = $count WHERE name LIKE '$name'")
        } catch (e: ParseException) {
          e.printStackTrace()
        }
      }
    } catch (e: SQLException) {
      Bukkit.getLogger().warning(Util.name(name) + " sql failed. => chainvote")
      e.printStackTrace()
      return false
    }

    return true
  }

  @Suppress("RedundantSuspendModifier")
  private @SuspendingMethod def assertPlayerDataExistenceFor(playerName: String): ResponseEffectOrResult[CommandSender, Unit] =
      try {
        gateway.executeQuery(s"select * from $tableReference where name like $playerName").use { resultSet =>
          if (!resultSet.next()) {
            s"${RED}$playerName はデータベースに登録されていません。".asMessageEffect().left()
          } else {
            Unit.right()
          }
        }
      } catch (e: SQLException) {
        Bukkit.getLogger().warning(s"sql failed on checking data existence of $playerName")
        e.printStackTrace()

        s"${RED}プレーヤーデータへのアクセスに失敗しました。".asMessageEffect().left()
      }

  @SuspendingMethod def addContributionPoint(targetPlayerName: String, point: Int): ResponseEffectOrResult[CommandSender, Unit] = {
    @Suppress("RedundantSuspendModifier")
    @SuspendingMethod def executeUpdate(): ResponseEffectOrResult[CommandSender, Unit] = {
      val updateCommand = s"UPDATE $tableReference SET contribute_point = contribute_point + $point WHERE name LIKE '$targetPlayerName'"

      if (gateway.executeUpdate(updateCommand) === Fail) {
        Bukkit.getLogger().warning(s"sql failed on updating $targetPlayerName's contribute_point")
      s"${RED}貢献度ptの変更に失敗しました。".asMessageEffect ().left ()
      } else {
        Unit.right()
      }
    }

    @Suppress("RedundantSuspendModifier")
    @SuspendingMethod def updatePlayerDataMemoryCache() {
      val targetPlayer = Bukkit.getServer.getPlayer(targetPlayerName).ifNull(return)
      val targetPlayerData = SeichiAssist.playermap(targetPlayer.getUniqueId).ifNull(return)

      targetPlayerData.contribute_point += point
      targetPlayerData.setContributionPoint(point)
    }

    return assertPlayerDataExistenceFor(targetPlayerName)
        .flatMap { executeUpdate() }
        .map { updatePlayerDataMemoryCache() }
  }

  // anniversary変更
  def setAnniversary(anniversary: Boolean, uuid: UUID?): Boolean = {
    var command = s"UPDATE $tableReference SET anniversary = $anniversary"
    if (uuid != null) {
      command += s" WHERE uuid = '$uuid'"
    }
    if (gateway.executeUpdate(command) === Fail) {
      Bukkit.getLogger().warning("sql failed. => setAnniversary")
      return false
    }
    return true
  }


  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def saveSharedInventory(player: Player, playerData: PlayerData, serializedInventory: String): ResponseEffectOrResult[CommandSender, Unit] = {
    //連打による負荷防止の為クールダウン処理
    if (!playerData.shareinvcooldownflag) {
    return s"${RED}しばらく待ってからやり直してください".asMessageEffect ().left ()
    }
    CoolDownTask(player, CoolDownTask.SHAREINV).runTaskLater(plugin, 200)

    try {
      // 共有インベントリに既にアイテムが格納されていないことを確認する
      val selectCommand = s"SELECT shareinv FROM $tableReference WHERE uuid = '${playerData.uuid}'"
      gateway.executeQuery(selectCommand).use { lrs =>
        lrs.next()
        val sharedInventorySerial = lrs.getString("shareinv")
        if (sharedInventorySerial != null && sharedInventorySerial != "") {
    return s"${RED}既にアイテムが収納されています".asMessageEffect ().left ()
        }
      }

      // シリアル化されたインベントリデータを書き込む
      val updateCommand = s"UPDATE $tableReference SET shareinv = '$serializedInventory' WHERE uuid = '${playerData.uuid}'"
      if (gateway.executeUpdate(updateCommand) === Fail) {
        Bukkit.getLogger().warning(s"${player.name} sql failed. => saveSharedInventory(executeUpdate failed)")

    return s"${RED}アイテムの収納に失敗しました".asMessageEffect ().left ()
      }

      return Unit.right()
    } catch (e: SQLException) {
      Bukkit.getLogger().warning(s"${player.name} sql failed. => clearShareInv(SQLException)")
      e.printStackTrace()

    return s"${RED}共有インベントリにアクセスできません".asMessageEffect ().left ()
    }
  }

  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def loadShareInv(player: Player, playerData: PlayerData): ResponseEffectOrResult[CommandSender, String] = {
    //連打による負荷防止の為クールダウン処理
    if (!playerData.shareinvcooldownflag) {
    return s"${RED}しばらく待ってからやり直してください".asMessageEffect ().left ()
    }
    CoolDownTask(player, CoolDownTask.SHAREINV).runTaskLater(plugin, 200)

    val command = s"SELECT shareinv FROM $tableReference WHERE uuid = '${playerData.uuid}'"
    try {
      gateway.executeQuery(command).use { lrs =>
        lrs.next()
        return lrs.getString("shareinv").right()
      }
    } catch (e: SQLException) {
      Bukkit.getLogger().warning(player.name + " sql failed. => loadShareInv")
      e.printStackTrace()

    return s"${RED}共有インベントリにアクセスできません".asMessageEffect ().left ()
    }
  }

  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def clearShareInv(player: Player, playerdata: PlayerData): ResponseEffectOrResult[CommandSender, Unit] = {
    val command = s"UPDATE $tableReference SET shareinv = '' WHERE uuid = '${playerdata.uuid}'"

    if (gateway.executeUpdate(command) === Fail) {
      Bukkit.getLogger().warning(s"${player.name} sql failed. => clearShareInv")
    return s"${RED}アイテムのクリアに失敗しました".asMessageEffect ().left ()
    }

    return Unit.right()
  }

  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def selectLeaversUUIDs(days: Int): List[UUID]? = {
    val command = s"select name, uuid from $tableReference " +
        s"where ((lastquit <= date_sub(curdate(), interval $days day)) " +
        "or (lastquit is null)) and (name != '') and (uuid != '')"
    val uuidList = util.ArrayList[UUID]()

    try {
      gateway.executeQuery(command).recordIteration {
        val lrs = this
        try {
          uuidList += UUID.fromString(lrs.getString("uuid"))
        } catch (e: IllegalArgumentException) {
          println("不適切なUUID: " + lrs.getString("name") + ": " + lrs.getString("uuid"))
        }
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      return null
    }
    return uuidList.toList()
  }

  //ランキング表示用に総破壊ブロック数のカラムだけ全員分引っ張る
  private def successBlockRankingUpdate(): Boolean = {
    val ranklist = util.ArrayList[RankData]()
    SeichiAssist.allplayerbreakblockint = 0
    val command = ("select name,level,totalbreaknum from " + tableReference
        + " order by totalbreaknum desc")
    try {
      gateway.executeQuery(command).recordIteration {
        val lrs = this
        val rankdata = RankData()
        rankdata.name = lrs.getString("name")
        rankdata.level = lrs.getInt("level")
        rankdata.totalbreaknum = lrs.getLong("totalbreaknum")
        ranklist += rankdata
        SeichiAssist.allplayerbreakblockint += rankdata.totalbreaknum
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      return false
    }

    SeichiAssist.ranklist.clear()
    SeichiAssist.ranklist.addAll(ranklist)
    return true
  }

  //ランキング表示用にプレイ時間のカラムだけ全員分引っ張る
  private def successPlayTickRankingUpdate(): Boolean = {
    val ranklist = util.ArrayList[RankData]()
    val command = ("select name,playtick from " + tableReference
        + " order by playtick desc")
    try {
      gateway.executeQuery(command).recordIteration {
        val lrs = this
        val rankdata = RankData()
        rankdata.name = lrs.getString("name")
        rankdata.playtick = lrs.getInt("playtick")
        ranklist += rankdata
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      return false
    }

    SeichiAssist.ranklist_playtick.clear()
    SeichiAssist.ranklist_playtick.addAll(ranklist)
    return true
  }

  //ランキング表示用に投票数のカラムだけ全員分引っ張る
  private def successVoteRankingUpdate(): Boolean = {
    val ranklist = util.ArrayList[RankData]()
    val command = ("select name,p_vote from " + tableReference
        + " order by p_vote desc")
    try {
      gateway.executeQuery(command).recordIteration {
        val lrs = this
        val rankdata = RankData()
        rankdata.name = lrs.getString("name")
        rankdata.p_vote = lrs.getInt("p_vote")
        ranklist += rankdata
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      return false
    }

    SeichiAssist.ranklist_p_vote.clear()
    SeichiAssist.ranklist_p_vote.addAll(ranklist)
    return true
  }

  //ランキング表示用にプレミアムエフェクトポイントのカラムだけ全員分引っ張る
  private def successPremiumEffectPointRanking(): Boolean = {
    val ranklist = util.ArrayList[RankData]()
    val command = ("select name,premiumeffectpoint from " + tableReference
        + " order by premiumeffectpoint desc")
    try {
      gateway.executeQuery(command).recordIteration {
        val lrs = this
        val rankdata = RankData()
        rankdata.name = lrs.getString("name")
        rankdata.premiumeffectpoint = lrs.getInt("premiumeffectpoint")
        ranklist += rankdata
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      return false
    }

    SeichiAssist.ranklist_premiumeffectpoint.clear()
    SeichiAssist.ranklist_premiumeffectpoint.addAll(ranklist)
    return true
  }

  //ランキング表示用に上げたりんご数のカラムだけ全員分引っ張る
  private def successAppleNumberRankingUpdate(): Boolean = {
    val ranklist = util.ArrayList[RankData]()
    SeichiAssist.allplayergiveapplelong = 0
    val command = s"select name,p_apple from $tableReference order by p_apple desc"
    try {
      gateway.executeQuery(command).recordIteration {
        val lrs = this
        val rankdata = RankData()
        rankdata.name = lrs.getString("name")
        rankdata.p_apple = lrs.getInt("p_apple")
        ranklist += rankdata
        SeichiAssist.allplayergiveapplelong += rankdata.p_apple.toLong()
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
      return false
    }

    SeichiAssist.ranklist_p_apple.clear()
    SeichiAssist.ranklist_p_apple.addAll(ranklist)
    return true
  }

  /**
   * 全ランキングリストの更新処理
   * @return 成否…true: 成功、false: 失敗
   * TODO この処理はDB上と通信を行う為非同期にすべき
   */
  def successRankingUpdate(): Boolean = {
    if (!successBlockRankingUpdate()) return false
    if (!successPlayTickRankingUpdate()) return false
    if (!successVoteRankingUpdate()) return false
    if (!successPremiumEffectPointRanking()) return false
    return successAppleNumberRankingUpdate()

  }

  //全員に詫びガチャの配布
  def addAllPlayerBug(amount: Int): ActionStatus = {
    val command = s"update $tableReference set numofsorryforbug = numofsorryforbug + $amount"
    return gateway.executeUpdate(command)
  }

  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def selectPocketInventoryOf(uuid: UUID): ResponseEffectOrResult[CommandSender, Inventory] = {
    val command = s"select inventory from $tableReference where uuid like '$uuid'"

    try {
      gateway.executeQuery(command).recordIteration {
        return BukkitSerialization.fromBase64(getString("inventory")).right()
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
    }

    return s"${RED}データベースから四次元ポケットのインベントリを取得できませんでした。".asMessageEffect ().left ()
  }

  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def inquireLastQuitOf(playerName: String): TargetedEffect[CommandSender] = {
    @SuspendingMethod def fetchLastQuitData(): String? = {
      val command = s"select lastquit from $tableReference where playerName = '$playerName'"
      try {
        gateway.executeQuery(command).use { lrs =>
          return if (lrs.next()) lrs.getString("lastquit") else null
        }
      } catch (e: SQLException) {
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return null
      }
    }

    return fetchLastQuitData()
        ?.let { s"${playerName}の最終ログアウト日時：$it".asMessageEffect() }
        ?: run {
          val messages = List(
            s"${RED}最終ログアウト日時の照会に失敗しました。",
            s"${RED}プレイヤー名やプレイヤー名が変更されていないか確認してください。",
            s"${RED}プレイヤー名が正しいのにこのエラーが出る場合、最終ログイン時間が古い可能性があります。"
          )

          messages.asMessageEffect()
        }
  }

  def loadPlayerData(playerUUID: UUID, playerName: String): PlayerData = {
    val databaseGateway = SeichiAssist.databaseGateway
    val table = DatabaseConstants.PLAYERDATA_TABLENAME
    val db = SeichiAssist.seichiAssistConfig.db

    //sqlコネクションチェック
    databaseGateway.ensureConnection()

    //同ステートメントだとmysqlの処理がバッティングした時に止まってしまうので別ステートメントを作成する
    val stmt = databaseGateway.con.createStatement()

    val stringUuid: String = playerUUID.toString().toLowerCase()

    //uuidがsqlデータ内に存在するか検索
    val count = run {
      val command = (s"select count(*) as count from $db.$table where uuid = '$stringUuid'")

      stmt.executeQuery(command).use { resultSet =>
        resultSet.next()
        resultSet.getInt("count")
      }
    }

    return when (count) {
      0 => {
        //uuidが存在しない時の処理
        SeichiAssist.instance.server.consoleSender.sendMessage(s"${YELLOW}${playerName}は完全初見です。プレイヤーデータを作成します")

        //新しくuuidとnameを設定し行を作成
        val command = s"insert into $db.$table (name,uuid,loginflag) values('$playerName','$stringUuid','1')"
        stmt.executeUpdate(command)

        PlayerData(playerUUID, playerName)
      }
      else => {
        //uuidが存在するときの処理
        loadExistingPlayerData(playerUUID, playerName)
      }
    }
  }

}

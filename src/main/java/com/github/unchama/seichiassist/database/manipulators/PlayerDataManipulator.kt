package com.github.unchama.seichiassist.database.manipulators

import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.github.unchama.contextualexecutor.builder.ResponseEffectOrResult
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.RankData
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.database.DatabaseConstants
import com.github.unchama.seichiassist.database.DatabaseGateway
import com.github.unchama.seichiassist.task.CoolDownTask
import com.github.unchama.seichiassist.task.loadExistingPlayerData
import com.github.unchama.seichiassist.task.recordIteration
import com.github.unchama.seichiassist.util.BukkitSerialization
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.asMessageEffect
import com.github.unchama.util.ActionStatus
import com.github.unchama.util.ActionStatus.Fail
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import java.io.IOException
import java.sql.SQLException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PlayerDataManipulator(private val gateway: DatabaseGateway) {
  private val plugin = SeichiAssist.instance

  private val tableReference: String
    get() = gateway.databaseName + "." + DatabaseConstants.PLAYERDATA_TABLENAME

  private inline fun ifCoolDownDoneThenGet(player: Player,
                                           playerdata: PlayerData,
                                           supplier: () -> Int): Int {
    //連打による負荷防止の為クールダウン処理
    if (!playerdata.votecooldownflag) {
      player.sendMessage(ChatColor.RED.toString() + "しばらく待ってからやり直してください")
      return 0
    }
    CoolDownTask(player, true, false, false).runTaskLater(plugin, 1200)

    return supplier()
  }

  //投票特典配布時の処理(p_givenvoteの値の更新もココ)
  fun compareVotePoint(player: Player, playerdata: PlayerData): Int {
    return ifCoolDownDoneThenGet(player, playerdata) {
      val struuid = playerdata.uuid.toString()

      var p_vote = 0
      var p_givenvote = 0

      var command = "select p_vote,p_givenvote from $tableReference where uuid = '$struuid'"
      try {
        gateway.executeQuery(command).recordIteration {
          p_vote = getInt("p_vote")
          p_givenvote = getInt("p_givenvote")
        }
      } catch (e: SQLException) {
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        player.sendMessage(ChatColor.RED.toString() + "投票特典の受け取りに失敗しました")
        return@ifCoolDownDoneThenGet 0
      }

      //比較して差があればその差の値を返す(同時にp_givenvoteも更新しておく)
      if (p_vote > p_givenvote) {
        command = ("update " + tableReference
            + " set p_givenvote = " + p_vote
            + " where uuid like '" + struuid + "'")
        if (gateway.executeUpdate(command) === Fail) {
          player.sendMessage(ChatColor.RED.toString() + "投票特典の受け取りに失敗しました")
          return@ifCoolDownDoneThenGet 0
        }

        return p_vote - p_givenvote
      }
      player.sendMessage(ChatColor.YELLOW.toString() + "投票特典は全て受け取り済みのようです")
      0
    }
  }

  //最新のnumofsorryforbug値を返してmysqlのnumofsorrybug値を初期化する処理
  fun givePlayerBug(player: Player, playerdata: PlayerData): Int {
    return ifCoolDownDoneThenGet(player, playerdata) {
      val struuid = playerdata.uuid.toString()
      var numofsorryforbug = 0

      var command = "select numofsorryforbug from $tableReference where uuid = '$struuid'"
      try {
        gateway.executeQuery(command).recordIteration {
          numofsorryforbug = getInt("numofsorryforbug")
        }
      } catch (e: SQLException) {
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        player.sendMessage(ChatColor.RED.toString() + "ガチャ券の受け取りに失敗しました")
        return@ifCoolDownDoneThenGet 0
      }

      if (numofsorryforbug > 576) {
        // 576より多い場合はその値を返す(同時にnumofsorryforbugから-576)
        command = ("update " + tableReference
            + " set numofsorryforbug = numofsorryforbug - 576"
            + " where uuid like '" + struuid + "'")
        if (gateway.executeUpdate(command) === Fail) {
          player.sendMessage(ChatColor.RED.toString() + "ガチャ券の受け取りに失敗しました")
          return@ifCoolDownDoneThenGet 0
        }

        return@ifCoolDownDoneThenGet 576
      } else if (numofsorryforbug > 0) {
        // 0より多い場合はその値を返す(同時にnumofsorryforbug初期化)
        command = ("update " + tableReference
            + " set numofsorryforbug = 0"
            + " where uuid like '" + struuid + "'")
        if (gateway.executeUpdate(command) === Fail) {
          player.sendMessage(ChatColor.RED.toString() + "ガチャ券の受け取りに失敗しました")
          return@ifCoolDownDoneThenGet 0
        }

        return@ifCoolDownDoneThenGet numofsorryforbug
      }

      player.sendMessage(ChatColor.YELLOW.toString() + "ガチャ券は全て受け取り済みのようです")
      0
    }
  }

  /**
   * 投票ポイントをインクリメントするメソッド。
   * @param playerName プレーヤー名
   * @return 処理の成否
   */
  fun incrementVotePoint(playerName: String): ActionStatus {
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
  fun addPremiumEffectPoint(playerName: String, num: Int): ActionStatus {
    val command = ("update " + tableReference
        + " set premiumeffectpoint = premiumeffectpoint + " + num //引数で来たポイント数分加算

        + " where name like '" + playerName + "'")

    return gateway.executeUpdate(command)
  }


  //指定されたプレイヤーにガチャ券を送信する
  fun addPlayerBug(playerName: String, num: Int): ActionStatus {
    val command = ("update " + tableReference
        + " set numofsorryforbug = numofsorryforbug + " + num
        + " where name like '" + playerName + "'")

    return gateway.executeUpdate(command)
  }

  fun addChainVote(name: String): Boolean {
    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy/MM/dd")
    var lastvote: String? = null
    var select = "SELECT lastvote FROM " + tableReference + " " +
        "WHERE name LIKE '" + name + "'"
    try {
      gateway.executeQuery(select).use { lrs ->
        // 初回のnextがnull→データが1件も無い場合
        if (!lrs.next()) {
          return false
        }

        val lv = lrs.getString("lastvote")

        lastvote = if (lv == null || lv == "") {
          sdf.format(cal.time)
        } else {
          lv
        }
      }

      val update = "UPDATE " + tableReference + " " +
          " SET lastvote = '" + sdf.format(cal.time) + "'" +
          " WHERE name LIKE '" + name + "'"

      gateway.executeUpdate(update)
    } catch (e: SQLException) {
      Bukkit.getLogger().warning(Util.getName(name) + " sql failed. -> lastvote")
      e.printStackTrace()
      return false
    }

    select = "SELECT chainvote FROM " + tableReference + " " +
        "WHERE name LIKE '" + name + "'"
    try {
      gateway.executeQuery(select).use { lrs ->
        // 初回のnextがnull→データが1件も無い場合
        if (!lrs.next()) {
          return false
        }
        var count = lrs.getInt("chainvote")
        try {
          val TodayDate = sdf.parse(sdf.format(cal.time))
          val LastDate = sdf.parse(lastvote)
          val TodayLong = TodayDate.time
          val LastLong = LastDate.time

          val datediff = (TodayLong - LastLong) / (1000 * 60 * 60 * 24)
          if (datediff <= 1 || datediff >= 0) {
            count++
          } else {
            count = 1
          }
          //プレイヤーがオンラインの時即時反映させる
          val player = Bukkit.getServer().getPlayer(name)
          if (player != null) {
            //UUIDを取得
            val givenuuid = player.uniqueId
            //playerdataを取得
            val playerdata = SeichiAssist.playermap[givenuuid]!!

            playerdata.ChainVote++
          }
        } catch (e: ParseException) {
          e.printStackTrace()
        }

        lrs.close()

        val update = "UPDATE " + tableReference + " " +
            " SET chainvote = " + count +
            " WHERE name LIKE '" + name + "'"

        gateway.executeUpdate(update)
      }
    } catch (e: SQLException) {
      Bukkit.getLogger().warning(Util.getName(name) + " sql failed. -> chainvote")
      e.printStackTrace()
      return false
    }

    return true
  }

  @Suppress("RedundantSuspendModifier")
  private suspend fun assertPlayerDataExistenceFor(playerName: String): ResponseEffectOrResult<CommandSender, Unit> =
      try {
        gateway.executeQuery("select * from $tableReference where name like $playerName").use { resultSet ->
          if (!resultSet.next()) {
            "${ChatColor.RED}$playerName はデータベースに登録されていません。".asMessageEffect().left()
          } else {
            Unit.right()
          }
        }
      } catch (e: SQLException) {
        Bukkit.getLogger().warning("sql failed on checking data existence of $playerName")
        e.printStackTrace()

        "${ChatColor.RED}プレーヤーデータへのアクセスに失敗しました。".asMessageEffect().left()
      }

  suspend fun addContributionPoint(targetPlayerName: String, point: Int): ResponseEffectOrResult<CommandSender, Unit> {
    @Suppress("RedundantSuspendModifier")
    suspend fun executeUpdate(): ResponseEffectOrResult<CommandSender, Unit> {
      val updateCommand = "UPDATE $tableReference SET contribute_point = contribute_point + $point WHERE name LIKE '$targetPlayerName'"

      return if (gateway.executeUpdate(updateCommand) === Fail) {
        Bukkit.getLogger().warning("sql failed on updating $targetPlayerName's contribute_point")
        "${ChatColor.RED}貢献度ptの変更に失敗しました。".asMessageEffect().left()
      } else {
        Unit.right()
      }
    }

    @Suppress("RedundantSuspendModifier")
    suspend fun updatePlayerDataMemoryCache() {
      Bukkit.getServer().getPlayer(targetPlayerName)?.let { targetPlayer ->
        val targetPlayerData = SeichiAssist.playermap[targetPlayer.uniqueId] ?: return@let

        targetPlayerData.contribute_point += point
        targetPlayerData.setContributionPoint(point)
      }
    }

    return assertPlayerDataExistenceFor(targetPlayerName)
        .flatMap { executeUpdate() }
        .map { updatePlayerDataMemoryCache() }
  }

  // anniversary変更
  fun setAnniversary(anniversary: Boolean, uuid: UUID?): Boolean {
    var command = "UPDATE $tableReference SET anniversary = $anniversary"
    if (uuid != null) {
      command += " WHERE uuid = '$uuid'"
    }
    if (gateway.executeUpdate(command) === Fail) {
      Bukkit.getLogger().warning("sql failed. -> setAnniversary")
      return false
    }
    return true
  }


  @Suppress("RedundantSuspendModifier")
  suspend fun saveSharedInventory(player: Player, playerData: PlayerData, serializedInventory: String): ResponseEffectOrResult<CommandSender, Unit> {
    //連打による負荷防止の為クールダウン処理
    if (!playerData.shareinvcooldownflag) {
      return "${ChatColor.RED}しばらく待ってからやり直してください".asMessageEffect().left()
    }
    CoolDownTask(player, CoolDownTask.SHAREINV).runTaskLater(plugin, 200)

    try {
      // 共有インベントリに既にアイテムが格納されていないことを確認する
      val selectCommand = "SELECT shareinv FROM $tableReference WHERE uuid = '${playerData.uuid}'"
      gateway.executeQuery(selectCommand).use { lrs ->
        lrs.next()
        val sharedInventorySerial = lrs.getString("shareinv")
        if (sharedInventorySerial != null && sharedInventorySerial != "") {
          return "${ChatColor.RED}既にアイテムが収納されています".asMessageEffect().left()
        }
      }

      // シリアル化されたインベントリデータを書き込む
      val updateCommand = "UPDATE $tableReference SET shareinv = '$serializedInventory' WHERE uuid = '${playerData.uuid}'"
      if (gateway.executeUpdate(updateCommand) === Fail) {
        Bukkit.getLogger().warning("${player.name} sql failed. -> saveSharedInventory(executeUpdate failed)")

        return "${ChatColor.RED}アイテムの収納に失敗しました".asMessageEffect().left()
      }

      return Unit.right()
    } catch (e: SQLException) {
      Bukkit.getLogger().warning("${player.name} sql failed. -> clearShareInv(SQLException)")
      e.printStackTrace()

      return "${ChatColor.RED}共有インベントリにアクセスできません".asMessageEffect().left()
    }
  }

  @Suppress("RedundantSuspendModifier")
  suspend fun loadShareInv(player: Player, playerData: PlayerData): ResponseEffectOrResult<CommandSender, String> {
    //連打による負荷防止の為クールダウン処理
    if (!playerData.shareinvcooldownflag) {
      return "${ChatColor.RED}しばらく待ってからやり直してください".asMessageEffect().left()
    }
    CoolDownTask(player, CoolDownTask.SHAREINV).runTaskLater(plugin, 200)

    val command = "SELECT shareinv FROM $tableReference WHERE uuid = '${playerData.uuid}'"
    try {
      gateway.executeQuery(command).use { lrs ->
        lrs.next()
        return lrs.getString("shareinv").right()
      }
    } catch (e: SQLException) {
      Bukkit.getLogger().warning(player.name + " sql failed. -> loadShareInv")
      e.printStackTrace()

      return "${ChatColor.RED}共有インベントリにアクセスできません".asMessageEffect().left()
    }
  }

  @Suppress("RedundantSuspendModifier")
  suspend fun clearShareInv(player: Player, playerdata: PlayerData): ResponseEffectOrResult<CommandSender, Unit> {
    val command = "UPDATE $tableReference SET shareinv = '' WHERE uuid = '${playerdata.uuid}'"

    if (gateway.executeUpdate(command) === Fail) {
      Bukkit.getLogger().warning("${player.name} sql failed. -> clearShareInv")
      return "${ChatColor.RED}アイテムのクリアに失敗しました".asMessageEffect().left()
    }

    return Unit.right()
  }

  @Suppress("RedundantSuspendModifier")
  suspend fun selectLeaversUUIDs(days: Int): List<UUID>? {
    val command = "select name, uuid from $tableReference " +
        "where ((lastquit <= date_sub(curdate(), interval $days day)) " +
        "or (lastquit is null)) and (name != '') and (uuid != '')"
    val uuidList = ArrayList<UUID>()

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
  private fun successBlockRankingUpdate(): Boolean {
    val ranklist = ArrayList<RankData>()
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
  private fun successPlayTickRankingUpdate(): Boolean {
    val ranklist = ArrayList<RankData>()
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
  private fun successVoteRankingUpdate(): Boolean {
    val ranklist = ArrayList<RankData>()
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
  private fun successPremiumEffectPointRanking(): Boolean {
    val ranklist = ArrayList<RankData>()
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
  private fun successAppleNumberRankingUpdate(): Boolean {
    val ranklist = ArrayList<RankData>()
    SeichiAssist.allplayergiveapplelong = 0
    val command = "select name,p_apple from $tableReference order by p_apple desc"
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
  fun successRankingUpdate(): Boolean {
    if (!successBlockRankingUpdate()) return false
    if (!successPlayTickRankingUpdate()) return false
    if (!successVoteRankingUpdate()) return false
    if (!successPremiumEffectPointRanking()) return false
    return successAppleNumberRankingUpdate()

  }

  //全員に詫びガチャの配布
  fun addAllPlayerBug(amount: Int): ActionStatus {
    val command = "update $tableReference set numofsorryforbug = numofsorryforbug + $amount"
    return gateway.executeUpdate(command)
  }

  @Suppress("RedundantSuspendModifier")
  suspend fun selectPocketInventoryOf(uuid: UUID): ResponseEffectOrResult<CommandSender, Inventory> {
    val command = "select inventory from $tableReference where uuid like '$uuid'"

    try {
      gateway.executeQuery(command).recordIteration {
        return BukkitSerialization.fromBase64(getString("inventory")).right()
      }
    } catch (e: SQLException) {
      println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
      e.printStackTrace()
    }

    return "${ChatColor.RED}データベースから四次元ポケットのインベントリを取得できませんでした。".asMessageEffect().left()
  }

  @Suppress("RedundantSuspendModifier")
  suspend fun inquireLastQuitOf(playerName: String): TargetedEffect<CommandSender> {
    suspend fun fetchLastQuitData(): String? {
      val command = "select lastquit from $tableReference where playerName = '$playerName'"
      try {
        gateway.executeQuery(command).use { lrs ->
          return if (lrs.next()) lrs.getString("lastquit") else null
        }
      } catch (e: SQLException) {
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return null
      }
    }

    return fetchLastQuitData()
        ?.let { "${playerName}の最終ログアウト日時：$it".asMessageEffect() }
        ?: run {
          val messages = listOf(
              "${ChatColor.RED}最終ログアウト日時の照会に失敗しました。",
              "${ChatColor.RED}プレイヤー名やプレイヤー名が変更されていないか確認してください。",
              "${ChatColor.RED}プレイヤー名が正しいのにこのエラーが出る場合、最終ログイン時間が古い可能性があります。"
          )

          messages.asMessageEffect()
        }
  }

  fun loadPlayerData(playerUUID: UUID, playerName: String, ignoreActiveState: Boolean): PlayerData {
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
      val command = ("select count(*) as count from $db.$table where uuid = '$stringUuid'")

      stmt.executeQuery(command).use { resultSet ->
        resultSet.next()
        resultSet.getInt("count")
      }
    }

    return when (count) {
      0 -> {
        //uuidが存在しない時の処理
        SeichiAssist.instance.server.consoleSender.sendMessage("${ChatColor.YELLOW}${playerName}は完全初見です。プレイヤーデータを作成します")

        //新しくuuidとnameを設定し行を作成
        val command = "insert into $db.$table (name,uuid,loginflag) values('$playerName','$stringUuid','1')"
        stmt!!.executeUpdate(command)

        PlayerData(playerUUID, playerName)
      }
      else -> {
        //uuidが存在するときの処理
        loadExistingPlayerData(playerUUID, playerName, ignoreActiveState)
      }
    }
  }

  companion object {
    //指定プレイヤーの四次元ポケットの中身取得
    fun selectInventory(playerDataManipulator: PlayerDataManipulator, uuid: UUID): Inventory? {
      val struuid = uuid.toString()
      var inventory: Inventory? = null
      val command = ("select inventory from " + playerDataManipulator.tableReference
          + " where uuid like '" + struuid + "'")
      try {
        playerDataManipulator.gateway.executeQuery(command).recordIteration {
          inventory = BukkitSerialization.fromBase64(getString("inventory"))
        }
      } catch (e: SQLException) {
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return null
      } catch (e: IOException) {
        println("sqlクエリの実行に失敗しました。以下にエラーを表示します")
        e.printStackTrace()
        return null
      }

      return inventory
    }
  }

}

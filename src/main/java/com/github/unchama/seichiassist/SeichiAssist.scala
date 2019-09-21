package com.github.unchama.seichiassist

import java.util.UUID

import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.menuinventory.MenuHandler
import com.github.unchama.seichiassist.bungee.BungeeReceiver
import com.github.unchama.seichiassist.commands._
import com.github.unchama.seichiassist.commands.legacy.GachaCommand
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.data.{GachaPrize, MineStackGachaData, RankData}
import com.github.unchama.seichiassist.database.DatabaseGateway
import com.github.unchama.seichiassist.listener.new_year_event.NewYearsEvent
import com.github.unchama.seichiassist.listener._
import com.github.unchama.seichiassist.minestack.{MineStackObj, MineStackObjectCategory}
import com.github.unchama.seichiassist.task.{HalfHourRankingRoutine, PlayerDataBackupTask, PlayerDataPeriodicRecalculation}
import com.github.unchama.util.syntax.Nullability._
import kotlinx.coroutines.Job
import org.bukkit.ChatColor._
import org.bukkit.block.Block
import org.bukkit.command.{Command, CommandSender}
import org.bukkit.entity.Entity
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.{Bukkit, Material}

import scala.collection.JavaConverters._
import scala.collection.mutable
class SeichiAssist extends JavaPlugin() {
  SeichiAssist.instance = this

  private var repeatedJobCoroutine: Option[Job] = None

  val expBarSynchronization = new ExpBarSynchronization()

  override def onEnable() {
    val logger = getLogger

    //チャンネルを追加
    Bukkit.getMessenger.registerOutgoingPluginChannel(this, "BungeeCord")

    // BungeeCordとのI/O
    Bukkit.getMessenger.registerIncomingPluginChannel(this, "SeichiAssistBungee", new BungeeReceiver(this))
    Bukkit.getMessenger.registerOutgoingPluginChannel(this, "SeichiAssistBungee")


    //コンフィグ系の設定は全てConfig.javaに移動
    SeichiAssist.seichiAssistConfig = new Config(this)
    SeichiAssist.seichiAssistConfig.loadConfig()

    if (SeichiAssist.seichiAssistConfig.getDebugMode == 1) {
      //debugmode=1の時は最初からデバッグモードで鯖を起動
      logger.info(s"${RED}seichiassistをデバッグモードで起動します")
      logger.info(s"${RED}コンソールから/seichi debugmode")
      logger.info(s"${RED}を実行するといつでもONOFFを切り替えられます")
      SeichiAssist.DEBUG = true
    } else {
      //debugmode=0の時は/seichi debugmodeによる変更コマンドも使えない
      logger.info(s"${GREEN}seichiassistを通常モードで起動します")
      logger.info(s"${GREEN}デバッグモードを使用する場合は")
      logger.info(s"${GREEN}config.ymlの設定値を書き換えて再起動してください")
    }

    try {
      SeichiAssist.databaseGateway = DatabaseGateway.createInitializedInstance(
        SeichiAssist.seichiAssistConfig.getURL, SeichiAssist.seichiAssistConfig.getDB,
        SeichiAssist.seichiAssistConfig.getID, SeichiAssist.seichiAssistConfig.getPW
      )
    } catch {
      case e: Exception => {
        e.printStackTrace()
        logger.severe("データベース初期化に失敗しました。サーバーを停止します…")
        Bukkit.shutdown()
      }
    }

    //mysqlからガチャデータ読み込み
    if (!SeichiAssist.databaseGateway.gachaDataManipulator.loadGachaData()) {
      logger.severe("ガチャデータのロードに失敗しました")
      Bukkit.shutdown()
    }

    //mysqlからMineStack用ガチャデータ読み込み
    if (!SeichiAssist.databaseGateway.mineStackGachaDataManipulator.loadMineStackGachaData()) {
      logger.severe("MineStack用ガチャデータのロードに失敗しました")
      Bukkit.shutdown()
    }

    MineStackObjectList.minestackGachaPrizes ++= SeichiAssist.generateGachaPrizes()

    MineStackObjectList.minestacklist.clear()
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistmine
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistdrop
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistfarm
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistbuild
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistrs
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestackGachaPrizes

    // コマンドの登録
    Map(
        "gacha" -> new GachaCommand(),
        "ef" -> EffectCommand.executor,
        "seichihaste" -> SeichiHasteCommand.executor,
        "seichiassist" -> SeichiAssistCommand.executor,
        "openpocket" -> OpenPocketCommand.executor,
        "lastquit" -> LastQuitCommand.executor,
        "stick" -> StickCommand.executor,
        "rmp" -> RmpCommand.executor,
        "shareinv" -> ShareInvCommand.executor,
        "mebius" -> MebiusCommand.executor,
        "achievement" -> AchievementCommand.executor,
        "halfguard" -> HalfBlockProtectCommand.executor,
        "event" -> EventCommand.executor,
        "contribute" -> ContributeCommand.executor,
        "subhome" -> SubHomeCommand.executor,
        "gtfever" -> GiganticFeverCommand.executor,
        "minehead" -> MineHeadCommand.executor,
        "x-transfer" -> RegionOwnerTransferCommand.executor
    ).foreach {
      case (commandName, executor) => getCommand(commandName).setExecutor(executor)
    }

    //リスナーの登録
    List(
        new PlayerJoinListener(),
        new PlayerQuitListener(),
        new PlayerClickListener(),
        new PlayerChatEventListener(),
        new PlayerBlockBreakListener(),
        new PlayerInventoryListener(),
        new EntityListener(),
        new PlayerPickupItemListener(),
        new PlayerDeathEventListener(),
        new GachaItemListener(),
        new MebiusListener(),
        new RegionInventoryListener(),
        new WorldRegenListener()
    ).foreach { getServer.getPluginManager.registerEvents(_, this) }

    //正月イベント用
    new NewYearsEvent(this)

    //Menu用Listener
    getServer.getPluginManager.registerEvents(MenuHandler, this)

    //オンラインの全てのプレイヤーを処理
    getServer.getOnlinePlayers.asScala.foreach { p =>
      try {
        //プレイヤーデータを生成
        SeichiAssist.playermap(p.getUniqueId) = SeichiAssist.databaseGateway
          .playerDataManipulator.loadPlayerData(p.getUniqueId, p.getName)
      } catch {
        case e: Exception => {
          e.printStackTrace()
          p.kickPlayer("プレーヤーデータの読み込みに失敗しました。")
        }
      }
    }

    //ランキングリストを最新情報に更新する
    if (!SeichiAssist.databaseGateway.playerDataManipulator.successRankingUpdate()) {
      logger.info("ランキングデータの作成に失敗しました")
      Bukkit.shutdown()
    }

    startRepeatedJobs()

    logger.info("SeichiAssist is Enabled!")

    SeichiAssist.buildAssist = new BuildAssist(this)
    SeichiAssist.buildAssist.onEnable()
  }

  override def onDisable(): Unit = {
    val logger = getLogger

    cancelRepeatedJobs()

    //全てのエンティティを削除
    SeichiAssist.entitylist.foreach {
      _.remove()
    }

    //全てのスキルで破壊されるブロックを強制破壊
    SeichiAssist.allblocklist.foreach(_.setType(Material.AIR))

    //sqlコネクションチェック
    SeichiAssist.databaseGateway.ensureConnection()
    getServer.getOnlinePlayers.asScala.foreach { p => {
      //UUIDを取得
      val uuid = p.getUniqueId
      //プレイヤーデータ取得
      val playerdata = SeichiAssist.playermap(uuid)
      //念のためエラー分岐
      if (playerdata == null) {
        p.sendMessage(s"${RED}playerdataの保存に失敗しました。管理者に報告してください")
        getServer.getConsoleSender.sendMessage(s"${RED}SeichiAssist[Ondisable処理]でエラー発生")
        logger.warning(s"${p.getName}のplayerdataの保存失敗。開発者に報告してください")
        return
      }
      //quit時とondisable時、プレイヤーデータを最新の状態に更新
      playerdata.updateOnQuit()

      runBlocking {
        savePlayerData(playerdata)
      }
    }

      if (SeichiAssist.databaseGateway.disconnect() == Fail) {
        logger.info("データベース切断に失敗しました")
      }

      logger.info("SeichiAssist is Disabled!")

      SeichiAssist.buildAssist.onDisable()
    }
  }

  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Unit
      = SeichiAssist.buildAssist.onCommand(sender, command, label, args)

  private def startRepeatedJobs() {
    repeatedJobCoroutine = Some(CoroutineScope(Schedulers.sync).launch {
      launch { HalfHourRankingRoutine.launch() }
      launch { PlayerDataPeriodicRecalculation.launch() }
      launch { PlayerDataBackupTask.launch() }
    })
  }

  private def cancelRepeatedJobs() {
    repeatedJobCoroutine match {
      case Some(x) => x.cancel(null)
    }
  }

  def restartRepeatedJobs() {
    cancelRepeatedJobs()
    startRepeatedJobs()
  }
}

object SeichiAssist {
  var instance: SeichiAssist

  //デバッグフラグ(デバッグモード使用時はここで変更するのではなくconfig.ymlの設定値を変更すること！)
  var DEBUG = false

  //ガチャシステムのメンテナンスフラグ
  var gachamente = false

  val SEICHIWORLDNAME = "world_sw"
  val DEBUGWORLDNAME = "world"

  // TODO staticであるべきではない
  var databaseGateway: DatabaseGateway
  var seichiAssistConfig: Config

  var buildAssist: BuildAssist

  //Gachadataに依存するデータリスト
  val gachadatalist: mutable.MutableList[GachaPrize] = mutable.MutableList()

  //(minestackに格納する)Gachadataに依存するデータリスト
  var msgachadatalist: mutable.MutableList[MineStackGachaData] = mutable.MutableList()

  //Playerdataに依存するデータリスト
  val playermap: mutable.HashMap[UUID, PlayerData] = mutable.HashMap()

  //総採掘量ランキング表示用データリスト
  val ranklist: mutable.MutableList[RankData] = mutable.MutableList()

  //プレイ時間ランキング表示用データリスト
  val ranklist_playtick: mutable.MutableList[RankData] = mutable.MutableList()

  //投票ポイント表示用データリスト
  val ranklist_p_vote: mutable.MutableList[RankData] = mutable.MutableList()

  //マナ妖精表示用のデータリスト
  val ranklist_p_apple: mutable.MutableList[RankData] = mutable.MutableList()

  //プレミアムエフェクトポイント表示用データリスト
  val ranklist_premiumeffectpoint: mutable.MutableList[RankData] = mutable.MutableList()

  //総採掘量表示用
  var allplayerbreakblockint = 0L

  var allplayergiveapplelong = 0L

  //プラグインで出すエンティティの保存
  val entitylist: mutable.MutableList[Entity] = mutable.MutableList()

  //プレイヤーがスキルで破壊するブロックリスト
  val allblocklist: mutable.MutableList[Block] = mutable.MutableList()

  private def generateGachaPrizes(): List[MineStackObj] = {
    val minestacklist = mutable.MutableList[MineStackObj]()
    for (i <- msgachadatalist.indices) {
      val g = msgachadatalist(i)
      if (g.itemStack.getType != Material.EXP_BOTTLE) { //経験値瓶だけはすでにリストにあるので除外
        minestacklist += new MineStackObj(g.objName, null, g.level, g.itemStack, true, i, MineStackObjectCategory.GACHA_PRIZES)
      }
    }
    minestacklist.toList
  }
}

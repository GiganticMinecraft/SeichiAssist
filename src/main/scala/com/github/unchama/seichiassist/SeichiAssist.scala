package com.github.unchama.seichiassist

import java.util.UUID

import cats.Parallel.Aux
import cats.effect
import cats.effect.{Fiber, IO, SyncIO, Timer}
import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.chatinterceptor.{ChatInterceptor, InterceptionScope}
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.ResourceScope.SingleResourceScope
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.itemmigration.domain.ItemMigrations
import com.github.unchama.itemmigration.service.ItemMigrationService
import com.github.unchama.itemmigration.{service, _}
import com.github.unchama.menuinventory.MenuHandler
import com.github.unchama.playerdatarepository.{NonPersistentPlayerDataRefRepository, TryableFiberRepository}
import com.github.unchama.seichiassist.MaterialSets.BlockBreakableBySkill
import com.github.unchama.seichiassist.bungee.BungeeReceiver
import com.github.unchama.seichiassist.commands._
import com.github.unchama.seichiassist.commands.legacy.GachaCommand
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.data.{GachaPrize, MineStackGachaData, RankData}
import com.github.unchama.seichiassist.database.DatabaseGateway
import com.github.unchama.seichiassist.domain.minecraft.UuidRepository
import com.github.unchama.seichiassist.infrastructure.ScalikeJDBCConfiguration
import com.github.unchama.seichiassist.infrastructure.minecraft.JdbcBackedUuidRepository
import com.github.unchama.seichiassist.listener._
import com.github.unchama.seichiassist.listener.new_year_event.NewYearsEvent
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.minestack.{MineStackObj, MineStackObjectCategory}
import com.github.unchama.seichiassist.subsystems._
import com.github.unchama.seichiassist.subsystems.itemmigration.SeichiAssistItemMigrations
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.loggers.{PersistedItemsMigrationSlf4jLogger, PlayerItemsMigrationSlf4jLogger, WorldLevelMigrationSlf4jLogger}
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.repositories.{PersistedItemsMigrationVersionRepository, PlayerItemsMigrationVersionRepository, WorldLevelItemsMigrationVersionRepository}
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.targets.{SeichiAssistPersistedItems, SeichiAssistWorldLevelData}
import com.github.unchama.seichiassist.task.PlayerDataSaveTask
import com.github.unchama.seichiassist.task.global.{HalfHourRankingRoutine, PlayerDataBackupRoutine, PlayerDataRecalculationRoutine}
import com.github.unchama.util.{ActionStatus, ClassUtils}
import org.bukkit.ChatColor._
import org.bukkit.command.{Command, CommandSender}
import org.bukkit.entity.Entity
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.{Bukkit, Material}
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.impl.JDK14LoggerFactory
import scalikejdbc.DB

import scala.collection.JavaConverters._
import scala.collection.mutable

class SeichiAssist extends JavaPlugin() {
  SeichiAssist.instance = this

  private var hasBeenLoadedAlready = false

  val expBarSynchronization = new ExpBarSynchronization()
  private var repeatedTaskFiber: Option[Fiber[IO, List[Nothing]]] = None

  // TODO: `ResourceScope[IO, SyncIO, Projectile]` にしたい
  val arrowSkillProjectileScope: ResourceScope[IO, IO, Entity] = {
    import PluginExecutionContexts.asyncShift
    ResourceScope.unsafeCreate
  }
  // TODO: `ResourceScope[IO, SyncIO, Entity]` にしたい
  val magicEffectEntityScope: SingleResourceScope[IO, Entity] = {
    import PluginExecutionContexts.asyncShift
    ResourceScope.unsafeCreateSingletonScope
  }

  lazy val expBottleStackSystem: StatefulSubsystem[subsystems.expbottlestack.InternalState[IO, SyncIO]] = {
    import PluginExecutionContexts.asyncShift
    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    subsystems.expbottlestack.System.wired[IO, SyncIO]
  }.unsafeRunSync()

  /**
   * スキル使用などで破壊されることが確定したブロック塊のスコープ
   *
   * TODO: `ResourceScope[IO, SyncIO, Set[BlockBreakableBySkill]]` にしたい
   */
  val lockedBlockChunkScope: ResourceScope[IO, IO, Set[BlockBreakableBySkill]] = {
    import PluginExecutionContexts.asyncShift
    ResourceScope.unsafeCreate
  }

  val activeSkillAvailability: NonPersistentPlayerDataRefRepository[SyncIO, IO, SyncIO, Boolean] = {
    import PluginExecutionContexts.asyncShift
    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    new NonPersistentPlayerDataRefRepository[SyncIO, IO, SyncIO, Boolean](true)
  }

  val assaultSkillRoutines: TryableFiberRepository[IO, SyncIO] = {
    import PluginExecutionContexts.asyncShift
    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    new TryableFiberRepository[IO, SyncIO]()
  }

  private val kickAllPlayersDueToInitialization: SyncIO[Unit] = SyncIO {
    getServer.getOnlinePlayers.asScala.foreach { player =>
      player.kickPlayer("プラグインを初期化しています。時間を置いて再接続してください。")
    }
  }

  override def onEnable(): Unit = {
    /**
     * Spigotサーバーが開始されるときにはまだPreLoginEventがcatchされない等色々な不都合があるので、
     * SeichiAssistの初期化はプレーヤーが居ないことを前提として進めることとする。
     *
     * NOTE:
     * PreLoginToQuitPlayerDataRepository に関してはJoinEventさえcatchできれば弾けるので、
     * 接続を試みているプレーヤーは弾かないで良さそう、と言うか弾く術がない
     */
    kickAllPlayersDueToInitialization.unsafeRunSync()

    val logger = getLogger
    // java.util.logging.Loggerの名前はJVM上で一意
    implicit val slf4jLogger: Logger = new JDK14LoggerFactory().getLogger(logger.getName)

    if (hasBeenLoadedAlready) {
      slf4jLogger.error("SeichiAssistは2度enableされることを想定されていません！シャットダウンします…")
      Bukkit.shutdown()
      return
    }

    //チャンネルを追加
    Bukkit.getMessenger.registerOutgoingPluginChannel(this, "BungeeCord")

    // BungeeCordとのI/O
    Bukkit.getMessenger.registerIncomingPluginChannel(this, "SeichiAssistBungee", new BungeeReceiver(this))
    Bukkit.getMessenger.registerOutgoingPluginChannel(this, "SeichiAssistBungee")


    //コンフィグ系の設定は全てConfig.javaに移動
    SeichiAssist.seichiAssistConfig = Config.loadFrom(this)

    if (SeichiAssist.seichiAssistConfig.getDebugMode == 1) {
      //debugmode=1の時は最初からデバッグモードで鯖を起動
      logger.info(s"${RED}SeichiAssistをデバッグモードで起動します")
      logger.info(s"${RED}コンソールから/seichi debugmode")
      logger.info(s"${RED}を実行するといつでもON/OFFを切り替えられます")
      SeichiAssist.DEBUG = true
    } else {
      //debugmode=0の時は/seichi debugmodeによる変更コマンドも使えない
      logger.info(s"${GREEN}SeichiAssistを通常モードで起動します")
      logger.info(s"${GREEN}デバッグモードを使用する場合は")
      logger.info(s"${GREEN}config.ymlの設定値を書き換えて再起動してください")
    }

    {
      val config = SeichiAssist.seichiAssistConfig
      import config._
      ScalikeJDBCConfiguration.initializeConnectionPool(s"$getURL/$getDB", getID, getPW)
      ScalikeJDBCConfiguration.initializeGlobalConfigs()

      /*
       * Flywayクラスは、ロード時にstaticフィールドの初期化処理でJavaUtilLogCreatorをContextClassLoader経由で
       * インスタンス化を試みるが、ClassNotFoundExceptionを吐いてしまう。これはSpigotが使用しているクラスローダーが
       * ContextClassLoaderに指定されていないことに起因する。
       *
       * 明示的にプラグインクラスを読み込んだクラスローダーを使用することで正常に読み込みが完了する。
       */
      ClassUtils.withThreadContextClassLoaderAs(
        classOf[SeichiAssist].getClassLoader,
        () => Flyway.configure.dataSource(getURL, getID, getPW)
          .baselineOnMigrate(true)
          .locations("db/migration", "com/github/unchama/seichiassist/database/migrations")
          .baselineVersion("1.0.0")
          .schemas("flyway_managed_schema")
          .load.migrate
      )
    }

    val migrations: ItemMigrations = {
      implicit val uuidRepository: UuidRepository[SyncIO] =
        JdbcBackedUuidRepository.initializeInstance[SyncIO].unsafeRunSync()

      SeichiAssistItemMigrations.seq
    }

    DB.autoCommit { implicit session =>
      // DB内アイテムのマイグレーション
      ItemMigrationService.inContextOf[SyncIO](
        new PersistedItemsMigrationVersionRepository(),
        new PersistedItemsMigrationSlf4jLogger(slf4jLogger)
      )
        .runMigration(migrations)(new SeichiAssistPersistedItems())
        .unsafeRunSync()
    }

    // ワールド内アイテムのマイグレーション
    service.ItemMigrationService.inContextOf[IO](
      new WorldLevelItemsMigrationVersionRepository(SeichiAssist.seichiAssistConfig.getServerId),
      new WorldLevelMigrationSlf4jLogger(slf4jLogger)
    )
      .runMigration(migrations) {
        import PluginExecutionContexts.asyncShift
        new SeichiAssistWorldLevelData()
      }
      .unsafeRunSync()

    try {
      SeichiAssist.databaseGateway = DatabaseGateway.createInitializedInstance(
        SeichiAssist.seichiAssistConfig.getURL, SeichiAssist.seichiAssistConfig.getDB,
        SeichiAssist.seichiAssistConfig.getID, SeichiAssist.seichiAssistConfig.getPW
      )
    } catch {
      case e: Exception =>
        e.printStackTrace()
        logger.severe("データベース初期化に失敗しました。サーバーを停止します…")
        Bukkit.shutdown()
    }

    //mysqlからガチャデータ読み込み
    if (!SeichiAssist.databaseGateway.gachaDataManipulator.loadGachaData()) {
      logger.severe("ガチャデータのロードに失敗しました。サーバーを停止します…")
      Bukkit.shutdown()
    }

    //mysqlからMineStack用ガチャデータ読み込み
    if (!SeichiAssist.databaseGateway.mineStackGachaDataManipulator.loadMineStackGachaData()) {
      logger.severe("MineStack用ガチャデータのロードに失敗しました。サーバーを停止します…")
      Bukkit.shutdown()
    }

    // プレーヤーインベントリ内アイテムのマイグレーション処理のコントローラであるリスナー
    val playerItemMigrationControllerListeners: Seq[Listener] = {
      import PluginExecutionContexts.asyncShift

      implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

      val service = ItemMigrationService.inContextOf[IO](
        new PlayerItemsMigrationVersionRepository(SeichiAssist.seichiAssistConfig.getServerId),
        new PlayerItemsMigrationSlf4jLogger(slf4jLogger)
      )

      new PlayerItemMigrationEntryPoints[IO, SyncIO](migrations, service).listenersToBeRegistered
    }

    import PluginExecutionContexts._

    MineStackObjectList.minestackGachaPrizes ++= SeichiAssist.generateGachaPrizes()

    MineStackObjectList.minestacklist.clear()
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistmine
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistdrop
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistfarm
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistbuild
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistrs
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestackGachaPrizes

    import SeichiAssist.Scopes.globalChatInterceptionScope

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val timer: Timer[IO] = IO.timer(cachedThreadPool)

    val subsystems = Seq(
      mebius.System.wired,
      expBottleStackSystem
    )

    // コマンドの登録
    Map(
      "gacha" -> new GachaCommand(),
      "map" -> MapCommand.executor,
      "ef" -> EffectCommand.executor,
      "seichihaste" -> SeichiHasteCommand.executor,
      "seichiassist" -> SeichiAssistCommand.executor,
      "openpocket" -> OpenPocketCommand.executor,
      "lastquit" -> LastQuitCommand.executor,
      "stick" -> StickCommand.executor,
      "rmp" -> RmpCommand.executor,
      "shareinv" -> ShareInvCommand.executor,
      "achievement" -> AchievementCommand.executor,
      "halfguard" -> HalfBlockProtectCommand.executor,
      "event" -> EventCommand.executor,
      "contribute" -> ContributeCommand.executor,
      "subhome" -> SubHomeCommand.executor,
      "gtfever" -> GiganticFeverCommand.executor,
      "minehead" -> MineHeadCommand.executor,
      "x-transfer" -> RegionOwnerTransferCommand.executor,
    )
      .concat(subsystems.flatMap(_.commands))
      .foreach {
        case (commandName, executor) => getCommand(commandName).setExecutor(executor)
      }

    val repositories = Seq(
      activeSkillAvailability,
      assaultSkillRoutines
    )

    //リスナーの登録
    Seq(
      new PlayerJoinListener(),
      new PlayerQuitListener(),
      new PlayerClickListener(),
      new PlayerBlockBreakListener(),
      new PlayerInventoryListener(),
      new EntityListener(),
      new PlayerPickupItemListener(),
      new PlayerDeathEventListener(),
      new GachaItemListener(),
      new RegionInventoryListener(),
      new WorldRegenListener(),
      new ChatInterceptor(List(globalChatInterceptionScope)),
      new MenuHandler()
    )
      .concat(repositories)
      .concat(subsystems.flatMap(_.listeners))
      .concat(playerItemMigrationControllerListeners)
      .foreach {
        getServer.getPluginManager.registerEvents(_, this)
      }

    //正月イベント用
    new NewYearsEvent(this)

    //オンラインの全てのプレイヤーを処理
    getServer.getOnlinePlayers.asScala.foreach { p =>
      try {
        //プレイヤーデータを生成
        SeichiAssist.playermap(p.getUniqueId) = SeichiAssist.databaseGateway
          .playerDataManipulator.loadPlayerData(p.getUniqueId, p.getName)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          p.kickPlayer("プレーヤーデータの読み込みに失敗しました。")
      }
    }

    //ランキングリストを最新情報に更新する
    if (!SeichiAssist.databaseGateway.playerDataManipulator.successRankingUpdate()) {
      logger.info("ランキングデータの作成に失敗しました。サーバーを停止します…")
      Bukkit.shutdown()
    }

    startRepeatedJobs()

    logger.info("SeichiAssist is Enabled!")

    SeichiAssist.buildAssist = new BuildAssist(this)
    SeichiAssist.buildAssist.onEnable()

    hasBeenLoadedAlready = true
    kickAllPlayersDueToInitialization.unsafeRunSync()
  }

  private def startRepeatedJobs(): Unit = {
    val startTask = {
      import PluginExecutionContexts._
      import cats.implicits._

      // 公共鯖(7)と建築鯖(8)なら整地量のランキングを表示する必要はない
      val programs: List[IO[Nothing]] =
        List(
          PlayerDataRecalculationRoutine(),
          PlayerDataBackupRoutine()
        ) ++
          Option.unless(
            SeichiAssist.seichiAssistConfig.getServerNum == 7
              || SeichiAssist.seichiAssistConfig.getServerNum == 8
          )(
            HalfHourRankingRoutine()
          ).toList

      implicit val ioParallel: Aux[IO, effect.IO.Par] = IO.ioParallel(asyncShift)
      programs.parSequence.start(asyncShift)
    }

    repeatedTaskFiber = Some(startTask.unsafeRunSync())
  }

  override def onDisable(): Unit = {
    val logger = getLogger

    cancelRepeatedJobs()

    // 管理下にある資源を開放する

    // ファイナライザはunsafeRunSyncによってこのスレッドで同期的に実行されるため
    // onDisable内で呼び出して問題はない。
    // https://scastie.scala-lang.org/NqT4BFw0TiyfjycWvzRIuQ
    lockedBlockChunkScope.getReleaseAllAction.unsafeRunSync().unsafeRunSync()
    arrowSkillProjectileScope.getReleaseAllAction.unsafeRunSync().unsafeRunSync()
    magicEffectEntityScope.getReleaseAllAction.unsafeRunSync().value.unsafeRunSync()

    expBottleStackSystem.state.managedBottleScope.getReleaseAllAction.unsafeRunSync().unsafeRunSync()

    //sqlコネクションチェック
    SeichiAssist.databaseGateway.ensureConnection()
    getServer.getOnlinePlayers.asScala.foreach { p =>
      //UUIDを取得
      val uuid = p.getUniqueId

      //プレイヤーデータ取得
      val playerData = SeichiAssist.playermap(uuid)

      //quit時とondisable時、プレイヤーデータを最新の状態に更新
      playerData.updateOnQuit()

      PlayerDataSaveTask.savePlayerData(playerData)
    }

    if (SeichiAssist.databaseGateway.disconnect() == ActionStatus.Fail) {
      logger.info("データベース切断に失敗しました")
    }

    logger.info("SeichiAssist is Disabled!")

    SeichiAssist.buildAssist.onDisable()
  }

  override def onCommand(sender: CommandSender, command: Command, label: String, args: Array[String]): Boolean
  = SeichiAssist.buildAssist.onCommand(sender, command, label, args)

  def restartRepeatedJobs(): Unit = {
    cancelRepeatedJobs()
    startRepeatedJobs()
  }

  private def cancelRepeatedJobs(): Unit = {
    repeatedTaskFiber match {
      case Some(x) => x.cancel.unsafeRunSync()
      case None =>
    }
  }
}

object SeichiAssist {
  val SEICHIWORLDNAME = "world_sw"
  val DEBUGWORLDNAME = "world"
  //Gachadataに依存するデータリスト
  val gachadatalist: mutable.ArrayBuffer[GachaPrize] = mutable.ArrayBuffer()
  //Playerdataに依存するデータリスト
  val playermap: mutable.HashMap[UUID, PlayerData] = mutable.HashMap()
  //総採掘量ランキング表示用データリスト
  val ranklist: mutable.ArrayBuffer[RankData] = mutable.ArrayBuffer()
  //プレイ時間ランキング表示用データリスト
  val ranklist_playtick: mutable.ArrayBuffer[RankData] = mutable.ArrayBuffer()
  //投票ポイント表示用データリスト
  val ranklist_p_vote: mutable.ArrayBuffer[RankData] = mutable.ArrayBuffer()
  //マナ妖精表示用のデータリスト
  val ranklist_p_apple: mutable.ArrayBuffer[RankData] = mutable.ArrayBuffer()

  var instance: SeichiAssist = _
  //デバッグフラグ(デバッグモード使用時はここで変更するのではなくconfig.ymlの設定値を変更すること！)
  var DEBUG = false
  //ガチャシステムのメンテナンスフラグ
  var gachamente = false
  // TODO staticであるべきではない
  var databaseGateway: DatabaseGateway = _
  var seichiAssistConfig: Config = _
  var buildAssist: BuildAssist = _
  //(minestackに格納する)Gachadataに依存するデータリスト
  val msgachadatalist: mutable.ArrayBuffer[MineStackGachaData] = mutable.ArrayBuffer()
  //総採掘量表示用
  var allplayerbreakblockint = 0L
  var allplayergiveapplelong = 0L

  object Scopes {
    implicit val globalChatInterceptionScope: InterceptionScope[UUID, String] = {
      import PluginExecutionContexts.asyncShift

      new InterceptionScope[UUID, String]()
    }
  }

  private def generateGachaPrizes(): List[MineStackObj] =
    msgachadatalist
      .toList
      .zipWithIndex
      .filter(_._1.itemStack.getType != Material.EXP_BOTTLE) //経験値瓶だけはすでにリストにあるので除外
      .map { case (g, i) =>
        new MineStackObj(g.objName, None, g.level, g.itemStack, true, i, MineStackObjectCategory.GACHA_PRIZES)
      }
}

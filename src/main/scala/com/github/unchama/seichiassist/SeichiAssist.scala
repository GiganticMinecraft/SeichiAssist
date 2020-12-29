package com.github.unchama.seichiassist

import akka.actor.ActorSystem
import cats.Parallel.Aux
import cats.effect
import cats.effect.{Clock, ConcurrentEffect, Fiber, IO, SyncIO, Timer}
import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.bungeesemaphoreresponder.domain.{PlayerDataFinalizer, PlayerDataFinalizerList}
import com.github.unchama.bungeesemaphoreresponder.{System => BungeeSemaphoreResponderSystem}
import com.github.unchama.chatinterceptor.{ChatInterceptor, InterceptionScope}
import com.github.unchama.datarepository.bukkit.player.{NonPersistentPlayerDataRefRepository, TryableFiberRepository}
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.ResourceScope.SingleResourceScope
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.menuinventory.MenuHandler
import com.github.unchama.seichiassist.MaterialSets.BlockBreakableBySkill
import com.github.unchama.seichiassist.SeichiAssist.seichiAssistConfig
import com.github.unchama.seichiassist.bungee.BungeeReceiver
import com.github.unchama.seichiassist.commands._
import com.github.unchama.seichiassist.commands.legacy.GachaCommand
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{asyncShift, cachedThreadPool}
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.data.{GachaPrize, MineStackGachaData, RankData}
import com.github.unchama.seichiassist.database.DatabaseGateway
import com.github.unchama.seichiassist.infrastructure.akka.ConfiguredActorSystemProvider
import com.github.unchama.seichiassist.infrastructure.logging.jul.NamedJULLogger
import com.github.unchama.seichiassist.infrastructure.scalikejdbc.ScalikeJDBCConfiguration
import com.github.unchama.seichiassist.listener._
import com.github.unchama.seichiassist.meta.subsystem.{StatefulSubsystem, Subsystem}
import com.github.unchama.seichiassist.minestack.{MineStackObj, MineStackObjectCategory}
import com.github.unchama.seichiassist.subsystems._
import com.github.unchama.seichiassist.subsystems.managedfly.InternalState
import com.github.unchama.seichiassist.subsystems.seasonalevents.api.SeasonalEventsAPI
import com.github.unchama.seichiassist.task.PlayerDataSaveTask
import com.github.unchama.seichiassist.task.global._
import com.github.unchama.util.{ActionStatus, ClassUtils}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.bukkit.ChatColor._
import org.bukkit.entity.{Entity, Player, Projectile}
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.{Bukkit, Material}
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.impl.JDK14LoggerFactory

import java.util.UUID
import java.util.logging.LogManager
import scala.collection.mutable
import scala.jdk.CollectionConverters._

class SeichiAssist extends JavaPlugin() {
  SeichiAssist.instance = this

  private var hasBeenLoadedAlready = false

  //region logging infrastructure

  /*
   * JDK14LoggerFactoryは `java.util.logging.Logger.getLogger` によりロガーを解決している。
   * しかし、プラグインロガーが何故か `getLogger.getName` により解決ができず、
   * このため `NamedJULLogger` で一旦ラップしたものをLogManagerに登録し、
   * それをSlf4jにアダプトしたロガーを利用している。
   */
  implicit val logger: Logger = {
    // TODO すべてのロギングをlog4cats経由で行えばこれは要らなさそう？
    //      適当なルートロガーをJUL経由で引っ張ってきてPrefixedLogger("[SeichiAssist]")を使う
    val pluginLogger = getLogger
    val customLoggerName = s"seichi_assist_custom_named_logger_at_${pluginLogger.getName}"
    val newLogger = new NamedJULLogger(customLoggerName, pluginLogger)

    LogManager.getLogManager.addLogger(newLogger)
    new JDK14LoggerFactory().getLogger(newLogger.getName)
  }

  implicit val loggerF: io.chrisdavenport.log4cats.Logger[IO] = Slf4jLogger.getLoggerFromSlf4j(logger)

  //endregion

  val expBarSynchronization = new ExpBarSynchronization()
  private var repeatedTaskFiber: Option[Fiber[IO, List[Nothing]]] = None

  //region resource scopes

  val arrowSkillProjectileScope: ResourceScope[IO, SyncIO, Projectile] = {
    import PluginExecutionContexts.asyncShift
    ResourceScope.unsafeCreate
  }

  val magicEffectEntityScope: SingleResourceScope[IO, SyncIO, Entity] = {
    import PluginExecutionContexts.asyncShift
    ResourceScope.unsafeCreateSingletonScope
  }
  //endregion

  //region subsystems

  lazy val expBottleStackSystem: StatefulSubsystem[IO, subsystems.expbottlestack.InternalState[IO, SyncIO]] = {
    import PluginExecutionContexts.asyncShift
    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    subsystems.expbottlestack.System.wired[IO, SyncIO, IO]
  }.unsafeRunSync()

  lazy val itemMigrationSystem: StatefulSubsystem[IO, subsystems.itemmigration.InternalState[IO]] = {
    import PluginExecutionContexts.asyncShift
    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    subsystems.itemmigration.System.wired[IO, SyncIO, IO]
  }.unsafeRunSync()

  lazy val managedFlySystem: StatefulSubsystem[IO, subsystems.managedfly.InternalState[SyncIO]] = {
    import PluginExecutionContexts.{asyncShift, cachedThreadPool, syncShift}

    implicit val effectEnvironment: DefaultEffectEnvironment.type = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val timer: Timer[IO] = IO.timer(cachedThreadPool)

    val configuration = subsystems.managedfly.application.SystemConfiguration(
      expConsumptionAmount = seichiAssistConfig.getFlyExp
    )

    subsystems.managedfly.System.wired[IO, SyncIO](configuration).unsafeRunSync().coerceFinalizationContextTo[IO]
  }

  lazy val autoSaveSystem: StatefulSubsystem[IO, List[IO[Nothing]]] = {
    import PluginExecutionContexts.{syncShift, timer}

    val configuration = seichiAssistConfig.getAutoSaveSystemConfiguration

    subsystems.autosave.System.wired[IO, IO](configuration)
  }

  lazy val bookedAchievementSystem: Subsystem[IO] = {
    import PluginExecutionContexts.asyncShift

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)

    subsystems.bookedachivement.System.wired[IO, IO]
  }

  lazy val seasonalEventsSystem: subsystems.seasonalevents.System[IO] = {
    import PluginExecutionContexts.asyncShift

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)

    subsystems.seasonalevents.System.wired[IO, IO](this)
  }

  lazy val bungeeSemaphoreResponderSystem: BungeeSemaphoreResponderSystem[IO] = {
    import cats.implicits._
    implicit val timer: Timer[IO] = IO.timer(cachedThreadPool)
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val systemConfiguration: com.github.unchama.bungeesemaphoreresponder.Configuration =
      seichiAssistConfig.getBungeeSemaphoreSystemConfiguration

    val savePlayerData: PlayerDataFinalizer[IO, Player] = { player =>
      IO {
        import scala.util.chaining._
        SeichiAssist.playermap.remove(player.getUniqueId).get.tap(_.updateOnQuit())
      } >>= (playerData =>
        PlayerDataSaveTask.savePlayerData[IO](player, playerData)
        )
    }

    val playerDataFinalizers = PlayerDataFinalizerList[IO, Player](
      managedFlySystem.managedFinalizers
    ).withAnotherFinalizer(savePlayerData)

    new BungeeSemaphoreResponderSystem(playerDataFinalizers, PluginExecutionContexts.asyncShift)
  }

  //endregion

  private implicit val _akkaSystem: ActorSystem = ConfiguredActorSystemProvider("reference.conf").provide()

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

  /**
   * プラグインを初期化する。ここで例外が投げられるとBukkitがシャットダウンされる。
   */
  private def monitoredInitialization(): Unit = {
    /**
     * Spigotサーバーが開始されるときにはまだPreLoginEventがcatchされない等色々な不都合があるので、
     * SeichiAssistの初期化はプレーヤーが居ないことを前提として進めることとする。
     *
     * NOTE:
     * PreLoginToQuitPlayerDataRepository に関してはJoinEventさえcatchできれば弾けるので、
     * 接続を試みているプレーヤーは弾かないで良さそう、と言うか弾く術がない
     */
    kickAllPlayersDueToInitialization.unsafeRunSync()

    if (hasBeenLoadedAlready) {
      throw new IllegalStateException("SeichiAssistは2度enableされることを想定されていません！シャットダウンします…")
    }

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    implicit val syncClock: Clock[SyncIO] = Clock.create[SyncIO]
    implicit val syncSeasonalEventsSystemAPI: SeasonalEventsAPI[SyncIO] = seasonalEventsSystem.api[SyncIO]

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

    itemMigrationSystem.state.entryPoints.runDatabaseMigration[SyncIO].unsafeRunSync()
    itemMigrationSystem.state.entryPoints.runWorldMigration.unsafeRunSync()

    SeichiAssist.databaseGateway = DatabaseGateway.createInitializedInstance(
      SeichiAssist.seichiAssistConfig.getURL, SeichiAssist.seichiAssistConfig.getDB,
      SeichiAssist.seichiAssistConfig.getID, SeichiAssist.seichiAssistConfig.getPW
    )

    //mysqlからガチャデータ読み込み
    if (!SeichiAssist.databaseGateway.gachaDataManipulator.loadGachaData()) {
      throw new Exception("ガチャデータのロードに失敗しました。サーバーを停止します…")
    }

    //mysqlからMineStack用ガチャデータ読み込み
    if (!SeichiAssist.databaseGateway.mineStackGachaDataManipulator.loadMineStackGachaData()) {
      throw new Exception("MineStack用ガチャデータのロードに失敗しました。サーバーを停止します…")
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

    val subsystems = Seq(
      mebius.System.wired,
      expBottleStackSystem,
      itemMigrationSystem,
      managedFlySystem,
      rescueplayer.System.wired,
      bookedAchievementSystem,
      seasonalEventsSystem
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
      "halfguard" -> HalfBlockProtectCommand.executor,
      "contribute" -> ContributeCommand.executor,
      "subhome" -> SubHomeCommand.executor,
      "gtfever" -> GiganticFeverCommand.executor,
      "minehead" -> MineHeadCommand.executor,
      "x-transfer" -> RegionOwnerTransferCommand.executor,
      "stickmenu" -> StickMenuCommand.executor
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
    val listeners = Seq(
      new PlayerJoinListener(),
      new ExpBarDesynchronizationListener(),
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
      new MenuHandler(),
      PlayerSeichiLevelUpListener,
      SpawnRegionProjectileInterceptor,
    )
      .concat(bungeeSemaphoreResponderSystem.listenersToBeRegistered)
      .concat(repositories)
      .concat(subsystems.flatMap(_.listeners))

    listeners.foreach {
      getServer.getPluginManager.registerEvents(_, this)
    }

    //ランキングリストを最新情報に更新する
    if (!SeichiAssist.databaseGateway.playerDataManipulator.successRankingUpdate()) {
      throw new RuntimeException("ランキングデータの作成に失敗しました。サーバーを停止します…")
    }

    startRepeatedJobs()

    SeichiAssist.buildAssist = {
      implicit val flySystem: StatefulSubsystem[IO, InternalState[SyncIO]] = managedFlySystem
      new BuildAssist(this)
    }
    SeichiAssist.buildAssist.onEnable()

    hasBeenLoadedAlready = true
    kickAllPlayersDueToInitialization.unsafeRunSync()

    logger.info("SeichiAssistが有効化されました！")
  }

  override def onEnable(): Unit = {
    try {
      monitoredInitialization()
    } catch {
      case e: Exception =>
        logger.error("初期化処理に失敗しました。シャットダウンしています…")
        e.printStackTrace()
        Bukkit.shutdown()
    }
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
          ).toList ++ autoSaveSystem.state

      implicit val ioParallel: Aux[IO, effect.IO.Par] = IO.ioParallel(asyncShift)
      programs.parSequence.start(asyncShift)
    }

    repeatedTaskFiber = Some(startTask.unsafeRunSync())
  }

  override def onDisable(): Unit = {
    cancelRepeatedJobs()

    // 管理下にある資源を開放する

    // ファイナライザはunsafeRunSyncによってこのスレッドで同期的に実行されるため
    // onDisable内で呼び出して問題はない。
    // https://scastie.scala-lang.org/NqT4BFw0TiyfjycWvzRIuQ
    lockedBlockChunkScope.getReleaseAllAction.unsafeRunSync().unsafeRunSync()
    arrowSkillProjectileScope.getReleaseAllAction.unsafeRunSync().unsafeRunSync()
    magicEffectEntityScope.getReleaseAllAction.unsafeRunSync().value.unsafeRunSync()

    expBottleStackSystem.state.managedBottleScope.getReleaseAllAction.unsafeRunSync().unsafeRunSync()

    import cats.implicits._

    // BungeeSemaphoreResponderの全ファイナライザを走らせる
    getServer
      .getOnlinePlayers.asScala.toList
      .flatMap(player => bungeeSemaphoreResponderSystem.playerFinalizerList.allActionsOnQuitOf(player).toList)
      .sequence
      .unsafeRunSync()

    if (SeichiAssist.databaseGateway.disconnect() == ActionStatus.Fail) {
      logger.info("データベース切断に失敗しました")
    }

    SeichiAssist.buildAssist.onDisable()

    logger.info("SeichiAssistが無効化されました!")
  }

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
  // TODO deprecate this
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

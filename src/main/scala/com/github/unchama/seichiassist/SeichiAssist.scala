package com.github.unchama.seichiassist

import akka.actor.ActorSystem
import cats.Parallel.Aux
import cats.effect
import cats.effect.concurrent.Ref
import cats.effect.{Clock, ConcurrentEffect, Fiber, IO, SyncIO, Timer}
import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.buildassist.menu.BuildAssistMenuRouter
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.bungeesemaphoreresponder.{System => BungeeSemaphoreResponderSystem}
import com.github.unchama.chatinterceptor.{ChatInterceptor, InterceptionScope}
import com.github.unchama.concurrent.RepeatingRoutine
import com.github.unchama.datarepository.bukkit.player.{BukkitRepositoryControls, PlayerDataRepository}
import com.github.unchama.datarepository.definitions.SessionMutexRepositoryDefinition
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.SinglePhasedRepositoryInitialization
import com.github.unchama.generic.effect.ResourceScope
import com.github.unchama.generic.effect.ResourceScope.SingleResourceScope
import com.github.unchama.generic.effect.concurrent.SessionMutex
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.menuinventory.MenuHandler
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.minecraft.actions.{GetConnectedPlayers, SendMinecraftMessage}
import com.github.unchama.minecraft.bukkit.actions.{GetConnectedBukkitPlayers, SendBukkitMessage}
import com.github.unchama.seichiassist.MaterialSets.BlockBreakableBySkill
import com.github.unchama.seichiassist.SeichiAssist.seichiAssistConfig
import com.github.unchama.seichiassist.bungee.BungeeReceiver
import com.github.unchama.seichiassist.commands._
import com.github.unchama.seichiassist.commands.legacy.{DonationCommand, GachaCommand, VoteCommand}
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.asyncShift
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.data.{GachaPrize, MineStackGachaData, RankData}
import com.github.unchama.seichiassist.database.DatabaseGateway
import com.github.unchama.seichiassist.domain.actions.{GetNetworkConnectionCount, UuidToLastSeenName}
import com.github.unchama.seichiassist.domain.configuration.RedisBungeeRedisConfiguration
import com.github.unchama.seichiassist.infrastructure.akka.ConfiguredActorSystemProvider
import com.github.unchama.seichiassist.infrastructure.logging.jul.NamedJULLogger
import com.github.unchama.seichiassist.infrastructure.redisbungee.RedisBungeeNetworkConnectionCount
import com.github.unchama.seichiassist.infrastructure.scalikejdbc.ScalikeJDBCConfiguration
import com.github.unchama.seichiassist.listener._
import com.github.unchama.seichiassist.menus.{BuildMainMenu, TopLevelRouter}
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.minestack.{MineStackObj, MineStackObjectCategory}
import com.github.unchama.seichiassist.subsystems._
import com.github.unchama.seichiassist.subsystems.breakcount.{BreakCountAPI, BreakCountReadAPI}
import com.github.unchama.seichiassist.subsystems.breakcountbar.BreakCountBarAPI
import com.github.unchama.seichiassist.subsystems.buildcount.BuildCountAPI
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.Configuration
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.{FastDiggingEffectApi, FastDiggingSettingsApi}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.FourDimensionalPocketApi
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.mana.{ManaApi, ManaReadApi}
import com.github.unchama.seichiassist.subsystems.managedfly.ManagedFlyApi
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import com.github.unchama.seichiassist.subsystems.present.infrastructure.GlobalPlayerAccessor
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

  import cats.implicits._

  SeichiAssist.instance = this

  private var hasBeenLoadedAlready = false

  //region application infrastructure

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

  private var repeatedTaskFiber: Option[Fiber[IO, List[Nothing]]] = None

  //region repositories

  private val activeSkillAvailabilityRepositoryControls: BukkitRepositoryControls[SyncIO, Ref[SyncIO, Boolean]] =
    BukkitRepositoryControls.createHandles[SyncIO, Ref[SyncIO, Boolean]](
      RepositoryDefinition.SinglePhased.withoutTappingAction(
        SinglePhasedRepositoryInitialization.withSupplier(Ref[SyncIO].of(true)),
        RepositoryFinalization.trivial
      )
    ).unsafeRunSync()

  val activeSkillAvailability: PlayerDataRepository[Ref[SyncIO, Boolean]] =
    activeSkillAvailabilityRepositoryControls.repository

  private val assaultSkillRoutinesRepositoryControls: BukkitRepositoryControls[SyncIO, SessionMutex[IO, SyncIO]] = {
    val definition = {
      import PluginExecutionContexts.asyncShift
      SessionMutexRepositoryDefinition.withRepositoryContext[IO, SyncIO, Player]
    }

    BukkitRepositoryControls.createHandles(definition).unsafeRunSync()
  }

  val assaultSkillRoutines: PlayerDataRepository[SessionMutex[IO, SyncIO]] =
    assaultSkillRoutinesRepositoryControls.repository

  private val kickAllPlayersDueToInitialization: SyncIO[Unit] = SyncIO {
    getServer.getOnlinePlayers.asScala.foreach { player =>
      player.kickPlayer("プラグインを初期化しています。時間を置いて再接続してください。")
    }
  }

  //endregion

  //region resource scopes

  /**
   * スキル使用などで破壊されることが確定したブロック塊のスコープ
   *
   * TODO: `ResourceScope[IO, SyncIO, Set[BlockBreakableBySkill]]` にしたい
   */
  val lockedBlockChunkScope: ResourceScope[IO, IO, Set[BlockBreakableBySkill]] = {
    import PluginExecutionContexts.asyncShift
    ResourceScope.unsafeCreate
  }

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

  private lazy val expBottleStackSystem: subsystems.expbottlestack.System[IO, SyncIO, IO] = {
    import PluginExecutionContexts.asyncShift
    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    subsystems.expbottlestack.System.wired[IO, SyncIO, IO].unsafeRunSync()
  }

  private lazy val itemMigrationSystem: subsystems.itemmigration.System[IO] = {
    import PluginExecutionContexts.asyncShift
    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    subsystems.itemmigration.System.wired[IO, SyncIO].unsafeRunSync()
  }

  private lazy val managedFlySystem: subsystems.managedfly.System[SyncIO, IO] = {
    import PluginExecutionContexts.{asyncShift, cachedThreadPool, onMainThread}

    implicit val effectEnvironment: DefaultEffectEnvironment.type = DefaultEffectEnvironment
    implicit val timer: Timer[IO] = IO.timer(cachedThreadPool)

    val configuration = subsystems.managedfly.application.SystemConfiguration(
      expConsumptionAmount = seichiAssistConfig.getFlyExp
    )

    subsystems.managedfly.System.wired[IO, SyncIO](configuration).unsafeRunSync()
  }

  private lazy val bookedAchievementSystem: Subsystem[IO] = {
    import PluginExecutionContexts.asyncShift

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)

    subsystems.bookedachivement.System.wired[IO, IO]
  }

  private lazy val buildCountSystem: subsystems.buildcount.System[IO, SyncIO] = {
    import PluginExecutionContexts.timer

    implicit val configuration: subsystems.buildcount.application.Configuration =
      seichiAssistConfig.buildCountConfiguration

    subsystems.buildcount.System.wired[IO, SyncIO](loggerF).unsafeRunSync()
  }

  // TODO コンテキスト境界明確化のため、privateであるべきである
  lazy val breakCountSystem: subsystems.breakcount.System[IO, SyncIO] = {
    import PluginExecutionContexts.{asyncShift, onMainThread}

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)

    subsystems.breakcount.System.wired[IO, SyncIO].unsafeRunSync()
  }

  private lazy val manaSystem: subsystems.mana.System[IO, SyncIO, Player] = {
    implicit val breakCountApi: BreakCountAPI[IO, SyncIO, Player] = breakCountSystem.api

    subsystems.mana.System.wired[IO, SyncIO].unsafeRunSync()
  }

  private lazy val manaBarSystem: Subsystem[IO] = {
    implicit val manaApi: ManaReadApi[IO, SyncIO, Player] = manaSystem.manaApi

    subsystems.manabar.System.wired[IO, SyncIO].unsafeRunSync()
  }

  private lazy val seasonalEventsSystem: subsystems.seasonalevents.System[IO] = {
    import PluginExecutionContexts.{asyncShift, onMainThread}

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val manaApi: ManaApi[IO, SyncIO, Player] = manaSystem.manaApi

    subsystems.seasonalevents.System.wired[IO, SyncIO, IO](this)
  }

  private lazy val breakCountBarSystem: subsystems.breakcountbar.System[IO, SyncIO, Player] = {
    subsystems.breakcountbar.System.wired[SyncIO, IO](breakCountSystem.api).unsafeRunSync()
  }

  // TODO コンテキスト境界明確化のため、privateであるべきである
  implicit lazy val rankingSystemApi: subsystems.ranking.api.AssortedRankingApi[IO] = {
    import PluginExecutionContexts.{asyncShift, timer}

    subsystems.ranking.System.wired[IO, IO].unsafeRunSync()
  }

  private lazy val fourDimensionalPocketSystem: subsystems.fourdimensionalpocket.System[IO, Player] = {
    import PluginExecutionContexts.{asyncShift, onMainThread}

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)

    subsystems.fourdimensionalpocket.System.wired[IO, SyncIO](breakCountSystem.api).unsafeRunSync()
  }

  private lazy val fastDiggingEffectSystem: subsystems.fastdiggingeffect.System[IO, IO, Player] = {
    import PluginExecutionContexts.{asyncShift, onMainThread, timer}

    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val configuration: Configuration = seichiAssistConfig.getFastDiggingEffectSystemConfiguration
    implicit val breakCountApi: BreakCountAPI[IO, SyncIO, Player] = breakCountSystem.api
    implicit val getConnectedPlayers: GetConnectedPlayers[IO, Player] = new GetConnectedBukkitPlayers[IO]
    implicit val redisBungeeConfig: RedisBungeeRedisConfiguration = seichiAssistConfig.getRedisBungeeRedisConfiguration
    implicit val networkConnectionCount: GetNetworkConnectionCount[IO] = new RedisBungeeNetworkConnectionCount[IO](asyncShift)

    subsystems.fastdiggingeffect.System.wired[SyncIO, IO, SyncIO].unsafeRunSync()
  }

  private lazy val gachaPointSystem: subsystems.gachapoint.System[IO, SyncIO, Player] = {
    import PluginExecutionContexts.{asyncShift, onMainThread, timer}

    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val getConnectedPlayers: GetConnectedPlayers[IO, Player] = new GetConnectedBukkitPlayers[IO]

    subsystems.gachapoint.System.wired[IO, SyncIO](breakCountSystem.api).unsafeRunSync()
  }

  private lazy val mebiusSystem: Subsystem[IO] = {
    import PluginExecutionContexts.{onMainThread, sleepAndRoutineContext, timer}

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val syncClock: Clock[SyncIO] = Clock.create[SyncIO]
    implicit val syncSeasonalEventsSystemAPI: SeasonalEventsAPI[SyncIO] = seasonalEventsSystem.api[SyncIO]

    subsystems.mebius.System.wired[IO, SyncIO].unsafeRunSync()
  }

  private implicit lazy val discordNotificationSystem: subsystems.discordnotification.System[IO] = {
    import PluginExecutionContexts.asyncShift

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)

    subsystems.discordnotification.System.wired[IO](seichiAssistConfig.discordNotificationConfiguration)
  }

  private lazy val wiredSubsystems: List[Subsystem[IO]] = List(
    mebiusSystem,
    expBottleStackSystem,
    itemMigrationSystem,
    managedFlySystem,
    rescueplayer.System.wired,
    bookedAchievementSystem,
    seasonalEventsSystem,
    breakCountSystem,
    breakCountBarSystem,
    manaSystem,
    manaBarSystem,
    buildCountSystem,
    fastDiggingEffectSystem,
    fourDimensionalPocketSystem,
    gachaPointSystem,
    discordNotificationSystem,
  )

  private lazy val buildAssist: BuildAssist = {
    implicit val flyApi: ManagedFlyApi[SyncIO, Player] = managedFlySystem.api
    implicit val buildCountAPI: BuildCountAPI[SyncIO, Player] = buildCountSystem.api
    implicit val manaApi: ManaApi[IO, SyncIO, Player] = manaSystem.manaApi

    new BuildAssist(this)
  }

  private lazy val bungeeSemaphoreResponderSystem: BungeeSemaphoreResponderSystem[IO] = {
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

    import PluginExecutionContexts.timer

    new BungeeSemaphoreResponderSystem(
      PlayerDataFinalizer.concurrently[IO, Player](
        Seq(
          savePlayerData,
          assaultSkillRoutinesRepositoryControls.finalizer.coerceContextTo[IO],
          activeSkillAvailabilityRepositoryControls.finalizer.coerceContextTo[IO]
        )
          .appendedAll(wiredSubsystems.flatMap(_.managedFinalizers))
          .appendedAll(wiredSubsystems.flatMap(_.managedRepositoryControls.map(_.finalizer)))
          .toList
      ),
      PluginExecutionContexts.asyncShift
    )
  }

  lazy val presentSystem: Subsystem[IO] = {
    import PluginExecutionContexts.{asyncShift, onMainThread}

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val uuidToLastSeenName: UuidToLastSeenName[IO] = new GlobalPlayerAccessor[IO]
    subsystems.present.System.wired
  }

  //endregion

  private implicit val _akkaSystem: ActorSystem = ConfiguredActorSystemProvider("reference.conf").provide()

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

    itemMigrationSystem.entryPoints.runDatabaseMigration[SyncIO].unsafeRunSync()
    itemMigrationSystem.entryPoints.runWorldMigration.unsafeRunSync()

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
    implicit val breakCountApi: BreakCountAPI[IO, SyncIO, Player] = breakCountSystem.api
    implicit val breakCountBarApi: BreakCountBarAPI[SyncIO, Player] = breakCountBarSystem.api
    implicit val fastDiggingEffectApi: FastDiggingEffectApi[IO, Player] = fastDiggingEffectSystem.effectApi
    implicit val fastDiggingSettingsApi: FastDiggingSettingsApi[IO, Player] = fastDiggingEffectSystem.settingsApi
    implicit val fourDimensionalPocketApi: FourDimensionalPocketApi[IO, Player] = fourDimensionalPocketSystem.api
    implicit val gachaPointApi: GachaPointApi[IO, SyncIO, Player] = gachaPointSystem.api
    implicit val manaApi: ManaApi[IO, SyncIO, Player] = manaSystem.manaApi
    implicit val globalNotification: DiscordNotificationAPI[IO] = discordNotificationSystem.globalNotification
    val menuRouter = TopLevelRouter.apply
    import menuRouter.canOpenStickMenu

    MineStackObjectList.minestackGachaPrizes ++= SeichiAssist.generateGachaPrizes()

    MineStackObjectList.minestacklist.clear()
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistmine
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistdrop
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistfarm
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistbuild
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestacklistrs
    MineStackObjectList.minestacklist ++= MineStackObjectList.minestackGachaPrizes

    import SeichiAssist.Scopes.globalChatInterceptionScope

    buildAssist.onEnable()

    implicit val managedFlyApi: ManagedFlyApi[SyncIO, Player] = managedFlySystem.api
    // 本来は曖昧さ回避のためにRouterのインスタンスを生成するべきではないが、生成を回避しようとすると
    // 巨大な変更が必要となる。そのため、Routerのインスタンスを新しく生成することで、それまでの間
    // 機能を果たそうとするものである。
    implicit val canOpenBuildMainMenu: CanOpen[IO, BuildMainMenu.type] = BuildAssistMenuRouter.apply.canOpenBuildMainMenu
    // コマンドの登録
    Map(
      "gacha" -> new GachaCommand(),
      "vote" -> new VoteCommand,
      "donation" -> new DonationCommand,
      "map" -> MapCommand.executor,
      "ef" -> new EffectCommand(fastDiggingEffectSystem.settingsApi).executor,
      "seichiassist" -> SeichiAssistCommand.executor,
      "lastquit" -> LastQuitCommand.executor,
      "stick" -> StickCommand.executor,
      "rmp" -> RmpCommand.executor,
      "shareinv" -> ShareInvCommand.executor,
      "halfguard" -> HalfBlockProtectCommand.executor,
      "subhome" -> SubHomeCommand.executor,
      "gtfever" -> GiganticFeverCommand.executor,
      "minehead" -> new MineHeadCommand().executor,
      "x-transfer" -> RegionOwnerTransferCommand.executor,
      "stickmenu" -> StickMenuCommand.executor,
      "hat" -> HatCommand.executor
    )
      .concat(wiredSubsystems.flatMap(_.commands))
      .foreach {
        case (commandName, executor) => getCommand(commandName).setExecutor(executor)
      }

    import menuRouter.canOpenAchievementMenu
    //リスナーの登録
    val listeners = Seq(
      new PlayerJoinListener(),
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
      SpawnRegionProjectileInterceptor,
    )
      .concat(bungeeSemaphoreResponderSystem.listenersToBeRegistered)
      .concat {
        Seq(
          activeSkillAvailabilityRepositoryControls.initializer,
          assaultSkillRoutinesRepositoryControls.initializer
        )
      }
      .concat(wiredSubsystems.flatMap(_.listeners))
      .concat(wiredSubsystems.flatMap(_.managedRepositoryControls.map(_.initializer)))

    listeners.foreach {
      getServer.getPluginManager.registerEvents(_, this)
    }

    //ランキングリストを最新情報に更新する
    if (!SeichiAssist.databaseGateway.playerDataManipulator.successRankingUpdate()) {
      throw new RuntimeException("ランキングデータの作成に失敗しました。サーバーを停止します…")
    }

    startRepeatedJobs()

    // サブシステムのリポジトリのバックアップ処理を走らせる
    {
      import PluginExecutionContexts.{asyncShift, timer}

      import scala.concurrent.duration._

      implicit val ioConcurrent: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)

      val interval = IO.pure(1.minute)

      RepeatingRoutine
        .foreverMRecovering {
          wiredSubsystems.flatMap(_.managedRepositoryControls.map(_.backupProcess)).sequence
        }(interval)
        .start(asyncShift)
        .unsafeRunSync()
    }

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
      val dataRecalculationRoutine = {
        import PluginExecutionContexts._
        implicit val manaApi: ManaApi[IO, SyncIO, Player] = manaSystem.manaApi
        PlayerDataRecalculationRoutine()
      }

      val dataBackupRoutine = {
        import PluginExecutionContexts._
        PlayerDataBackupRoutine()
      }

      import PluginExecutionContexts._

      implicit val breakCountApi: BreakCountReadAPI[IO, SyncIO, Player] = breakCountSystem.api
      implicit val manaApi: ManaApi[IO, SyncIO, Player] = manaSystem.manaApi
      implicit val fastDiggingEffectApi: FastDiggingEffectApi[IO, Player] = fastDiggingEffectSystem.effectApi
      implicit val ioConcurrent: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
      implicit val sendMessages: SendMinecraftMessage[IO, Player] = new SendBukkitMessage[IO]

      val dragonNightTimeProcess: IO[Nothing] =
        subsystems.dragonnighttime.System.backgroundProcess[IO, SyncIO, Player]

      val halfHourRankingRoutineOption: Option[IO[Nothing]] =
      // 公共鯖(7)と建築鯖(8)なら整地量のランキングを表示する必要はない
        Option.unless(Set(7, 8).contains(SeichiAssist.seichiAssistConfig.getServerNum)) {
          subsystems.halfhourranking.System.backgroundProcess[IO, SyncIO]
        }

      val levelUpGiftProcess: IO[Nothing] =
        subsystems.seichilevelupgift.System.backGroundProcess[IO, SyncIO]

      val levelUpMessagesProcess: IO[Nothing] =
        subsystems.seichilevelupmessage.System.backgroundProcess[IO, SyncIO, Player]

      val autoSaveProcess: IO[Nothing] = {
        val configuration = seichiAssistConfig.getAutoSaveSystemConfiguration

        subsystems.autosave.System.backgroundProcess[IO, IO](configuration)
      }

      val programs: List[IO[Nothing]] =
        List(
          dataRecalculationRoutine,
          dataBackupRoutine,
          levelUpGiftProcess,
          dragonNightTimeProcess,
          levelUpMessagesProcess,
          autoSaveProcess
        ) ++
          halfHourRankingRoutineOption.toList

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

    expBottleStackSystem.managedBottleScope.getReleaseAllAction.unsafeRunSync().unsafeRunSync()

    // BungeeSemaphoreResponderの全ファイナライザを走らせる
    getServer
      .getOnlinePlayers.asScala.toList
      .traverse(bungeeSemaphoreResponderSystem.finalizer.onQuitOf)
      .unsafeRunSync()

    if (SeichiAssist.databaseGateway.disconnect() == ActionStatus.Fail) {
      logger.info("データベース切断に失敗しました")
    }

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

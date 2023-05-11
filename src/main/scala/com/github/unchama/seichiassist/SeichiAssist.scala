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
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
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
import com.github.unchama.minecraft.bukkit.actions.{
  GetConnectedBukkitPlayers,
  SendBukkitMessage
}
import com.github.unchama.seichiassist.MaterialSets.BlockBreakableBySkill
import com.github.unchama.seichiassist.SeichiAssist.seichiAssistConfig
import com.github.unchama.seichiassist.bungee.BungeeReceiver
import com.github.unchama.seichiassist.commands._
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{
  asyncShift,
  onMainThread
}
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.database.DatabaseGateway
import com.github.unchama.seichiassist.domain.actions.{
  GetNetworkConnectionCount,
  UuidToLastSeenName
}
import com.github.unchama.seichiassist.domain.configuration.RedisBungeeRedisConfiguration
import com.github.unchama.seichiassist.infrastructure.akka.ConfiguredActorSystemProvider
import com.github.unchama.seichiassist.infrastructure.logging.jul.NamedJULLogger
import com.github.unchama.seichiassist.infrastructure.redisbungee.RedisBungeeNetworkConnectionCount
import com.github.unchama.seichiassist.infrastructure.scalikejdbc.ScalikeJDBCConfiguration
import com.github.unchama.seichiassist.listener._
import com.github.unchama.seichiassist.menus.minestack.CategorizedMineStackMenu
import com.github.unchama.seichiassist.menus.{BuildMainMenu, TopLevelRouter}
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems._
import com.github.unchama.seichiassist.subsystems.anywhereender.AnywhereEnderChestAPI
import com.github.unchama.seichiassist.subsystems.breakcount.{BreakCountAPI, BreakCountReadAPI}
import com.github.unchama.seichiassist.subsystems.breakcountbar.BreakCountBarAPI
import com.github.unchama.seichiassist.subsystems.buildcount.BuildCountAPI
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import com.github.unchama.seichiassist.subsystems.donate.DonatePremiumPointAPI
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.Configuration
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.{
  FastDiggingEffectApi,
  FastDiggingSettingsApi
}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.FourDimensionalPocketApi
import com.github.unchama.seichiassist.subsystems.gacha.GachaDrawAPI
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.consumegachaticket.ConsumeGachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.GachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.idletime.IdleTimeAPI
import com.github.unchama.seichiassist.subsystems.home.HomeReadAPI
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import com.github.unchama.seichiassist.subsystems.itemmigration.infrastructure.minecraft.JdbcBackedUuidRepository
import com.github.unchama.seichiassist.subsystems.mana.{ManaApi, ManaReadApi}
import com.github.unchama.seichiassist.subsystems.managedfly.ManagedFlyApi
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.minestack.bukkit.MineStackCommand
import com.github.unchama.seichiassist.subsystems.present.infrastructure.GlobalPlayerAccessor
import com.github.unchama.seichiassist.subsystems.seasonalevents.api.SeasonalEventsAPI
import com.github.unchama.seichiassist.subsystems.sharedinventory.SharedInventoryAPI
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gttosiina.GtToSiinaAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairyspeech.FairySpeechAPI
import com.github.unchama.seichiassist.task.PlayerDataSaveTask
import com.github.unchama.seichiassist.task.global._
import com.github.unchama.util.{ActionStatus, ClassUtils}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.sentry.Sentry
import io.sentry.SentryLevel
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.entity.{Entity, Player, Projectile}
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
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

  // region application infrastructure

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

  implicit val loggerF: io.chrisdavenport.log4cats.Logger[IO] =
    Slf4jLogger.getLoggerFromSlf4j(logger)

  // endregion

  private var repeatedTaskFiber: Option[Fiber[IO, List[Nothing]]] = None

  // region repositories

  private val activeSkillAvailabilityRepositoryControls
    : BukkitRepositoryControls[SyncIO, Ref[SyncIO, Boolean]] =
    BukkitRepositoryControls
      .createHandles[SyncIO, Ref[SyncIO, Boolean]](
        RepositoryDefinition
          .Phased
          .SinglePhased
          .withoutTappingAction(
            SinglePhasedRepositoryInitialization.withSupplier(Ref[SyncIO].of(true)),
            RepositoryFinalization.trivial
          )
      )
      .unsafeRunSync()

  val activeSkillAvailability: PlayerDataRepository[Ref[SyncIO, Boolean]] =
    activeSkillAvailabilityRepositoryControls.repository

  private val assaultSkillRoutinesRepositoryControls
    : BukkitRepositoryControls[SyncIO, SessionMutex[IO, SyncIO]] = {
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

  // endregion

  // region resource scopes

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
  // endregion

  // region subsystems

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

    implicit val timer: Timer[IO] = IO.timer(cachedThreadPool)

    val configuration = subsystems
      .managedfly
      .application
      .SystemConfiguration(expConsumptionAmount = seichiAssistConfig.getFlyExp)

    implicit val idleTimeAPI: IdleTimeAPI[IO, Player] = idleTimeSystem.api

    subsystems.managedfly.System.wired[IO, SyncIO](configuration).unsafeRunSync()
  }

  private lazy val bookedAchievementSystem: Subsystem[IO] = {
    import PluginExecutionContexts.asyncShift

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)

    subsystems.bookedachivement.System.wired[IO, IO]
  }

  private lazy val buildCountSystem: subsystems.buildcount.System[IO, SyncIO] = {
    import PluginExecutionContexts.asyncShift

    implicit val configuration: subsystems.buildcount.application.Configuration =
      seichiAssistConfig.buildCountConfiguration

    implicit val syncIoClock: Clock[SyncIO] = Clock.create

    implicit val globalNotification: DiscordNotificationAPI[IO] =
      discordNotificationSystem.globalNotification

    subsystems.buildcount.System.wired[IO, SyncIO].unsafeRunSync()
  }

  // TODO コンテキスト境界明確化のため、privateであるべきである
  lazy val breakCountSystem: subsystems.breakcount.System[IO, SyncIO] = {
    import PluginExecutionContexts.{asyncShift, onMainThread}

    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val globalNotification: DiscordNotificationAPI[IO] =
      discordNotificationSystem.globalNotification
    subsystems.breakcount.System.wired[IO, SyncIO]().unsafeRunSync()
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
    implicit val gtToSiinaAPI: GtToSiinaAPI[ItemStack] = gtToSiinaSystem.api

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

  private lazy val fourDimensionalPocketSystem
    : subsystems.fourdimensionalpocket.System[IO, Player] = {
    import PluginExecutionContexts.{asyncShift, onMainThread}

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val syncIOUuidRepository: UuidRepository[SyncIO] =
      JdbcBackedUuidRepository.initializeStaticInstance[SyncIO].unsafeRunSync().apply[SyncIO]

    subsystems
      .fourdimensionalpocket
      .System
      .wired[IO, SyncIO](breakCountSystem.api)
      .unsafeRunSync()
  }

  private lazy val fastDiggingEffectSystem
    : subsystems.fastdiggingeffect.System[IO, IO, Player] = {
    import PluginExecutionContexts.{asyncShift, onMainThread, timer}

    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val configuration: Configuration =
      seichiAssistConfig.getFastDiggingEffectSystemConfiguration
    implicit val breakCountApi: BreakCountAPI[IO, SyncIO, Player] = breakCountSystem.api
    implicit val getConnectedPlayers: GetConnectedPlayers[IO, Player] =
      new GetConnectedBukkitPlayers[IO]
    implicit val redisBungeeConfig: RedisBungeeRedisConfiguration =
      seichiAssistConfig.getRedisBungeeRedisConfiguration
    implicit val networkConnectionCount: GetNetworkConnectionCount[IO] =
      new RedisBungeeNetworkConnectionCount[IO](asyncShift)

    subsystems.fastdiggingeffect.System.wired[SyncIO, IO, SyncIO].unsafeRunSync()
  }

  private lazy val gachaPointSystem: subsystems.gachapoint.System[IO, SyncIO, Player] = {
    import PluginExecutionContexts.{asyncShift, onMainThread, timer}

    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val getConnectedPlayers: GetConnectedPlayers[IO, Player] =
      new GetConnectedBukkitPlayers[IO]

    subsystems.gachapoint.System.wired[IO, SyncIO](breakCountSystem.api).unsafeRunSync()
  }

  private lazy val mebiusSystem: Subsystem[IO] = {
    import PluginExecutionContexts.{onMainThread, sleepAndRoutineContext, timer}

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val syncClock: Clock[SyncIO] = Clock.create[SyncIO]
    implicit val syncSeasonalEventsSystemAPI: SeasonalEventsAPI[SyncIO] =
      seasonalEventsSystem.api[SyncIO]

    subsystems.mebius.System.wired[IO, SyncIO].unsafeRunSync()
  }

  private implicit lazy val discordNotificationSystem
    : subsystems.discordnotification.System[IO] = {
    import PluginExecutionContexts.asyncShift

    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)

    subsystems
      .discordnotification
      .System
      .wired[IO](seichiAssistConfig.discordNotificationConfiguration)
  }

  private lazy val homeSystem: home.System[IO] = {
    import PluginExecutionContexts.{asyncShift, onMainThread}

    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val breakCountReadAPI: BreakCountAPI[IO, SyncIO, Player] = breakCountSystem.api
    implicit val buildCountReadAPI: BuildCountAPI[IO, SyncIO, Player] = buildCountSystem.api

    home.System.wired[IO, SyncIO]
  }

  private lazy val presentSystem: Subsystem[IO] = {
    import PluginExecutionContexts.{asyncShift, onMainThread}

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val uuidToLastSeenName: UuidToLastSeenName[IO] = new GlobalPlayerAccessor[IO]
    subsystems.present.System.wired
  }

  private lazy val anywhereEnderSystem: subsystems.anywhereender.System[IO] = {
    import PluginExecutionContexts.onMainThread

    implicit val seichiAmountReadApi: BreakCountAPI[IO, SyncIO, Player] = breakCountSystem.api
    subsystems
      .anywhereender
      .System
      .wired[SyncIO, IO](seichiAssistConfig.getAnywhereEnderConfiguration)
  }

  private implicit lazy val mineStackAPI: MineStackAPI[IO, Player, ItemStack] =
    mineStackSystem.api

  private lazy val sharedInventorySystem: subsystems.sharedinventory.System[IO] = {
    import PluginExecutionContexts.timer
    subsystems.sharedinventory.System.wired[IO, IO].unsafeRunSync()
  }

  private lazy val gachaPrizeSystem: subsystems.gachaprize.System[IO] =
    subsystems.gachaprize.System.wired.unsafeRunSync()

  private implicit lazy val gachaPrizeAPI: GachaPrizeAPI[IO, ItemStack, Player] =
    gachaPrizeSystem.api

  private lazy val gachaSystem: subsystems.gacha.System[IO, Player] = {
    implicit val gachaTicketAPI: GachaTicketAPI[IO] = gachaTicketSystem.api

    subsystems.gacha.System.wired[IO].unsafeRunSync()
  }

  private lazy val consumeGachaTicketSystem
    : subsystems.gacha.subsystems.consumegachaticket.System[IO] = {
    subsystems.gacha.subsystems.consumegachaticket.System.wired[IO, SyncIO].unsafeRunSync()
  }

  private lazy val gachaTicketSystem: gachaticket.System[IO] =
    gachaticket.System.wired[IO]

  private lazy val gtToSiinaSystem
    : subsystems.tradesystems.subsystems.gttosiina.System[IO, ItemStack] =
    subsystems.tradesystems.subsystems.gttosiina.System.wired[IO]

  private lazy val gachaTradeSystem: Subsystem[IO] = {
    implicit val gachaPointApi: GachaPointApi[IO, SyncIO, Player] = gachaPointSystem.api
    subsystems.tradesystems.subsystems.gachatrade.System.wired[IO, SyncIO]
  }

  private lazy val lastQuitSystem: subsystems.lastquit.System[IO] =
    subsystems.lastquit.System.wired[IO]

  private lazy val donateSystem: subsystems.donate.System[IO] =
    subsystems.donate.System.wired[IO]

  private lazy val idleTimeSystem: subsystems.idletime.System[IO, Player] = {
    import PluginExecutionContexts.{onMainThread, sleepAndRoutineContext}
    subsystems.idletime.System.wired[IO].unsafeRunSync()
  }

  private lazy val awayScreenNameSystem: Subsystem[IO] = {
    import PluginExecutionContexts.{onMainThread, sleepAndRoutineContext}

    implicit val idleTimeAPI: IdleTimeAPI[IO, Player] = idleTimeSystem.api

    subsystems.idletime.subsystems.awayscreenname.System.wired[IO].unsafeRunSync()
  }

  // TODO: これはprivateであるべきだが、Achievementシステムが再実装されるまでやむを得ずpublicにする
  lazy val voteSystem: subsystems.vote.System[IO, Player] = {
    implicit val breakCountAPI: BreakCountAPI[IO, SyncIO, Player] = breakCountSystem.api

    subsystems.vote.System.wired[IO, SyncIO]
  }

  private lazy val fairySystem: subsystems.vote.subsystems.fairy.System[IO, SyncIO, Player] = {
    import PluginExecutionContexts.{asyncShift, sleepAndRoutineContext}
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val breakCountAPI: BreakCountAPI[IO, SyncIO, Player] = breakCountSystem.api
    implicit val voteAPI: VoteAPI[IO, Player] = voteSystem.api
    implicit val manaApi: ManaApi[IO, SyncIO, Player] = manaSystem.manaApi
    implicit val fairySpeechAPI: FairySpeechAPI[IO, Player] = fairySpeechSystem.api

    subsystems.vote.subsystems.fairy.System.wired.unsafeRunSync()
  }

  private lazy val fairySpeechSystem
    : subsystems.vote.subsystems.fairyspeech.System[IO, Player] = {
    import PluginExecutionContexts.timer

    subsystems.vote.subsystems.fairyspeech.System.wired[IO]
  }

  /* TODO: mineStackSystemは本来privateであるべきだが、mineStackにアイテムを格納するAPIが現状の
      BreakUtilの実装から呼び出されている都合上やむを得ずpublicになっている。*/
  lazy val mineStackSystem: subsystems.minestack.System[IO, Player, ItemStack] =
    subsystems.minestack.System.wired[IO, SyncIO].unsafeRunSync()

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
    homeSystem,
    presentSystem,
    anywhereEnderSystem,
    voteSystem,
    fairySpeechSystem,
    fairySystem,
    gachaPrizeSystem,
    idleTimeSystem,
    awayScreenNameSystem,
    lastQuitSystem,
    donateSystem,
    gachaSystem,
    gachaTicketSystem,
    gtToSiinaSystem,
    gachaTradeSystem,
    sharedInventorySystem,
    mineStackSystem,
    consumeGachaTicketSystem,
    openirontrapdoor.System.wired
  )

  private lazy val buildAssist: BuildAssist = {
    implicit val flyApi: ManagedFlyApi[SyncIO, Player] = managedFlySystem.api
    implicit val buildCountAPI: BuildCountAPI[IO, SyncIO, Player] = buildCountSystem.api
    implicit val manaApi: ManaApi[IO, SyncIO, Player] = manaSystem.manaApi

    new BuildAssist(this)
  }

  private lazy val bungeeSemaphoreResponderSystem: BungeeSemaphoreResponderSystem[IO] = {
    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment
    implicit val concurrentEffect: ConcurrentEffect[IO] = IO.ioConcurrentEffect(asyncShift)
    implicit val systemConfiguration
      : com.github.unchama.bungeesemaphoreresponder.Configuration =
      seichiAssistConfig.getBungeeSemaphoreSystemConfiguration

    val savePlayerData: PlayerDataFinalizer[IO, Player] = { player =>
      IO {
        import scala.util.chaining._
        SeichiAssist.playermap.remove(player.getUniqueId).get.tap(_.updateOnQuit())
      } >>= (playerData => PlayerDataSaveTask.savePlayerData[IO](player, playerData))
    }

    import PluginExecutionContexts.timer

    new BungeeSemaphoreResponderSystem(
      Seq(
        savePlayerData,
        assaultSkillRoutinesRepositoryControls.finalizer.coerceContextTo[IO],
        activeSkillAvailabilityRepositoryControls.finalizer.coerceContextTo[IO]
      ).appendedAll(wiredSubsystems.flatMap(_.managedFinalizers))
        .appendedAll(wiredSubsystems.flatMap(_.managedRepositoryControls.map(_.finalizer)))
        .toList,
      PluginExecutionContexts.asyncShift
    )
  }

  // endregion

  private implicit val _akkaSystem: ActorSystem =
    ConfiguredActorSystemProvider("reference.conf").provide()

  /**
   * プラグインを初期化する。ここで例外が投げられるとBukkitがシャットダウンされる。
   */
  private def monitoredInitialization(): Unit = {

    /**
     * Spigotサーバーが開始されるときにはまだPreLoginEventがcatchされない等色々な不都合があるので、
     * SeichiAssistの初期化はプレーヤーが居ないことを前提として進めることとする。
     *
     * NOTE: PreLoginToQuitPlayerDataRepository に関してはJoinEventさえcatchできれば弾けるので、
     * 接続を試みているプレーヤーは弾かないで良さそう、と言うか弾く術がない
     */
    kickAllPlayersDueToInitialization.unsafeRunSync()

    if (hasBeenLoadedAlready) {
      throw new IllegalStateException("SeichiAssistは2度enableされることを想定されていません！シャットダウンします…")
    }

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    // チャンネルを追加
    Bukkit.getMessenger.registerOutgoingPluginChannel(this, "BungeeCord")

    // BungeeCordとのI/O
    Bukkit
      .getMessenger
      .registerIncomingPluginChannel(this, "SeichiAssistBungee", new BungeeReceiver(this))
    Bukkit.getMessenger.registerOutgoingPluginChannel(this, "SeichiAssistBungee")

    // コンフィグ系の設定は全てConfig.javaに移動
    SeichiAssist.seichiAssistConfig = Config.loadFrom(this)

    Sentry.init { options =>
      options.setDsn("https://7f241763b17c49db982ea29ad64b0264@sentry.onp.admin.seichi.click/2")
      // パフォーマンスモニタリングに使うトレースサンプルの送信割合
      // tracesSampleRateを1.0にすると全てのイベントが送られるため、送りすぎないように調整する必要がある
      options.setTracesSampleRate(0.25)

      // どのサーバーからイベントが送られているのかを判別する識別子
      options.setEnvironment(SeichiAssist.seichiAssistConfig.getServerId)
    }

    Sentry.configureScope(_.setLevel(SentryLevel.WARNING))

    if (SeichiAssist.seichiAssistConfig.getDebugMode == 1) {
      // debugmode=1の時は最初からデバッグモードで鯖を起動
      logger.info(s"${RED}SeichiAssistをデバッグモードで起動します")
      logger.info(s"${RED}コンソールから/seichi debugmode")
      logger.info(s"${RED}を実行するといつでもON/OFFを切り替えられます")
      SeichiAssist.DEBUG = true
    } else {
      // debugmode=0の時は/seichi debugmodeによる変更コマンドも使えない
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
        () => {
          val loadedFlyway = Flyway
            .configure
            .dataSource(getURL, getID, getPW)
            .baselineOnMigrate(true)
            .locations("db/migration", "com/github/unchama/seichiassist/database/migrations")
            .baselineVersion("1.0.0")
            .schemas("flyway_managed_schema")
            .load

          loadedFlyway.repair()
          loadedFlyway.migrate
        }
      )
    }

    itemMigrationSystem.entryPoints.runDatabaseMigration[SyncIO].unsafeRunSync()
    itemMigrationSystem.entryPoints.runWorldMigration.unsafeRunSync()

    SeichiAssist.databaseGateway = DatabaseGateway.createInitializedInstance(
      SeichiAssist.seichiAssistConfig.getURL,
      SeichiAssist.seichiAssistConfig.getDB,
      SeichiAssist.seichiAssistConfig.getID,
      SeichiAssist.seichiAssistConfig.getPW
    )

    import PluginExecutionContexts._
    implicit val breakCountApi: BreakCountAPI[IO, SyncIO, Player] = breakCountSystem.api
    implicit val breakCountBarApi: BreakCountBarAPI[SyncIO, Player] = breakCountBarSystem.api
    implicit val fastDiggingEffectApi: FastDiggingEffectApi[IO, Player] =
      fastDiggingEffectSystem.effectApi
    implicit val fastDiggingSettingsApi: FastDiggingSettingsApi[IO, Player] =
      fastDiggingEffectSystem.settingsApi
    implicit val fourDimensionalPocketApi: FourDimensionalPocketApi[IO, Player] =
      fourDimensionalPocketSystem.api
    implicit val gachaPointApi: GachaPointApi[IO, SyncIO, Player] = gachaPointSystem.api
    implicit val manaApi: ManaApi[IO, SyncIO, Player] = manaSystem.manaApi
    implicit val globalNotification: DiscordNotificationAPI[IO] =
      discordNotificationSystem.globalNotification
    implicit val subHomeReadApi: HomeReadAPI[IO] = homeSystem.api
    implicit val everywhereEnderChestApi: AnywhereEnderChestAPI[IO] =
      anywhereEnderSystem.accessApi
    implicit val sharedInventoryAPI: SharedInventoryAPI[IO, Player] =
      sharedInventorySystem.api
    implicit val voteAPI: VoteAPI[IO, Player] = voteSystem.api
    implicit val fairyAPI: FairyAPI[IO, SyncIO, Player] = fairySystem.api
    implicit val fairySpeechAPI: FairySpeechAPI[IO, Player] = fairySpeechSystem.api
    implicit val donateAPI: DonatePremiumPointAPI[IO] = donateSystem.api
    implicit val gachaTicketAPI: GachaTicketAPI[IO] =
      gachaTicketSystem.api
    implicit val gachaAPI: GachaDrawAPI[IO, Player] = gachaSystem.api
    implicit val consumeGachaTicketAPI: ConsumeGachaTicketAPI[IO, Player] =
      consumeGachaTicketSystem.api

    val menuRouter = TopLevelRouter.apply
    import SeichiAssist.Scopes.globalChatInterceptionScope
    import menuRouter.canOpenStickMenu

    buildAssist.onEnable()

    implicit val managedFlyApi: ManagedFlyApi[SyncIO, Player] = managedFlySystem.api
    // 本来は曖昧さ回避のためにRouterのインスタンスを生成するべきではないが、生成を回避しようとすると
    // 巨大な変更が必要となる。そのため、Routerのインスタンスを新しく生成することで、それまでの間
    // 機能を果たそうとするものである。
    implicit val canOpenBuildMainMenu: CanOpen[IO, BuildMainMenu.type] =
      BuildAssistMenuRouter.apply.canOpenBuildMainMenu
    implicit val ioCanOpenCategorizedMenu: IO CanOpen CategorizedMineStackMenu =
      menuRouter.ioCanOpenCategorizedMineStackMenu

    // コマンドの登録
    Map(
      "map" -> MapCommand.executor,
      "ef" -> new EffectCommand(fastDiggingEffectSystem.settingsApi).executor,
      "seichiassist" -> SeichiAssistCommand.executor,
      "stick" -> StickCommand.executor,
      "rmp" -> RmpCommand.executor,
      "halfguard" -> HalfBlockProtectCommand.executor,
      "gtfever" -> GiganticFeverCommand.executor,
      "minehead" -> new MineHeadCommand().executor,
      "x-transfer" -> RegionOwnerTransferCommand.executor,
      "stickmenu" -> StickMenuCommand.executor,
      "hat" -> HatCommand.executor,
      "minestack" -> MineStackCommand.executor // FIXME: 現在のsubsystemだと、ioCanOpen...を要求できないのでやむを得ずこうしている
    ).concat(wiredSubsystems.flatMap(_.commands)).foreach {
      case (commandName, executor) => getCommand(commandName).setExecutor(executor)
    }

    import menuRouter.ioCanOpenNickNameMenu
    // リスナーの登録
    val listeners = Seq(
      new PlayerJoinListener(),
      new PlayerClickListener(),
      new PlayerBlockBreakListener(),
      new PlayerInventoryListener(),
      new EntityListener(),
      new PlayerDeathEventListener(),
      new GachaItemListener(),
      new RegionInventoryListener(),
      new WorldRegenListener(),
      new ChatInterceptor(List(globalChatInterceptionScope)),
      new MenuHandler(),
      SpawnRegionProjectileInterceptor,
      Y5DoubleSlabCanceller
    ).concat(bungeeSemaphoreResponderSystem.listenersToBeRegistered)
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

    removeRegions()

    logger.info("SeichiAssistが有効化されました！")
  }

  override def onEnable(): Unit = {
    try {
      monitoredInitialization()
    } catch {
      case e: Throwable =>
        logger.error("初期化処理に失敗しました。シャットダウンしています…")
        e.printStackTrace()
        Bukkit.shutdown()
    }
  }

  // FIXME: rmpコマンドを実装しているシステムをsubsystemに切り出したらapiを利用して処理をする
  // ref: https://github.com/GiganticMinecraft/SeichiAssist/pulls#discussion_r1020897163
  private def removeRegions(): Unit = {
    Bukkit.dispatchCommand(Bukkit.getConsoleSender, "rmp remove world_SW_2 3")
    Bukkit.dispatchCommand(Bukkit.getConsoleSender, "rmp remove world_SW_4 3")
  }

  private def startRepeatedJobs(): Unit = {
    val startTask = {
      val dataRecalculationRoutine = {
        import PluginExecutionContexts._
        PlayerDataRecalculationRoutine()
      }

      val dataBackupRoutine = {
        import PluginExecutionContexts._
        PlayerDataBackupRoutine()
      }

      import PluginExecutionContexts._

      implicit val breakCountApi: BreakCountReadAPI[IO, SyncIO, Player] = breakCountSystem.api
      implicit val manaApi: ManaApi[IO, SyncIO, Player] = manaSystem.manaApi
      implicit val gachaPointApi: GachaPointApi[IO, SyncIO, Player] = gachaPointSystem.api
      implicit val fastDiggingEffectApi: FastDiggingEffectApi[IO, Player] =
        fastDiggingEffectSystem.effectApi
      implicit val gachaDrawAPI: GachaDrawAPI[IO, Player] = gachaSystem.api
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
      .getOnlinePlayers
      .asScala
      .toList
      .flatTraverse { player =>
        bungeeSemaphoreResponderSystem.finalizers.traverse(_.onQuitOf(player))
      }
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
      case None    =>
    }
  }
}

object SeichiAssist {
  // Playerdataに依存するデータリスト
  val playermap: mutable.HashMap[UUID, PlayerData] = mutable.HashMap()

  var instance: SeichiAssist = _
  // デバッグフラグ(デバッグモード使用時はここで変更するのではなくconfig.ymlの設定値を変更すること！)
  // TODO deprecate this
  var DEBUG = false
  // TODO staticであるべきではない
  var databaseGateway: DatabaseGateway = _
  var seichiAssistConfig: Config = _

  object Scopes {
    implicit val globalChatInterceptionScope: InterceptionScope[UUID, String] = {
      import PluginExecutionContexts.asyncShift

      new InterceptionScope[UUID, String]()
    }
  }
}

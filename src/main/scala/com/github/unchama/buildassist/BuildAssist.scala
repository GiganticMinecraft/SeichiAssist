package com.github.unchama.buildassist

import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.buildassist.listener._
import com.github.unchama.buildassist.menu.BuildAssistMenuRouter
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.listener.BuildMainMenuOpener
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.managedfly.ManagedFlyApi
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.{DefaultEffectEnvironment, subsystems}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.{Bukkit, Material}

import java.util
import java.util.UUID
import scala.collection.mutable

class BuildAssist(plugin: Plugin)(
  implicit flyApi: ManagedFlyApi[SyncIO, Player],
  buildCountAPI: subsystems.buildcount.BuildCountAPI[IO, SyncIO, Player],
  manaApi: ManaApi[IO, SyncIO, Player],
  mineStackAPI: MineStackAPI[IO, Player, ItemStack],
  ioConcurrentEffect: ConcurrentEffect[IO],
  playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
) {

  // TODO この辺のフィールドを整理する

  /**
   * 永続化されない、プレーヤーのセッション内でのみ有効な一時データを管理するMap。 [[TemporaryDataInitializer]] によって初期化、削除される。
   */
  val temporaryData: mutable.HashMap[UUID, TemporaryMutableBuildAssistPlayerData] =
    mutable.HashMap()

  val buildAmountDataRepository
    : KeyedDataRepository[Player, ReadOnlyRef[SyncIO, BuildAmountData]] =
    buildCountAPI.playerBuildAmountRepository

  {
    BuildAssist.plugin = plugin
    BuildAssist.instance = this
  }

  def onEnable(): Unit = {
    implicit val menuRouter: BuildAssistMenuRouter[IO] = {
      import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{
        layoutPreparationContext,
        onMainThread
      }

      BuildAssistMenuRouter.apply
    }

    // コンフィグ系の設定は全てConfig.javaに移動
    BuildAssist.config = new BuildAssistConfig(plugin)
    BuildAssist.config.loadConfig()

    import buildCountAPI._
    import menuRouter._

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    val listeners = List(
      new BuildMainMenuOpener(),
      new PlayerInventoryListener(),
      new TemporaryDataInitializer(this.temporaryData),
      new BlockLineUpTriggerListener[SyncIO],
      new TilingSkillTriggerListener[IO, SyncIO]
    )

    listeners.foreach { listener =>
      Bukkit.getServer.getPluginManager.registerEvents(listener, plugin)
    }

    plugin.getLogger.info("BuildAssist is Enabled!")
  }

}

object BuildAssist {
  var instance: BuildAssist = _

  val material_destruction: java.util.Set[Material] = util
    .EnumSet
    .of(
      Material.GRASS,
      Material.TALL_GRASS,
      Material.DEAD_BUSH // 枯れ木
      ,
      Material.DANDELION,
      Material.POPPY,
      Material.BLUE_ORCHID,
      Material.ALLIUM,
      Material.AZURE_BLUET,
      Material.RED_TULIP,
      Material.ORANGE_TULIP,
      Material.WHITE_TULIP,
      Material.PINK_TULIP,
      Material.OXEYE_DAISY,
      Material.BROWN_MUSHROOM // きのこ
      ,
      Material.RED_MUSHROOM // 赤きのこ
      ,
      Material.TORCH // 松明
      ,
      Material.SNOW // 雪
      ,
      Material.SUNFLOWER,
      Material.LILAC,
      Material.LARGE_FERN,
      Material.ROSE_BUSH,
      Material.PEONY,
      Material.WATER // 水
      ,
      Material.LAVA // 溶岩
      ,
      Material.VINE // ツタ
    )

  var plugin: Plugin = _
  var config: BuildAssistConfig = _
  val line_up_str: Seq[String] = Seq("OFF", "上側", "下側")
  val line_up_step_str: Seq[String] = Seq("上側", "下側", "両方")
  val line_up_off_on_str: Seq[String] = Seq("OFF", "ON")
}

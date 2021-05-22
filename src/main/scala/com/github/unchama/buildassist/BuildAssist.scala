package com.github.unchama.buildassist

import cats.effect.{IO, SyncIO}
import com.github.unchama.buildassist.listener._
import com.github.unchama.buildassist.menu.BuildAssistMenuRouter
import com.github.unchama.buildassist.enums._
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.listener.BuildMainMenuOpener
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountData
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.managedfly.ManagedFlyApi
import com.github.unchama.seichiassist.{DefaultEffectEnvironment, subsystems}
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

import java.util.UUID
import scala.collection.mutable

class BuildAssist(plugin: Plugin)
                 (implicit
                  flyApi: ManagedFlyApi[SyncIO, Player],
                  buildCountAPI: subsystems.buildcount.BuildCountAPI[SyncIO, Player],
                  manaApi: ManaApi[IO, SyncIO, Player]) {

  // TODO この辺のフィールドを整理する

  /**
   * 永続化されない、プレーヤーのセッション内でのみ有効な一時データを管理するMap。
   * [[TemporaryDataInitializer]] によって初期化、削除される。
   */
  val temporaryData: mutable.HashMap[UUID, TemporaryMutableBuildAssistPlayerData] = mutable.HashMap()

  val buildAmountDataRepository: KeyedDataRepository[Player, ReadOnlyRef[SyncIO, BuildAmountData]] =
    buildCountAPI.playerBuildAmountRepository

  {
    BuildAssist.plugin = plugin
    BuildAssist.instance = this
  }

  def onEnable(): Unit = {
    implicit val menuRouter: BuildAssistMenuRouter[IO] = {
      import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, onMainThread}

      BuildAssistMenuRouter.apply
    }

    //コンフィグ系の設定は全てConfig.javaに移動
    BuildAssist.config = new BuildAssistConfig(plugin)
    BuildAssist.config.loadConfig()

    import buildCountAPI._
    import menuRouter._

    implicit val effectEnvironment: EffectEnvironment = DefaultEffectEnvironment

    val listeners = List(
      new BuildMainMenuOpener(),
      new TemporaryDataInitializer(this.temporaryData),
      new BlockLineUpTriggerListener[SyncIO],
      new TilingSkillTriggerListener[SyncIO],
    )

    listeners.foreach { listener =>
      Bukkit.getServer.getPluginManager.registerEvents(listener, plugin)
    }

    plugin.getLogger.info("BuildAssist is Enabled!")
  }

}

object BuildAssist {
  var instance: BuildAssist = _
  // TODO autodestructible     , Material.STATIONARY_LAVA // 溶岩
  //    , Material.VINE // ツタ
  var plugin: Plugin = _
  var config: BuildAssistConfig = _

  val lineFillStateDescriptions = Map(
    LineFillStatusFlag.Disabled -> "OFF",
    LineFillStatusFlag.UpperSide -> "上側",
    LineFillStatusFlag.LowerSide -> "下側"
  )

  val lineFillSlabPositionDescriptions = Map(
    LineFillSlabPosition.Upper -> "上側",
    LineFillSlabPosition.Lower -> "下側",
    LineFillSlabPosition.Both -> "両方",
  )

  val lineFillPrioritizeMineStackDescriptions = asDescription _

  private[buildassist] def asDescription(isTurnedOn: Boolean): String =
    if (isTurnedOn) "ON" else "OFF"
}

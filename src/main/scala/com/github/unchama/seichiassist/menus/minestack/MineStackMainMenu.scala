package com.github.unchama.seichiassist.menus.minestack

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory._
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory.{AGRICULTURAL, BUILDING, GACHA_PRIZES, MOB_DROP, ORES, REDSTONE_AND_TRANSPORTATION}
import com.github.unchama.seichiassist.{CommonSoundEffects, SeichiAssist}
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player

object MineStackMainMenu extends Menu {

  import com.github.unchama.menuinventory.syntax._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, sync}
  import eu.timepit.refined.auto._

  override val frame: MenuFrame =
    MenuFrame(6.chestRows, s"$DARK_PURPLE${BOLD}MineStackメインメニュー")
  val categoryButtonLayout: MenuSlotLayout = {
    def iconMaterialFor(category: MineStackObjectCategory): Material = category match {
      case ORES => Material.DIAMOND_ORE
      case MOB_DROP => Material.ENDER_PEARL
      case AGRICULTURAL => Material.SEEDS
      case BUILDING => Material.SMOOTH_BRICK
      case REDSTONE_AND_TRANSPORTATION => Material.REDSTONE
      case GACHA_PRIZES => Material.GOLDEN_APPLE
    }

    val layoutMap = MineStackObjectCategory.values.zipWithIndex.map { case (category, index) =>
      val slotIndex = index + 1 // 0には自動スタック機能トグルが入るので、1から入れ始める
      val iconItemStack = new IconItemStackBuilder(iconMaterialFor(category))
        .title(s"$BLUE$UNDERLINE$BOLD${category.uiLabel}")
        .build()

      val button = Button(
        iconItemStack,
        action.LeftClickButtonEffect(
          CommonSoundEffects.menuTransitionFenceSound,
          CategorizedMineStackMenu.forCategory(category).open
        )
      )
      slotIndex -> button
    }.toMap

    MenuSlotLayout(layoutMap)
  }

  override def computeMenuLayout(player: Player): IO[MenuSlotLayout] = {
    for {
      autoMineStackToggleButton <- MineStackButtons(player).computeAutoMineStackToggleButton()
      historicalMineStackSection <- ButtonComputations(player).computeHistoricalMineStackLayout()
    } yield {
      MenuSlotLayout(
        ChestSlotRef(0, 0) -> autoMineStackToggleButton,
        ChestSlotRef(5, 0) -> CommonButtons.openStickMenu
      )
        .merge(categoryButtonLayout)
        .merge(historicalMineStackSection)
    }
  }

  private case class ButtonComputations(player: Player) extends AnyVal {

    import cats.implicits._
    import player._

    import scala.jdk.CollectionConverters._

    /**
     * メインメニュー内の「履歴」機能部分のレイアウトを計算する
     */
    def computeHistoricalMineStackLayout(): IO[MenuSlotLayout] = {
      val playerData = SeichiAssist.playermap(getUniqueId)

      for {
        usageHistory <- IO {
          playerData.hisotryData.usageHistory
        }
        buttonMapping <- usageHistory.asScala.zipWithIndex
          .map { case (mineStackObject, index) =>
            val slotIndex = 18 + index // 3行目から入れだす
            val button = MineStackButtons(player).getMineStackItemButtonOf(mineStackObject)

            slotIndex -> button
          }
          .toList
          .map(_.sequence)
          .sequence
      } yield MenuSlotLayout(buttonMapping: _*)
    }
  }

}

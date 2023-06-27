package com.github.unchama.seichiassist.menus.minestack

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory._
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.menus.stickmenu.FirstPage
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectCategory
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectCategory._
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object MineStackMainMenu extends Menu {

  import com.github.unchama.menuinventory.syntax._
  import eu.timepit.refined.auto._

  class Environment(
    implicit val ioOnMainThread: OnMinecraftServerThread[IO],
    implicit val ioCanOpenFirstPage: IO CanOpen FirstPage.type,
    implicit val ioCanOpenCategorizedMineStackMenu: IO CanOpen CategorizedMineStackMenu,
    implicit val gachaPrizeAPI: GachaPrizeAPI[IO, ItemStack, Player],
    implicit val mineStackAPI: MineStackAPI[IO, Player, ItemStack]
  )

  override val frame: MenuFrame = MenuFrame(6.chestRows, s"$DARK_PURPLE${BOLD}MineStackメインメニュー")

  private def categoryButtonLayout(
    implicit ioCanOpenCategorizedMineStackMenu: IO CanOpen CategorizedMineStackMenu
  ): MenuSlotLayout = {
    def iconMaterialFor(category: MineStackObjectCategory): Material = category match {
      case ORES                        => Material.DIAMOND_ORE
      case MOB_DROP                    => Material.ENDER_PEARL
      case AGRICULTURAL                => Material.WHEAT_SEEDS
      case BUILDING                    => Material.STONE_BRICKS
      case REDSTONE_AND_TRANSPORTATION => Material.REDSTONE
      case GACHA_PRIZES                => Material.GOLDEN_APPLE
    }

    val layoutMap = MineStackObjectCategory
      .values
      .zipWithIndex
      .map {
        case (category, index) =>
          val slotIndex = index + 1 // 0には自動スタック機能トグルが入るので、1から入れ始める
          val iconItemStack = new IconItemStackBuilder(iconMaterialFor(category))
            .title(s"$BLUE$UNDERLINE$BOLD${category.uiLabel}")
            .build()

          val button = Button(
            iconItemStack,
            action.LeftClickButtonEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              ioCanOpenCategorizedMineStackMenu.open(CategorizedMineStackMenu(category))
            )
          )
          slotIndex -> button
      }
      .toMap

    MenuSlotLayout(layoutMap)
  }

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import environment._

    for {
      autoMineStackToggleButton <- MineStackButtons(player).computeAutoMineStackToggleButton
      historicalMineStackSection <- ButtonComputations(player).computeHistoricalMineStackLayout
    } yield {
      MenuSlotLayout(
        ChestSlotRef(0, 0) -> autoMineStackToggleButton,
        ChestSlotRef(5, 0) -> CommonButtons.openStickMenu
      ).merge(categoryButtonLayout).merge(historicalMineStackSection)
    }
  }

  private case class ButtonComputations(player: Player) extends AnyVal {

    import cats.implicits._

    /**
     * メインメニュー内の「履歴」機能部分のレイアウトを計算する
     */
    def computeHistoricalMineStackLayout(
      implicit environment: Environment
    ): IO[MenuSlotLayout] = {
      import environment._
      for {
        usageHistory <- mineStackAPI.getUsageHistory(player)
        buttonMapping <- usageHistory
          .zipWithIndex
          .map {
            case (mineStackObject, index) =>
              val slotIndex = 18 + index // 3行目から入れだす
              val button =
                MineStackButtons(player).getMineStackObjectButtonOf(mineStackObject)

              slotIndex -> button
          }
          .traverse(_.sequence)
      } yield MenuSlotLayout(buttonMapping: _*)
    }
  }

}

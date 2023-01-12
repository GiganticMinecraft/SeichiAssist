package com.github.unchama.seichiassist.menus.gridregion

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.subsystems.gridregion.GridRegionAPI
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Location, Material}

object GridTemplateMenu extends Menu {

  private val aisleAmount =
    Math.ceil(SeichiAssist.seichiAssistConfig.getTemplateKeepAmount / 9.0).toInt + 1

  class Environment(
    implicit val canOpenGridRegionMenu: IO CanOpen GridRegionMenu.type,
    implicit val gridRegionAPI: GridRegionAPI[IO, Player, Location]
  )

  override val frame: MenuFrame =
    MenuFrame(aisleAmount.chestRows, s"${LIGHT_PURPLE}グリッド式保護・設定保存")

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import environment._
    val computeButtons = ComputeButtons(player)
    import computeButtons._

    for {
      templateButtons <- gridTemplateButtons
    } yield {
      val templateButtonLayout = MenuSlotLayout(templateButtons.zipWithIndex.map {
        case (button: Button, index: Int) => index -> button
      }: _*)
      val backMenuButtonPosition = (aisleAmount - 1) * 9
      val backMenuButton = CommonButtons.transferButton(
        new IconItemStackBuilder(Material.BARRIER).lore(List(s"$RED${UNDERLINE}クリックで戻る")),
        "グリッド式保護メニューに戻る",
        GridRegionMenu
      )

      MenuSlotLayout(backMenuButtonPosition -> backMenuButton).merge(templateButtonLayout)
    }
  }

  private case class ComputeButtons(player: Player)(implicit environment: Environment) {
    import environment._

    def gridTemplateButtons: IO[List[Button]] = {
      for {
        templates <- gridRegionAPI.gridRegionTemplate(player)
      } yield {
        templates.map {
          case (id, regionUnits) =>
            val lore = List(
              s"${GREEN}設定内容",
              s"${GRAY}前方向：$AQUA${regionUnits.ahead.units}${GRAY}ユニット",
              s"${GRAY}後ろ方向：$AQUA${regionUnits.behind.units}${GRAY}ユニット",
              s"${GRAY}右方向：$AQUA${regionUnits.right.units}${GRAY}ユニット",
              s"${GRAY}左方向：$AQUA${regionUnits.left.units}${GRAY}ユニット",
              s"${GREEN}左クリックで設定を読み込み",
              s"${RED}右クリックで現在の設定で上書き"
            )

            val itemStack = new IconItemStackBuilder(Material.CHEST)
              .title(s"${GREEN}テンプレNo.${id.value + 1}(設定済み)")
              .lore(lore)
              .build()

            Button(itemStack)
        }.toList
      }
    }

  }

}

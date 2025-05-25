package com.github.unchama.seichiassist.menus.gridregion

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton}
import com.github.unchama.menuinventory.slot.button.action.{
  LeftClickButtonEffect,
  RightClickButtonEffect
}
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{
  LayoutPreparationContext,
  Menu,
  MenuFrame,
  MenuSlotLayout
}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.subsystems.gridregion.GridRegionAPI
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  RegionTemplate,
  RegionTemplateId
}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.SequentialEffect
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Location, Material, World}

object GridTemplateMenu extends Menu {

  private val templateKeepAmount = SeichiAssist.seichiAssistConfig.getTemplateKeepAmount

  private val aisleAmount = Math.ceil(templateKeepAmount / 9.0).toInt + 1

  class Environment(
    implicit val canOpenGridRegionMenu: IO CanOpen GridRegionMenu.type,
    implicit val gridRegionAPI: GridRegionAPI[IO, Player, Location, World],
    implicit val layoutPreparationContext: LayoutPreparationContext,
    implicit val onMinecraftServerThread: OnMinecraftServerThread[IO]
  )

  override val frame: MenuFrame =
    MenuFrame(aisleAmount.chestRows, s"${LIGHT_PURPLE}グリッド式保護・設定保存")

  import cats.implicits._

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import environment._
    val computeButtons = ComputeButtons(player)
    import computeButtons._

    for {
      templateButtons <- (1 to templateKeepAmount).toList.traverse { id =>
        gridTemplateButton(id)
      }
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

    def gridTemplateButton(id: Int): IO[Button] = RecomputedButton {
      for {
        templates <- gridRegionAPI.savedGridRegionTemplates(player)
        currentRegionUnits <- gridRegionAPI.currentlySelectedShape(player)
      } yield {
        val template = templates.find { _.templateId.value == id }

        template match {
          case Some(regionTemplate) =>
            Button(
              new IconItemStackBuilder(Material.CHEST)
                .title(s"${GREEN}テンプレNo.${regionTemplate.templateId.value + 1}(設定済み)")
                .lore(
                  List(
                    s"${GREEN}設定内容",
                    s"${GRAY}前方向：$AQUA${regionTemplate.shape.ahead.rul}${GRAY}ユニット",
                    s"${GRAY}後ろ方向：$AQUA${regionTemplate.shape.behind.rul}${GRAY}ユニット",
                    s"${GRAY}右方向：$AQUA${regionTemplate.shape.right.rul}${GRAY}ユニット",
                    s"${GRAY}左方向：$AQUA${regionTemplate.shape.left.rul}${GRAY}ユニット",
                    s"${GREEN}左クリックで設定を読み込み",
                    s"${RED}右クリックで現在の設定で上書き"
                  )
                )
                .build(),
              LeftClickButtonEffect(
                SequentialEffect(
                  gridRegionAPI.updateCurrentRegionShapeSettings(regionTemplate.shape),
                  MessageEffect(s"${GREEN}グリッド式保護設定データ読み込み完了")
                )
              ),
              RightClickButtonEffect(
                SequentialEffect(
                  gridRegionAPI.saveGridRegionTemplate(
                    RegionTemplate(regionTemplate.templateId, currentRegionUnits)
                  ),
                  MessageEffect(s"${GREEN}グリッド式保護の現在の設定を保存しました。")
                )
              )
            )

          case None =>
            Button(
              new IconItemStackBuilder(Material.PAPER)
                .title(s"${RED}テンプレNo.$id")
                .lore(List(s"${GREEN}未設定", s"${RED}左クリックで現在の設定を保存"))
                .build(),
              LeftClickButtonEffect(
                gridRegionAPI.saveGridRegionTemplate(
                  RegionTemplate(RegionTemplateId(id), currentRegionUnits)
                ),
                MessageEffect(s"${GREEN}グリッド式保護の現在の設定を保存しました。")
              )
            )
        }
      }
    }

  }

}

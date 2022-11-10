package com.github.unchama.seichiassist.menus.gridregion

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton}
import com.github.unchama.menuinventory.{LayoutPreparationContext, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.gridregion.GridRegionAPI
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{DeferredEffect, SequentialEffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.{Material, Sound}

object GridRegionMenu extends Menu {

  class Environment(
    implicit val onMainThread: OnMinecraftServerThread[IO],
    implicit val layoutPreparationContext: LayoutPreparationContext,
    val gridRegionAPI: GridRegionAPI[IO, Player]
  )

  override val frame: MenuFrame =
    MenuFrame(Right(InventoryType.DISPENSER), s"${LIGHT_PURPLE}グリッド式保護設定メニュー")

  override def computeMenuLayout(player: Player)(
    implicit environment: Environment
  ): IO[MenuSlotLayout] = ???

  case class computeButtons(player: Player)(implicit environment: Environment) {
    import environment._

    def toggleUnitPerClickButton(): IO[Button] = RecomputedButton {
      for {
        currentRegionUnit <- gridRegionAPI.unitPerClick(player)
      } yield {
        val iconItemStack = new IconItemStackBuilder(Material.STAINED_GLASS_PANE, 1)
          .title(s"${GREEN}拡張単位の変更")
          .lore(
            List(
              s"${GREEN}現在のユニット指定量",
              s"$AQUA${currentRegionUnit.units}${GREEN}ユニット($AQUA${currentRegionUnit.computeBlockAmount}${GREEN}ブロック)/1クリック",
              s"$RED${UNDERLINE}クリックで変更"
            )
          )
          .build()

        val leftClickEffect = LeftClickButtonEffect {
          SequentialEffect(
            DeferredEffect(IO(gridRegionAPI.toggleUnitPerClick)),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f)
          )
        }

        Button(iconItemStack, leftClickEffect)
      }
    }

  }

}

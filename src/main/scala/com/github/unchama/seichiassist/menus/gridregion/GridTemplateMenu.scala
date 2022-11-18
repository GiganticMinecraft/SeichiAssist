package com.github.unchama.seichiassist.menus.gridregion

import cats.effect.IO
import com.github.unchama.itemstackbuilder.{IconItemStackBuilder, SkullItemStackBuilder}
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{ChestSlotRef, Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.{SeichiAssist, SkullOwners}
import com.github.unchama.seichiassist.menus.{BuildMainMenu, CommonButtons}
import org.bukkit.ChatColor._
import org.bukkit.Material
import org.bukkit.entity.Player

object GridTemplateMenu extends Menu {

  private val aisleAmount = SeichiAssist.seichiAssistConfig.getTemplateKeepAmount + 1

  class Environment(
    implicit val canOpenGridRegionMenu: IO CanOpen GridRegionMenu.type
                   )

  override val frame: MenuFrame = MenuFrame(aisleAmount.chestRows, s"${LIGHT_PURPLE}グリッド式保護・設定保存")

  import eu.timepit.refined.auto._

  override def computeMenuLayout(player: Player)(implicit environment: Environment): IO[MenuSlotLayout] = {
    import environment._
    (aisleAmount * 9) -> CommonButtons.transferButton(
      new IconItemStackBuilder(Material.BARRIER).lore(List(s"$RED${UNDERLINE}クリックで戻る")),
      "グリッド式保護メニューに戻る",
      GridRegionMenu
    )
  }

  private object computeButtons {

    val

  }

}

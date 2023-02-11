package com.github.unchama.seichiassist.menus.nicknames

import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.syntax.IntInventorySizeOps
import com.github.unchama.menuinventory.{Menu, MenuFrame, MenuSlotLayout}
import com.github.unchama.seichiassist.SeichiAssist
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.ChatColor._

object NickNameMenu extends Menu {

  override type Environment = this.type

  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$DARK_PURPLE${BOLD}二つ名組み合わせシステム")

  override def computeMenuLayout(player: Player)(
    implicit environment: Environment
  ): IO[MenuSlotLayout] = ???

  private case class NickNameMenuButtons(player: Player) {

    private val playerData = SeichiAssist.playermap.apply(player.getUniqueId)

    val achievementPointsInformation: Button = Button(
      new IconItemStackBuilder(Material.EMERALD_ORE)
        .title(s"$YELLOW$UNDERLINE${BOLD}実績ポイント情報")
        .lore(
          List(
            s"${GREEN}クリックで情報を最新化",
            s"${RED}累計獲得量：${playerData.achievePoint.cumulativeTotal}",
            s"${RED}累計消費量：${playerData.achievePoint.used}",
            s"${RED}使用可能量：${playerData.achievePoint.left}"
          )
        )
        .build()
    )

  }

}

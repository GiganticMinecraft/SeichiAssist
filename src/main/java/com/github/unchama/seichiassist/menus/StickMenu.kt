package com.github.unchama.seichiassist.menus

import arrow.core.Left
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ButtonAction
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.descrptions.PlayerInformationDescriptions
import com.github.unchama.seichiassist.util.setLoreNotNull
import org.bukkit.ChatColor.*
import org.bukkit.entity.Player

/**
 * 木の棒メニュー
 *
 * @author karayuu
 */
object StickMenu {
  fun openBy(player: Player) {
    val data = SeichiAssist.playermap[player.uniqueId]!!

    val statsButtonIcon = SkullItemStackBuilder(player.uniqueId)
        .title("$YELLOW$BOLD$UNDERLINE${player.name}の統計データ")
        .lore(PlayerInformationDescriptions.playerInfoLore(data))
        .build()

    val statsButtonAction = ButtonAction(ClickEventFilter.LEFT_CLICK) { event ->
      data.toggleExpBarVisibility()
      data.notifyExpBarVisibility()
      event.currentItem.setLoreNotNull(PlayerInformationDescriptions.playerInfoLore(data))
    }

    val menuView =
        MenuInventoryView(
            Left(4 * 9), "${LIGHT_PURPLE}木の棒メニュー",
            mapOf(
                0 to Button(statsButtonIcon, listOf(statsButtonAction))
            )
        )

    player.openInventory(menuView.inventory)
  }
}

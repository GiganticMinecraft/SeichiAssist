package com.github.unchama.seichiassist.menus

import arrow.core.Right
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.targetedeffect.*
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType

/**
 * Created by karayuu on 2019/06/23
 */
object RegionMenu {
  private object Buttons {
    val summonWandButton: Button = run {
      val usage = listOf(
          "${GREEN}①召喚された斧を手に持ちます\n",
          "${GREEN}②保護したい領域の一方の角を${YELLOW}左${GREEN}クリック\n",
          "${GREEN}③もう一方の対角線上の角を${RED}右${GREEN}クリック\n",
          "${GREEN}④メニューの${YELLOW}金の斧${GREEN}をクリック\n"
      )

      val buttonLore = run {
        val summonInfo = listOf(
            "$DARK_RED${UNDERLINE}クリックで召喚",
            "$DARK_GREEN${UNDERLINE}※インベントリを空けておこう"
        )

        val commandInfo = "${DARK_GRAY}command->[//wand]"

        summonInfo + usage + commandInfo
      }

      val leftClickEffect = sequentialEffect(
          TargetedEffect { it.closeInventory() },
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          "wand".asCommandEffect(),
          usage.asMessageEffect()
      )

      Button(
          IconItemStackBuilder(Material.WOOD_AXE)
              .title("$YELLOW$UNDERLINE${BOLD}保護設定用の木の斧を召喚")
              .lore(buttonLore)
              .build(),
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, leftClickEffect)
      )
    }
  }

  private val menuLayout = with(Buttons) {
    IndexedSlotLayout(
        0 to summonWandButton
    )
  }

  val open: TargetedEffect<Player> = TargetedEffect {
    val view = MenuInventoryView(Right(InventoryType.HOPPER), "${BLACK}保護メニュー", menuLayout)
    view.createNewSession().open
  }
}

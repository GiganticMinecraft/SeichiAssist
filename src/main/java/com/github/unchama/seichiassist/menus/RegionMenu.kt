package com.github.unchama.seichiassist.menus

import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.targetedeffect.asCommandEffect
import com.github.unchama.targetedeffect.asMessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.sequentialEffect
import com.github.unchama.targetedeffect.unfocusedEffect
import org.bukkit.entity.Player
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound

/**
 * Created by karayuu on 2019/06/23
 */
object RegionMenu {
  private object ButtonComputations {
    fun Player.computeSummonWandButton(): Button {
      val buttonLore = listOf(
          "$DARK_RED${UNDERLINE}クリックで召喚",
          "$DARK_GREEN${UNDERLINE}※インベントリを空けておこう",
          "${GREEN}①召喚された斧を手に持ちます",
          "${GREEN}②保護したい領域の一方の角を${YELLOW}左${GREEN}クリック",
          "${GREEN}③もう一方の対角線上の角を${RED}右${GREEN}クリック",
          "${GREEN}④メニューの${YELLOW}金の斧${GREEN}をクリック",
          "${DARK_GRAY}command->[//wand]"
      )

      val message = listOf(
          "${GREEN}①召喚された斧を手に持ちます\n",
          "${GREEN}②保護したい領域の一方の角を${YELLOW}左${GREEN}クリック\n",
          "${GREEN}③もう一方の対角線上の角を${RED}右${GREEN}クリック\n",
          "${GREEN}④メニューの${YELLOW}金の斧${GREEN}をクリック\n"
      )

      val leftClickEffect = sequentialEffect(
          unfocusedEffect { this.closeInventory() },
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f),
          "wand".asCommandEffect(),
          message.asMessageEffect()
      )

      return Button(
          IconItemStackBuilder(Material.WOOD_AXE)
              .title("$YELLOW$UNDERLINE${BOLD}保護設定用の木の斧を召喚")
              .lore(buttonLore)
              .build(),
          FilteredButtonEffect(ClickEventFilter.LEFT_CLICK, leftClickEffect)
      )
    }
  }
}

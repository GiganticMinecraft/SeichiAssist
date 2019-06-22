package com.github.unchama.seichiassist.menus

import arrow.core.Left
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.IndexedSlotLayout
import com.github.unchama.menuinventory.MenuInventoryView
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.targetedeffect.*
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player

object MineStackUI {
  private object ButtonComputations {
    suspend fun Player.computeAutoMineStackToggleButton(): Button {
      val playerData = SeichiAssist.playermap[uniqueId]!!

      val iconItemStack = run {
        val baseBuilder =
            IconItemStackBuilder(Material.IRON_PICKAXE)
                .title("$YELLOW$UNDERLINE${BOLD}対象ブロック自動スタック機能")

        if (playerData.minestackflag) {
          baseBuilder
              .enchanted()
              .lore(listOf(
                  "$RESET${GREEN}現在ONです",
                  "$RESET$DARK_RED${UNDERLINE}クリックでOFF"
              ))
        } else {
          baseBuilder
              .lore(listOf(
                  "$RESET${RED}現在OFFです",
                  "$RESET$DARK_GREEN${UNDERLINE}クリックでON"
              ))
        }.build()
      }

      val buttonEffect = FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
        sequentialEffect(
            playerData.toggleAutoMineStack(),
            deferredEffect {
              val message: String
              val soundPitch: Float
              when {
                playerData.minestackflag -> {
                  message = "${GREEN}対象ブロック自動スタック機能:ON"
                  soundPitch = 1.0f
                }
                else -> {
                  message = "${RED}対象ブロック自動スタック機能:OFF"
                  soundPitch = 0.5f
                }
              }

              sequentialEffect(
                  message.asMessageEffect(),
                  FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch)
              )
            },
            deferredEffect { overwriteCurrentSlotBy(computeAutoMineStackToggleButton()) }
        )
      }

      return Button(iconItemStack, buttonEffect)
    }
  }

  private suspend fun Player.computeMineStackLayout(): IndexedSlotLayout {
    return with(ButtonComputations) {
      IndexedSlotLayout(
          0 to computeAutoMineStackToggleButton(),
          45 to CommonButtons.openStickMenu
      )
    }
  }

  val open: TargetedEffect<Player> = computedEffect { player ->
    val view = MenuInventoryView(
        Left(4 * 9),
        "$DARK_PURPLE${BOLD}MineStackメインメニュー",
        player.computeMineStackLayout()
    )

    view.createNewSession().open
  }
}
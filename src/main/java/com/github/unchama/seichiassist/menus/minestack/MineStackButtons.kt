package com.github.unchama.seichiassist.menus.minestack

import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.minestack.MineStackObj
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory
import com.github.unchama.seichiassist.minestack.category
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.*
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object MineStackButtons {
  private fun withDrawOneStackEffect(mineStackObj: MineStackObj): TargetedEffect<Player> {
    fun ItemStack.withAmount(amount: Int): ItemStack = clone().apply { this.amount = amount }

    return computedEffect { player ->
      val playerData = SeichiAssist.playermap[player.uniqueId]!!
      val currentAmount = playerData.minestack.getStackedAmountOf(mineStackObj)
      val grantAmount = Math.min(mineStackObj.itemStack.maxStackSize.toLong(), currentAmount).toInt()

      val soundEffectPitch = if (currentAmount >= grantAmount) 1.0f else 0.5f

      sequentialEffect(
          unfocusedEffect {
            Util.addItemToPlayerSafely(player, mineStackObj.itemStack.withAmount(grantAmount))
            playerData.minestack.subtractStackedAmountOf(mineStackObj, grantAmount.toLong())
          },
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundEffectPitch)
      )
    }
  }

  suspend fun Player.getMineStackItemButtonOf(mineStackObj: MineStackObj): Button {
    val playerData = SeichiAssist.playermap[uniqueId]!!
    val requiredLevel = SeichiAssist.seichiAssistConfig.getMineStacklevel(mineStackObj.level)

    val itemStack = mineStackObj.itemStack.clone().apply {
      itemMeta = itemMeta.apply {
        displayName = run {
          val name = if (hasDisplayName()) displayName else type.toString()

          "$YELLOW$UNDERLINE$BOLD$name"
        }

        lore = run {
          val stackedAmount = playerData.minestack.getStackedAmountOf(mineStackObj)

          listOf(
              "$RESET$GREEN${stackedAmount}個",
              "$RESET${DARK_GRAY}Lv${requiredLevel}以上でスタック可能",
              "$RESET$DARK_RED${UNDERLINE}クリックで1スタック取り出し"
          )
        }
      }
    }

    return Button(
        itemStack,
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
          sequentialEffect(
              withDrawOneStackEffect(mineStackObj),
              deferredEffect { overwriteCurrentSlotBy(getMineStackItemButtonOf(mineStackObj)) },
              unfocusedEffect {
                if (mineStackObj.category() != MineStackObjectCategory.GACHA_PRIZES) {
                  playerData.hisotryData.add(mineStackObj)
                }
              }
          )
        }
    )
  }

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
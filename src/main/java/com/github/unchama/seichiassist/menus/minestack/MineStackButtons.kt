package com.github.unchama.seichiassist.menus.minestack

import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.action.FilteredButtonEffect
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.minestack.MineStackObj
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.*
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor.*
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

    val itemStack = mineStackObj.itemStack.clone()
    val displayName = run {
      val name = if (itemStack.itemMeta.hasDisplayName()) itemStack.itemMeta.displayName else itemStack.type.toString()

      "$YELLOW$UNDERLINE$BOLD$name"
    }
    val lore = run {
      val requiredLevel = SeichiAssist.seichiAssistConfig.getMineStacklevel(mineStackObj.level)
      val stackedAmount = playerData.minestack.getStackedAmountOf(mineStackObj)

      listOf(
          "$RESET$GREEN${stackedAmount}個",
          "$RESET${DARK_GRAY}Lv${requiredLevel}以上でスタック可能",
          "$RESET$DARK_RED${UNDERLINE}クリックで1スタック取り出し"
      )
    }

    itemStack.itemMeta = itemStack.itemMeta.apply {
      this.displayName = displayName
      this.lore = lore
    }

    return Button(
        itemStack,
        FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
          sequentialEffect(
              withDrawOneStackEffect(mineStackObj),
              deferredEffect { overwriteCurrentSlotBy(getMineStackItemButtonOf(mineStackObj)) },
              unfocusedEffect { playerData.hisotryData.add(mineStackObj) }
          )
        }
    )
  }
}
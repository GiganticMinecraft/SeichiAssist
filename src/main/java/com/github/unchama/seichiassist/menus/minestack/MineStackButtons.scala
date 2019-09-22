package com.github.unchama.seichiassist.menus.minestack

import com.github.unchama.menuinventory.slot.button.{Button, action}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.minestack.{MineStackObj, MineStackObjectCategory}
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

private[minestack] case class MineStackButtons(player: Player) {
  private def withDrawOneStackEffect(mineStackObj: MineStackObj): TargetedEffect[Player] = {
    implicit class ItemStackOps(val itemStack: ItemStack) extends AnyVal {
      def withAmount(amount: Int): ItemStack = clone().apply { this.amount = amount }
    }
    implicit class MineStackObjectOps(mineStackObj: MineStackObj) {
      def generateParameterizedStack(player: Player): ItemStack = {
        // ガチャ品であり、かつがちゃりんごでも経験値瓶でもなければ
        if (this.stackType == MineStackObjectCategory.GACHA_PRIZES && this.gachaType >= 0) {
          val gachaData = SeichiAssist.msgachadatalist[this.gachaType]
          if (gachaData.probability < 0.1) {
            return this.itemStack.clone().apply {
              val itemLore = if (itemMeta.hasLore()) itemMeta.lore else List()
              lore = itemLore + s"$RESET${DARK_GREEN}所有者：${player.name}"
            }
          }
        }

        mineStackObj.itemStack.clone()
      }
    }

    return computedEffect { player =>
      val playerData = SeichiAssist.playermap(player.uniqueId)
      val currentAmount = playerData.minestack.getStackedAmountOf(mineStackObj)
      val grantAmount = min(mineStackObj.itemStack.maxStackSize.toLong(), currentAmount).toInt()

      val soundEffectPitch = if (currentAmount >= grantAmount) 1.0f else 0.5f
      val grantItemStack = mineStackObj.generateParameterizedStack(player).withAmount(grantAmount)

      sequentialEffect(
        targetedeffect.UnfocusedEffect {
            Util.addItemToPlayerSafely(player, grantItemStack)
            playerData.minestack.subtractStackedAmountOf(mineStackObj, grantAmount.toLong())
          },
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundEffectPitch)
      )
    }
  }

  @SuspendingMethod def getMineStackItemButtonOf(mineStackObj: MineStackObj): Button = recomputedButton {
    val playerData = SeichiAssist.playermap(uniqueId)
    val requiredLevel = SeichiAssist.seichiAssistConfig.getMineStacklevel(mineStackObj.level)

    val itemStack = mineStackObj.itemStack.clone().apply {
      itemMeta = itemMeta.apply {
        displayName = run {
          val name = mineStackObj.uiName ?: (if (hasDisplayName()) displayName else type.toString())

          s"$YELLOW$UNDERLINE$BOLD$name"
        }

        lore = run {
          val stackedAmount = playerData.minestack.getStackedAmountOf(mineStackObj)

          List(
              s"$RESET$GREEN${stackedAmount}個",
              s"$RESET${DARK_GRAY}Lv${requiredLevel}以上でスタック可能",
              s"$RESET$DARK_RED${UNDERLINE}クリックで1スタック取り出し"
          )
        }
      }
    }

    Button(
        itemStack,
        action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) {
          sequentialEffect(
              withDrawOneStackEffect(mineStackObj),
              targetedeffect.UnfocusedEffect {
                if (mineStackObj.category() !== MineStackObjectCategory.GACHA_PRIZES) {
                  playerData.hisotryData.add(mineStackObj)
                }
              }
          )
        }
    )
  }

  @SuspendingMethod def computeAutoMineStackToggleButton(): Button = recomputedButton {
    val playerData = SeichiAssist.playermap(uniqueId)

    val iconItemStack = run {
      val baseBuilder =
          IconItemStackBuilder(Material.IRON_PICKAXE)
              .title(s"$YELLOW$UNDERLINE${BOLD}対象ブロック自動スタック機能")

      if (playerData.settings.autoMineStack) {
        baseBuilder
            .enchanted()
            .lore(List(
                s"$RESET${GREEN}現在ONです",
                s"$RESET$DARK_RED${UNDERLINE}クリックでOFF"
            ))
      } else {
        baseBuilder
            .lore(List(
                s"$RESET${RED}現在OFFです",
                s"$RESET$DARK_GREEN${UNDERLINE}クリックでON"
            ))
      }.build()
    }

    val buttonEffect = action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) {
      sequentialEffect(
          playerData.settings.toggleAutoMineStack,
          deferredEffect {
            val message: String
            val soundPitch: Float
            when {
              playerData.settings.autoMineStack => {
                message = s"${GREEN}対象ブロック自動スタック機能:ON"
                soundPitch = 1.0f
              }
              else => {
                message = s"${RED}対象ブロック自動スタック機能:OFF"
                soundPitch = 0.5f
              }
            }

            sequentialEffect(
                message.asMessageEffect(),
                FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch)
            )
          }
      )
    }

    Button(iconItemStack, buttonEffect)
  }
}
package com.github.unchama.seichiassist.menus.minestack

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.concurrent.{BukkitSyncIOShift, Execution}
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton, action}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.minestack.{MineStackObj, MineStackObjectCategory}
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.player.{FocusedSoundEffect, MessageEffect}
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

private object MineStackButtons {

  import scala.jdk.CollectionConverters._
  import scala.util.chaining._

  implicit class ItemStackOps(val itemStack: ItemStack) extends AnyVal {
    def withAmount(amount: Int): ItemStack = itemStack.clone().tap(_.setAmount(amount))
  }

  implicit class MineStackObjectOps(val mineStackObj: MineStackObj) extends AnyVal {
    def parameterizedWith(player: Player): ItemStack = {
      // ガチャ品であり、かつがちゃりんごでも経験値瓶でもなければ
      if (mineStackObj.stackType == MineStackObjectCategory.GACHA_PRIZES && mineStackObj.gachaType >= 0) {
        val gachaData = SeichiAssist.msgachadatalist(mineStackObj.gachaType)
        if (gachaData.probability < 0.1) {
          return mineStackObj.itemStack.clone().tap { cloned =>
            val meta = cloned.getItemMeta.tap { itemMeta =>
              val itemLore = if (itemMeta.hasLore) itemMeta.getLore.asScala.toList else List()
              itemMeta.setLore((itemLore :+ s"$RESET${DARK_GREEN}所有者：${player.getName}").asJava)
            }
            cloned.setItemMeta(meta)
          }
        }
      }

      mineStackObj.itemStack.clone()
    }
  }

}

private[minestack] case class MineStackButtons(player: Player) {

  import MineStackButtons._
  import MineStackObjectCategory._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
  import com.github.unchama.targetedeffect._
  import player._

  import scala.jdk.CollectionConverters._

  def getMineStackItemButtonOf(mineStackObj: MineStackObj)(implicit ctx: BukkitSyncIOShift): IO[Button] = RecomputedButton(IO {
    val playerData = SeichiAssist.playermap(getUniqueId)
    val requiredLevel = SeichiAssist.seichiAssistConfig.getMineStacklevel(mineStackObj.level)

    import scala.util.chaining._

    val itemStack = mineStackObj.itemStack.clone().tap { itemStack =>
      import itemStack._
      setItemMeta {
        getItemMeta.tap { itemMeta =>
          import itemMeta._
          setDisplayName {
            val name = mineStackObj.uiName.getOrElse(if (hasDisplayName) getDisplayName else getType.toString)

            s"$YELLOW$UNDERLINE$BOLD$name"
          }

          setLore {
            val stackedAmount = playerData.minestack.getStackedAmountOf(mineStackObj)

            List(
              s"$RESET$GREEN${stackedAmount}個",
              s"$RESET${DARK_GRAY}Lv${requiredLevel}以上でスタック可能",
              s"$RESET$DARK_RED${UNDERLINE}クリックで1スタック取り出し"
            ).asJava
          }
        }
      }
    }

    Button(
      itemStack,
      action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
        sequentialEffect(
          withDrawOneStackEffect(mineStackObj),
          targetedeffect.UnfocusedEffect {
            if (mineStackObj.category() != MineStackObjectCategory.GACHA_PRIZES) {
              playerData.hisotryData.add(mineStackObj)
            }
          }
        )
      }
    )
  })

  private def withDrawOneStackEffect(mineStackObj: MineStackObj)(implicit ctx: BukkitSyncIOShift): TargetedEffect[Player] = {
    val maxStackSize = mineStackObj.itemStack.getMaxStackSize.toLong

    Kleisli(player => Execution.onServerMainThread {
      for {
        playerData <- IO { SeichiAssist.playermap(player.getUniqueId) }
        currentAmount <- IO { playerData.minestack.getStackedAmountOf(mineStackObj) }
        grantAmount = Math.min(maxStackSize, currentAmount).toInt

        soundEffectPitch = if (grantAmount == maxStackSize) 1.0f else 0.5f
        itemStackToGrant = mineStackObj.parameterizedWith(player).withAmount(grantAmount)

        _ <-
          sequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundEffectPitch),
            targetedeffect.UnfocusedEffect {
              playerData.minestack.subtractStackedAmountOf(mineStackObj, grantAmount.toLong)
            },
            // アイテム付与はアトミックな操作ではない(コンテキストシフトを含む)ので、
            // subtractが終わってから行わなければならない
            Util.grantItemStacksEffect(itemStackToGrant)
          ).run(player)
      } yield ()
    })
  }

  def computeAutoMineStackToggleButton(): IO[Button] = RecomputedButton(IO {
    val playerData = SeichiAssist.playermap(getUniqueId)

    val iconItemStack = {
      val baseBuilder =
        new IconItemStackBuilder(Material.IRON_PICKAXE)
          .title(s"$YELLOW$UNDERLINE${BOLD}対象アイテム自動スタック機能")

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
      }
      }.build()

    val buttonEffect = action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
      sequentialEffect(
        playerData.settings.toggleAutoMineStack,
        deferredEffect(IO {
          val (message, soundPitch) =
            if (playerData.settings.autoMineStack) {
              (s"${GREEN}対象アイテム自動スタック機能:ON", 1.0f)
            } else {
              (s"${RED}対象アイテム自動スタック機能:OFF", 0.5f)
            }

          sequentialEffect(
            MessageEffect(message),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch)
          )
        })
      )
    }

    Button(iconItemStack, buttonEffect)
  })
}
package com.github.unchama.seichiassist.menus.minestack

import cats.data.Kleisli
import cats.effect.{IO, SyncIO}
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton, action}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.util.InventoryOperations.grantItemStacksEffect
import com.github.unchama.seichiassist.MineStackObjectList.getBuiltinGachaPrizes
import com.github.unchama.seichiassist.minestack.{MineStackObject, MineStackObjectCategory}
import com.github.unchama.seichiassist.{MineStackObjectList, SeichiAssist}
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

private object MineStackButtons {

  import scala.jdk.CollectionConverters._
  import scala.util.chaining._

  implicit class ItemStackOps(private val itemStack: ItemStack) extends AnyVal {
    def withAmount(amount: Int): ItemStack = itemStack.clone().tap(_.setAmount(amount))
  }

  implicit class MineStackObjectOps(private val mineStackObj: MineStackObject) extends AnyVal {
    def parameterizedWith(player: Player): ItemStack = {
      // ガチャ品であり、かつがちゃりんごでも経験値瓶でもなければ
      if (
        mineStackObj.category == MineStackObjectCategory.GACHA_PRIZES && !getBuiltinGachaPrizes
          .contains(mineStackObj)
      ) {
        for {
          gachaData <- SeichiAssist
            .msgachadatalist
            .find(_.itemStack.isSimilar(mineStackObj.itemStack))
        } {
          if (gachaData.probability < 0.1) {
            return mineStackObj.itemStack.clone().tap { cloned =>
              val meta = cloned.getItemMeta.tap { itemMeta =>
                val itemLore = if (itemMeta.hasLore) itemMeta.getLore.asScala.toList else List()
                itemMeta
                  .setLore((itemLore :+ s"$RESET${DARK_GREEN}所有者：${player.getName}").asJava)
              }
              cloned.setItemMeta(meta)
            }
          }
        }
      }

      mineStackObj.itemStack.clone()
    }
  }

}

private[minestack] case class MineStackButtons(player: Player) {

  import MineStackButtons._
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
  import com.github.unchama.targetedeffect._
  import player._

  import scala.jdk.CollectionConverters._

  def getMineStackItemButtonOf(mineStackObj: MineStackObject)(
    implicit onMainThread: OnMinecraftServerThread[IO],
    canOpenCategorizedMineStackMenu: IO CanOpen CategorizedMineStackMenu
  ): IO[Button] = RecomputedButton(IO {
    val playerData = SeichiAssist.playermap(getUniqueId)

    import scala.util.chaining._

    val itemStack =
      mineStackObj.itemStack.clone().tap { itemStack =>
        import itemStack._
        setItemMeta {
          getItemMeta.tap { itemMeta =>
            import itemMeta._
            setDisplayName {
              val name = mineStackObj
                .uiName
                .getOrElse(if (hasDisplayName) getDisplayName else getType.toString)

              s"$YELLOW$UNDERLINE$BOLD$name"
            }

            setLore {
              val stackedAmount = playerData.minestack.getStackedAmountOf(mineStackObj)
              val itemDetail = List(s"$RESET$GREEN${stackedAmount.formatted("%,d")}個")
              val operationDetail = {
                if (isRepresentativeMineStackObjectAndCategorizedMineStackMenu(mineStackObj)) {
                  List(s"$RESET${DARK_GREEN}クリックで色選択画面を開きます。")
                } else {
                  List(
                    s"$RESET$DARK_RED${UNDERLINE}左クリックで1スタック取り出し",
                    s"$RESET$DARK_AQUA${UNDERLINE}右クリックで1個取り出し"
                  )
                }
              }
              (itemDetail ++ operationDetail).asJava
            }
          }
        }
      }

    Button(
      itemStack,
      action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
        clickEffect(mineStackObj, mineStackObj.itemStack.getMaxStackSize)
      },
      action.FilteredButtonEffect(ClickEventFilter.RIGHT_CLICK) { _ =>
        clickEffect(mineStackObj, 1)
      }
    )
  })

  private def clickEffect(mineStackObj: MineStackObject, amount: Int)(
    implicit onMainThread: OnMinecraftServerThread[IO],
    canOpenCategorizedMineStackMenu: IO CanOpen CategorizedMineStackMenu
  ): Kleisli[IO, Player, Unit] = {
    val playerData = SeichiAssist.playermap(getUniqueId)
    SequentialEffect(
      withDrawItemEffect(mineStackObj, amount),
      targetedeffect.UnfocusedEffect {
        if (mineStackObj.category != MineStackObjectCategory.GACHA_PRIZES) {
          playerData.hisotryData.addHistory(mineStackObj)
        }
      }
    )
  }

  private def withDrawItemEffect(mineStackObj: MineStackObject, amount: Int)(
    implicit onMainThread: OnMinecraftServerThread[IO],
    canOpen: CanOpen[IO, CategorizedMineStackMenu]
  ): TargetedEffect[Player] = {
    if (isRepresentativeMineStackObjectAndCategorizedMineStackMenu(mineStackObj)) {
      implicit val mineStackSelectItemColorMenu: MineStackSelectItemColorMenu.Environment =
        new MineStackSelectItemColorMenu.Environment()
      MineStackSelectItemColorMenu(mineStackObj).open
    } else {
      for {
        pair <- Kleisli((player: Player) =>
          onMainThread.runAction {
            for {
              playerData <- SyncIO {
                SeichiAssist.playermap(player.getUniqueId)
              }
              currentAmount <- SyncIO {
                playerData.minestack.getStackedAmountOf(mineStackObj)
              }

              grantAmount = Math.min(amount, currentAmount).toInt

              soundEffectPitch = if (grantAmount == amount) 1.0f else 0.5f
              itemStackToGrant = mineStackObj.parameterizedWith(player).withAmount(grantAmount)

              _ <- SyncIO {
                playerData.minestack.subtractStackedAmountOf(mineStackObj, grantAmount.toLong)
              }
            } yield (soundEffectPitch, itemStackToGrant)
          }
        )
        _ <- SequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, pair._1),
          grantItemStacksEffect(pair._2)
        )
      } yield ()
    }
  }

  private def isRepresentativeMineStackObjectAndCategorizedMineStackMenu(
    mineStackObj: MineStackObject
  ): Boolean = {
    val isMineStackMainMenu =
      player.getOpenInventory.getTopInventory.getName == s"$DARK_PURPLE${BOLD}MineStackメインメニュー"
    val isMineStackSelectItemColorMenu =
      player.getOpenInventory.getTopInventory.getName == s"$DARK_BLUE${BOLD}MineStack(アイテム色選択)"
    MineStackObjectList
      .allMineStackObjects
      .exists(mineStackObject =>
        mineStackObject.exists(_.representative == mineStackObj)
      ) && !isMineStackSelectItemColorMenu && !isMineStackMainMenu
  }

  def computeAutoMineStackToggleButton(
    implicit onMainThread: OnMinecraftServerThread[IO]
  ): IO[Button] =
    RecomputedButton(IO {
      val playerData = SeichiAssist.playermap(getUniqueId)

      val iconItemStack = {
        val baseBuilder =
          new IconItemStackBuilder(Material.IRON_PICKAXE)
            .title(s"$YELLOW$UNDERLINE${BOLD}対象アイテム自動スタック機能")

        if (playerData.settings.autoMineStack) {
          baseBuilder
            .enchanted()
            .lore(List(s"$RESET${GREEN}現在ONです", s"$RESET$DARK_RED${UNDERLINE}クリックでOFF"))
        } else {
          baseBuilder.lore(
            List(s"$RESET${RED}現在OFFです", s"$RESET$DARK_GREEN${UNDERLINE}クリックでON")
          )
        }
      }.build()

      val buttonEffect = action.FilteredButtonEffect(ClickEventFilter.ALWAYS_INVOKE) { _ =>
        SequentialEffect(
          playerData.settings.toggleAutoMineStack,
          DeferredEffect(IO {
            val (message, soundPitch) =
              if (playerData.settings.autoMineStack) {
                (s"${GREEN}対象アイテム自動スタック機能:ON", 1.0f)
              } else {
                (s"${RED}対象アイテム自動スタック機能:OFF", 0.5f)
              }

            SequentialEffect(
              MessageEffect(message),
              FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch)
            )
          })
        )
      }

      Button(iconItemStack, buttonEffect)
    })
}

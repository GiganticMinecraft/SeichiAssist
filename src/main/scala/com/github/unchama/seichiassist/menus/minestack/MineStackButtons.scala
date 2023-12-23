package com.github.unchama.seichiassist.menus.minestack

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton, action}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectGroup,
  MineStackObjectWithKindVariants
}
import com.github.unchama.seichiassist.util.InventoryOperations.grantItemStacksEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

private[minestack] case class MineStackButtons(player: Player)(
  implicit mineStackAPI: MineStackAPI[IO, Player, ItemStack],
  gachaPrizeAPI: GachaPrizeAPI[IO, ItemStack, Player]
) {

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
  import com.github.unchama.targetedeffect._

  import scala.jdk.CollectionConverters._

  private def getMineStackObjectFromMineStackObjectGroup(
    mineStackObjectGroup: MineStackObjectGroup[ItemStack]
  ): MineStackObject[ItemStack] = {
    mineStackObjectGroup match {
      case Left(mineStackObject) =>
        mineStackObject
      case Right(MineStackObjectWithKindVariants(representative, _)) =>
        representative
    }
  }

  def getMineStackObjectButtonOf(
    mineStackObject: MineStackObject[ItemStack]
  )(implicit onMainThread: OnMinecraftServerThread[IO]): IO[Button] = RecomputedButton {
    val mineStackObjectGroup: MineStackObjectGroup[ItemStack] = Left(mineStackObject)

    for {
      itemStack <- getMineStackObjectIconItemStack(mineStackObjectGroup)
    } yield Button(
      itemStack,
      action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
        objectClickEffect(mineStackObject, itemStack.getMaxStackSize)
      },
      action.FilteredButtonEffect(ClickEventFilter.RIGHT_CLICK) { _ =>
        objectClickEffect(mineStackObject, 1)
      }
    )
  }

  private def getMineStackObjectIconItemStack(
    mineStackObjectGroup: MineStackObjectGroup[ItemStack]
  ): IO[ItemStack] = {
    import scala.util.chaining._

    val mineStackObject = mineStackObjectGroup match {
      case Left(mineStackObject) =>
        mineStackObject
      case Right(MineStackObjectWithKindVariants(representative, _)) =>
        representative
    }

    for {
      stackedAmount <- mineStackAPI
        .mineStackRepository
        .getStackedAmountOf(player, mineStackObject)
    } yield {
      mineStackObject.itemStack.tap { itemStack =>
        import itemStack._
        setItemMeta {
          getItemMeta.tap { itemMeta =>
            import itemMeta._
            setDisplayName {
              val name = mineStackObject
                .uiName
                .fold(if (hasDisplayName) getDisplayName else getType.toString)(itemName =>
                  itemName
                )

              s"$YELLOW$UNDERLINE$BOLD$name"
            }

            setLore {
              val operationDetail =
                if (mineStackObjectGroup.isRight) {
                  List(s"$RESET${DARK_GREEN}クリックで種類選択画面を開きます。")
                } else {
                  List(
                    s"$RESET$GREEN${String.format("%,d", stackedAmount)}個",
                    s"$RESET$DARK_RED${UNDERLINE}左クリックで1スタック取り出し",
                    s"$RESET$DARK_AQUA${UNDERLINE}右クリックで1個取り出し"
                  )
                }
              operationDetail.asJava
            }

            setAmount(1)
          }
        }
      }
    }

  }

  def getMineStackGroupButtonOf(
    mineStackObjectGroup: MineStackObjectGroup[ItemStack],
    oldPage: Int
  )(
    implicit onMainThread: OnMinecraftServerThread[IO],
    canOpenCategorizedMineStackMenu: IO CanOpen MineStackSelectItemKindMenu
  ): IO[Button] = RecomputedButton {
    for {
      itemStack <- getMineStackObjectIconItemStack(mineStackObjectGroup)
    } yield Button(
      itemStack,
      action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
        objectGroupClickEffect(mineStackObjectGroup, itemStack.getMaxStackSize, oldPage)
      },
      action.FilteredButtonEffect(ClickEventFilter.RIGHT_CLICK) { _ =>
        objectGroupClickEffect(mineStackObjectGroup, 1, oldPage)
      }
    )
  }

  private def objectClickEffect(mineStackObject: MineStackObject[ItemStack], amount: Int)(
    implicit onMainThread: OnMinecraftServerThread[IO]
  ): Kleisli[IO, Player, Unit] = {
    SequentialEffect(
      withDrawItemEffect(mineStackObject, amount),
      DeferredEffect {
        IO(mineStackAPI.addUsageHistory(mineStackObject))
      }
    )
  }

  private def objectGroupClickEffect(
    mineStackObjectGroup: MineStackObjectGroup[ItemStack],
    amount: Int,
    oldPage: Int
  )(
    implicit onMainThread: OnMinecraftServerThread[IO],
    canOpenMineStackSelectItemColorMenu: IO CanOpen MineStackSelectItemKindMenu
  ): Kleisli[IO, Player, Unit] = {
    mineStackObjectGroup match {
      case Left(mineStackObject) =>
        SequentialEffect(
          withDrawItemEffect(mineStackObject, amount),
          DeferredEffect {
            IO {
              mineStackAPI.addUsageHistory(
                getMineStackObjectFromMineStackObjectGroup(mineStackObjectGroup)
              )
            }
          }
        )
      case Right(mineStackObjectWithColorVariants) =>
        canOpenMineStackSelectItemColorMenu.open(
          MineStackSelectItemKindMenu(mineStackObjectWithColorVariants, oldPage)
        )
    }
  }

  private def withDrawItemEffect(mineStackObject: MineStackObject[ItemStack], amount: Int)(
    implicit onMainThread: OnMinecraftServerThread[IO]
  ): TargetedEffect[Player] = {
    for {
      pair <- Kleisli((player: Player) =>
        for {
          grantAmount <- mineStackAPI
            .mineStackRepository
            .subtractStackedAmountOf(player, mineStackObject, amount)
          soundEffectPitch = if (grantAmount == amount) 1.0f else 0.5f
          signedItemStack <- mineStackObject.tryToSignedItemStack[IO, Player](player.getName)
          itemStackToGrant = signedItemStack.getOrElse(mineStackObject.itemStack)
          // NOTE: grantAmountが64を超えることはないので、Intで問題ない
          _ = itemStackToGrant.setAmount(grantAmount.toInt)
        } yield (soundEffectPitch, itemStackToGrant)
      )
      _ <- SequentialEffect(
        FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, pair._1),
        grantItemStacksEffect(pair._2)
      )
    } yield ()
  }

  def computeAutoMineStackToggleButton(
    implicit onMainThread: OnMinecraftServerThread[IO]
  ): IO[Button] =
    RecomputedButton(for {
      currentAutoMineStackState <- mineStackAPI.autoMineStack(player)
    } yield {
      val iconItemStack = {
        val baseBuilder =
          new IconItemStackBuilder(Material.IRON_PICKAXE)
            .title(s"$YELLOW$UNDERLINE${BOLD}対象アイテム自動スタック機能")

        if (currentAutoMineStackState) {
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
        val toggleEffect = for {
          _ <- mineStackAPI.toggleAutoMineStack(player)
        } yield {
          val (message, soundPitch) =
            if (!currentAutoMineStackState) { // NOTE: トグルした後なので反転させる必要がある
              (s"${GREEN}対象アイテム自動スタック機能:ON", 1.0f)
            } else {
              (s"${RED}対象アイテム自動スタック機能:OFF", 0.5f)
            }

          SequentialEffect(
            MessageEffect(message),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch)
          )
        }

        SequentialEffect(DeferredEffect(toggleEffect))
      }

      Button(iconItemStack, buttonEffect)
    })
}

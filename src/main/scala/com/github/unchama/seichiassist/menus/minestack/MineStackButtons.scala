package com.github.unchama.seichiassist.menus.minestack

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.slot.button.action.ClickEventFilter
import com.github.unchama.menuinventory.slot.button.{Button, RecomputedButton, action}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaAPI
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectGroup,
  MineStackObjectWithColorVariants
}
import com.github.unchama.seichiassist.util.InventoryOperations.grantItemStacksEffect
import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

private[minestack] case class MineStackButtons(player: Player)(
  implicit mineStackAPI: MineStackAPI[IO, Player, ItemStack],
  gachaAPI: GachaAPI[IO, ItemStack, Player]
) {

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
  import com.github.unchama.targetedeffect._

  import scala.jdk.CollectionConverters._

  private def getMineStackObjectFromMineStackObjectGroup(
    mineStackObjectGroup: MineStackObjectGroup[ItemStack]
  ): MineStackObject[ItemStack] = {
    mineStackObjectGroup match {
      case Left(mineStackObject: MineStackObject[ItemStack]) =>
        mineStackObject
      case Right(MineStackObjectWithColorVariants(representative, _)) =>
        representative
    }
  }

  def getMineStackObjectButtonOf(mineStackObject: MineStackObject[ItemStack])(
    implicit onMainThread: OnMinecraftServerThread[IO],
    canOpenCategorizedMineStackMenu: IO CanOpen CategorizedMineStackMenu
  ): IO[Button] = RecomputedButton(IO {
    val mineStackObjectGroup: MineStackObjectGroup[ItemStack] = Left(mineStackObject)
    val itemStack = getMineStackObjectIconItemStack(mineStackObjectGroup)

    Button(
      itemStack,
      action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
        objectClickEffect(mineStackObject, itemStack.getMaxStackSize)
      },
      action.FilteredButtonEffect(ClickEventFilter.RIGHT_CLICK) { _ =>
        objectClickEffect(mineStackObject, 1)
      }
    )
  })

  def getMineStackObjectIconItemStack(
    mineStackObjectGroup: MineStackObjectGroup[ItemStack]
  ): ItemStack = {
    import scala.util.chaining._

    val mineStackObject = mineStackObjectGroup match {
      case Left(mineStackObject: MineStackObject[ItemStack]) =>
        mineStackObject
      case Right(MineStackObjectWithColorVariants(representative, _)) =>
        representative
    }

    mineStackObject.itemStack.tap { itemStack =>
      import itemStack._
      setItemMeta {
        getItemMeta.tap { itemMeta =>
          import itemMeta._
          setDisplayName {
            val name = mineStackObject
              .uiName
              .getOrElse(if (hasDisplayName) getDisplayName else getType.toString)

            s"$YELLOW$UNDERLINE$BOLD$name"
          }

          setLore {
            val stackedAmount =
              mineStackAPI.getStackedAmountOf(player, mineStackObject).unsafeRunSync()
            val itemDetail = List(s"$RESET$GREEN${stackedAmount.formatted("%,d")}個")
            val operationDetail = {
              if (mineStackObjectGroup.isRight) {
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

          setAmount(1)
        }
      }
    }
  }

  def getMineStackGroupButtonOf(
    mineStackObjectGroup: MineStackObjectGroup[ItemStack],
    oldPage: Int
  )(
    implicit onMainThread: OnMinecraftServerThread[IO],
    canOpenCategorizedMineStackMenu: IO CanOpen MineStackSelectItemColorMenu
  ): IO[Button] = RecomputedButton(IO {
    val itemStack = getMineStackObjectIconItemStack(mineStackObjectGroup)

    Button(
      itemStack,
      action.FilteredButtonEffect(ClickEventFilter.LEFT_CLICK) { _ =>
        objectGroupClickEffect(mineStackObjectGroup, itemStack.getMaxStackSize, oldPage)
      },
      action.FilteredButtonEffect(ClickEventFilter.RIGHT_CLICK) { _ =>
        objectGroupClickEffect(mineStackObjectGroup, 1, oldPage)
      }
    )
  })

  private def objectClickEffect(mineStackObject: MineStackObject[ItemStack], amount: Int)(
    implicit onMainThread: OnMinecraftServerThread[IO],
    canOpenCategorizedMineStackMenu: IO CanOpen CategorizedMineStackMenu
  ): Kleisli[IO, Player, Unit] = {
    SequentialEffect(
      withDrawItemEffect(mineStackObject, amount),
      targetedeffect.UnfocusedEffect {
        mineStackAPI.addUsageHistory(player, mineStackObject).unsafeRunSync()
      }
    )
  }

  private def objectGroupClickEffect(
    mineStackObjectGroup: MineStackObjectGroup[ItemStack],
    amount: Int,
    oldPage: Int
  )(
    implicit onMainThread: OnMinecraftServerThread[IO],
    canOpenMineStackSelectItemColorMenu: IO CanOpen MineStackSelectItemColorMenu
  ): Kleisli[IO, Player, Unit] = {
    SequentialEffect(
      mineStackObjectGroup match {
        case Left(mineStackObject: MineStackObject[ItemStack]) =>
          withDrawItemEffect(mineStackObject, amount)
        case Right(
              mineStackObjectWithColorVariants: MineStackObjectWithColorVariants[ItemStack]
            ) =>
          canOpenMineStackSelectItemColorMenu.open(
            MineStackSelectItemColorMenu(mineStackObjectWithColorVariants, oldPage)
          )
      },
      if (mineStackObjectGroup.isLeft)
        targetedeffect.UnfocusedEffect {
          mineStackAPI.addUsageHistory(
            player,
            getMineStackObjectFromMineStackObjectGroup(mineStackObjectGroup)
          )
        }
      else emptyEffect
    )
  }

  private def withDrawItemEffect(mineStackObject: MineStackObject[ItemStack], amount: Int)(
    implicit onMainThread: OnMinecraftServerThread[IO]
  ): TargetedEffect[Player] = {
    for {
      pair <- Kleisli((player: Player) =>
        for {
          grantAmount <- mineStackAPI.subtractStackedAmountOf(player, mineStackObject, amount)
          soundEffectPitch = if (grantAmount == amount) 1.0f else 0.5f
          signedItemStack <- mineStackObject.tryToSignedItemStack[IO, Player](player.getName)
          itemStackToGrant = signedItemStack.getOrElse(mineStackObject.itemStack)
          _ = itemStackToGrant.setAmount(amount)
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
        SequentialEffect(DeferredEffect(for {
          _ <- mineStackAPI.toggleAutoMineStack(player)
        } yield {
          val (message, soundPitch) =
            if (currentAutoMineStackState) {
              (s"${GREEN}対象アイテム自動スタック機能:ON", 1.0f)
            } else {
              (s"${RED}対象アイテム自動スタック機能:OFF", 0.5f)
            }

          SequentialEffect(
            MessageEffect(message),
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, soundPitch)
          )
        }))
      }

      Button(iconItemStack, buttonEffect)
    })
}

package com.github.unchama.seichiassist.menus.trade

import com.github.unchama.menuinventory.Menu
import com.github.unchama.menuinventory.MenuFrame
import cats.effect.IO
import com.github.unchama.menuinventory.MenuSlotLayout
import org.bukkit.entity.Player
import org.bukkit.ChatColor._
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import org.bukkit.inventory.ItemStack
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectCategory
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectGroup
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObjectWithKindVariants
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.GachaTradeAPI
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.Sound
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.menuinventory.slot.button.RecomputedButton
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import org.bukkit.Material
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.menuinventory.ChestSlotRef
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import com.github.unchama.itemstackbuilder.SkullItemStackBuilder
import com.github.unchama.seichiassist.SkullOwners
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.MineStackObject
import cats.data.Kleisli
import com.github.unchama.seichiassist.subsystems.tradesystems.subsystems.gachatrade.domain.TradeError

object GachaTradeFromMineStackMenu {
  class Environment(
    implicit val mineStackAPI: MineStackAPI[IO, Player, ItemStack],
    implicit val gachaTradeAPI: GachaTradeAPI[IO, Player, ItemStack],
    implicit val gachaPrizeAPI: GachaPrizeAPI[IO, ItemStack, Player],
    implicit val onMainThread: OnMinecraftServerThread[IO],
    implicit val ioCanOpenTradeSelector: IO CanOpen TradeSelector.type,
    implicit val ioCanOpenGachaTradeMenu: IO CanOpen GachaTradeFromMineStackMenu,
    implicit val playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
  )

  sealed trait ExchangeAmount {
    val amount: Int
  }

  object ExchangeAmount {
    case object One extends ExchangeAmount {
      override val amount: Int = 1
    }

    case object Ten extends ExchangeAmount {
      override val amount: Int = 10
    }

    case object Hundred extends ExchangeAmount {
      override val amount: Int = 100
    }

    case object Thousand extends ExchangeAmount {
      override val amount: Int = 1000
    }

    case object TenThousand extends ExchangeAmount {
      override val amount: Int = 10000
    }
  }
}

case class GachaTradeFromMineStackMenu(
  exchangeAmount: GachaTradeFromMineStackMenu.ExchangeAmount =
    GachaTradeFromMineStackMenu.ExchangeAmount.One
) extends Menu {

  import com.github.unchama.menuinventory.syntax._
  import cats.implicits._
  import eu.timepit.refined.auto._

  override val frame: MenuFrame = MenuFrame(6.chestRows, s"$LIGHT_PURPLE${BOLD}交換したい景品を選んでください")

  override type Environment = GachaTradeFromMineStackMenu.Environment

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    val gachaTradeButton = GachaTradeButton(player)
    import gachaTradeButton._
    import environment._

    val backtoTradeSelectorButton = CommonButtons.transferButton(
      new SkullItemStackBuilder(SkullOwners.MHF_ArrowLeft),
      "交換元を選び直す",
      TradeSelector
    )

    val uiOperation = MenuSlotLayout(
      ChestSlotRef(5, 0) -> backtoTradeSelectorButton,
      ChestSlotRef(5, 8) -> toggeleExchangeAmountButton
    )

    for {
      tradeButtons <- tradeButtons
      tradeButtons <- tradeButtons.sequence
      tradeButtonLayouts = tradeButtons
        .filterNot(_.itemStack.getType() == Material.AIR)
        .zipWithIndex
        .map {
          case (button, index) =>
            index -> button
        }
    } yield MenuSlotLayout(tradeButtonLayouts: _*).merge(uiOperation)

  }

  private case class GachaTradeButton(player: Player)(implicit environment: Environment) {
    import environment._
    import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext
    import scala.util.chaining.scalaUtilChainingOps
    import scala.jdk.CollectionConverters._

    private def tradeButtonFromMineStackObjectGroup(
      tradableItemStacks: Vector[ItemStack],
      mineStackObjectGroup: MineStackObjectGroup[ItemStack]
    ): IO[Button] = RecomputedButton {

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
        signedItemStackOpt <- mineStackObject.tryToSignedItemStack[IO, Player](player.getName())
        signedItemStack <- IO.pure(signedItemStackOpt.getOrElse(mineStackObject.itemStack))
      } yield {
        val itemStackForButton = mineStackObject.itemStack.tap { itemStack =>
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
                  List(
                    s"$RESET$GREEN${String.format("%,d", stackedAmount)}個",
                    s"$RESET$DARK_AQUA${UNDERLINE}左クリックで${String.format("%,d", exchangeAmount.amount)}個交換します"
                  )
                operationDetail.asJava
              }

              setAmount(1)
            }
          }
        }

        if (tradableItemStacks.contains(signedItemStack)) {
          Button(
            itemStackForButton,
            LeftClickButtonEffect {
              SequentialEffect(
                tradeEffect(mineStackObject, exchangeAmount.amount),
                FocusedSoundEffect(Sound.BLOCK_DISPENSER_FAIL, 1f, 1f)
              )
            }
          )
        } else {
          Button.empty
        }
      }

    }

    private def tradeEffect(
      mineStackObject: MineStackObject[ItemStack],
      tradeAmount: Int
    ): TargetedEffect[Player] = Kleisli { player: Player =>
      for {
        tradeResult <- gachaTradeAPI.tryTradeFromMineStack(player, mineStackObject, tradeAmount)
        name <- IO.pure {
          val itemStack = mineStackObject.itemStack
          val meta = itemStack.getItemMeta

          val name = mineStackObject
            .uiName
            .fold(if (meta.hasDisplayName) meta.getDisplayName else itemStack.getType.toString)(
              itemName => itemName
            )

          s"$YELLOW$UNDERLINE$BOLD$name"
        }
        _ <- (tradeResult match {
          case Left(TradeError.NotEnougthItemAmount) =>
            MessageEffect(s"$RED${BOLD}交換するアイテムが足りません。")
          case Left(TradeError.NotTradableItem) =>
            MessageEffect(s"$RED${BOLD}そのアイテムは交換できません。")
          case Right(result) =>
            val gachaTicketAmount = result.tradedSuccessResult.map(_.amount).sum
            MessageEffect(
              s"$name$GREEN$BOLD${tradeAmount}個と$GREEN$BOLD${gachaTicketAmount}枚のガチャ券を交換しました。"
            )
        }).apply(player)
      } yield ()
    }

    val tradeButtons: IO[Vector[IO[Button]]] = {
      for {
        gachaPrizes <- mineStackAPI
          .mineStackObjectList
          .getAllObjectGroupsInCategory(MineStackObjectCategory.GACHA_PRIZES)
        tradableItemStacks <- gachaTradeAPI.getTradableItems(player)
        buttons = gachaPrizes.map(gachaPrize =>
          tradeButtonFromMineStackObjectGroup(tradableItemStacks, gachaPrize)
        )
      } yield buttons.toVector

    }

    val toggeleExchangeAmountButton: Button = {
      val nextExchangeAmount = exchangeAmount match {
        case GachaTradeFromMineStackMenu.ExchangeAmount.One =>
          GachaTradeFromMineStackMenu.ExchangeAmount.Ten
        case GachaTradeFromMineStackMenu.ExchangeAmount.Ten =>
          GachaTradeFromMineStackMenu.ExchangeAmount.Hundred
        case GachaTradeFromMineStackMenu.ExchangeAmount.Hundred =>
          GachaTradeFromMineStackMenu.ExchangeAmount.Thousand
        case GachaTradeFromMineStackMenu.ExchangeAmount.Thousand =>
          GachaTradeFromMineStackMenu.ExchangeAmount.TenThousand
        case GachaTradeFromMineStackMenu.ExchangeAmount.TenThousand =>
          GachaTradeFromMineStackMenu.ExchangeAmount.One
      }

      val itemStack = new IconItemStackBuilder(Material.PAPER)
        .title(s"$AQUA$BOLD${nextExchangeAmount.amount}個交換に切り替え")
        .lore(
          List(
            s"$RESET$DARK_AQUA${UNDERLINE}左クリックで交換数を切り替えます",
            s"$RESET$DARK_AQUA${UNDERLINE}現在の交換数: ${exchangeAmount.amount}個"
          )
        )
        .build()

      Button(
        itemStack,
        LeftClickButtonEffect(
          SequentialEffect(
            FocusedSoundEffect(Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f, 1.0f),
            ioCanOpenGachaTradeMenu.open(GachaTradeFromMineStackMenu(nextExchangeAmount))
          )
        )
      )
    }

  }

}

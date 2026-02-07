package com.github.unchama.seichiassist.menus.trade

import com.github.unchama.menuinventory.Menu
import com.github.unchama.menuinventory.MenuFrame
import cats.effect.IO
import com.github.unchama.menuinventory.MenuSlotLayout
import org.bukkit.entity.Player
import org.bukkit.ChatColor._
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.seichiassist.menus.stickmenu.FirstPage
import com.github.unchama.seichiassist.menus.CommonButtons
import com.github.unchama.menuinventory.ChestSlotRef
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.itemstackbuilder.IconItemStackBuilder
import org.bukkit.Material
import com.github.unchama.seichiassist.menus.trade.GachaTradeFromMineStackMenu.ExchangeAmount
import com.github.unchama.menuinventory.slot.button.Button
import com.github.unchama.menuinventory.slot.button.action.LeftClickButtonEffect
import com.github.unchama.targetedeffect.player.PlayerEffects.openInventoryEffect
import com.github.unchama.util.InventoryUtil
import com.github.unchama.minecraft.actions.OnMinecraftServerThread

object TradeSelector extends Menu {

  import com.github.unchama.menuinventory.syntax._
  import eu.timepit.refined.auto._

  override val frame: MenuFrame = MenuFrame(4.chestRows, s"$LIGHT_PURPLE${BOLD}交換元を選んでください")

  class Environment(
    implicit val ioCanOpenFirstPage: IO CanOpen FirstPage.type,
    implicit val ioCanOpenGachaTradeMenu: IO CanOpen GachaTradeFromMineStackMenu,
    implicit val playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player],
    implicit val onMainThread: OnMinecraftServerThread[IO]
  )

  override def computeMenuLayout(
    player: Player
  )(implicit environment: Environment): IO[MenuSlotLayout] = {
    import environment._

    val tradeFromMineStackMenuButton = CommonButtons.transferButton(
      new IconItemStackBuilder(Material.CHEST),
      "マインスタックから交換",
      GachaTradeFromMineStackMenu(ExchangeAmount.One)
    )

    val tradeFromInventoryMenuButton = Button(
      new IconItemStackBuilder(Material.PLAYER_HEAD)
        .title(s"$RESET$YELLOW$UNDERLINE${BOLD}インベントリから交換")
        .build(),
      LeftClickButtonEffect(
        openInventoryEffect(
          InventoryUtil.createInventory(
            size = 6.chestRows,
            title = Some(s"$LIGHT_PURPLE${BOLD}交換したい景品を入れてください")
          )
        )
      )
    )

    IO.pure(
      MenuSlotLayout(
        ChestSlotRef(1, 3) -> tradeFromMineStackMenuButton,
        ChestSlotRef(1, 5) -> tradeFromInventoryMenuButton,
        ChestSlotRef(3, 0) -> CommonButtons.openStickMenu
      )
    )
  }

}

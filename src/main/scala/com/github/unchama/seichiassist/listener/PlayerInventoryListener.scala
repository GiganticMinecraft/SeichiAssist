package com.github.unchama.seichiassist.listener

import cats.effect.IO
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.data.player.GiganticBerserk
import com.github.unchama.seichiassist.listener.invlistener.OnClickTitleMenu
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.seichiassist.menus.nicknames.NickNameMenu
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryCloseEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.{Bukkit, Material, Sound}

class PlayerInventoryListener(
  implicit effectEnvironment: EffectEnvironment,
  ioCanOpenNicknameMenu: IO CanOpen NickNameMenu.type,
  ioOnMainThread: OnMinecraftServerThread[IO]
) extends Listener {

  import com.github.unchama.targetedeffect._
  import com.github.unchama.util.InventoryUtil._
  import com.github.unchama.util.syntax._

  private val playerMap = SeichiAssist.playermap

  // 実績メニューの処理
  @EventHandler
  def onPlayerClickTitleMenuEvent(event: InventoryClickEvent): Unit = {
    OnClickTitleMenu.onPlayerClickTitleMenuEvent(event)
  }

  // 鉱石・交換券変換システム
  @EventHandler
  def onOreTradeEvent(event: InventoryCloseEvent): Unit = {
    val player = event.getPlayer.asInstanceOf[Player]

    // エラー分岐
    val inventory = event.getInventory

    // インベントリサイズが36でない時終了
    if (inventory.row != 4) return

    if (inventory.getTitle != s"$LIGHT_PURPLE${BOLD}交換したい鉱石を入れてください") return

    /*
     * step1 for文でinventory内の対象商品の個数を計算
     * 非対象商品は返却boxへ
     */

    // 石炭とラピスラズリを適切に処理するため、typeとdurabilityを持つクラスを用意
    case class ExchangeableMaterial(materialType: Material, durability: Short)

    val requiredAmountPerTicket = Map(
      ExchangeableMaterial(Material.COAL_ORE, 0) -> 128,
      ExchangeableMaterial(Material.IRON_ORE, 0) -> 64,
      ExchangeableMaterial(Material.GOLD_ORE, 0) -> 8,
      ExchangeableMaterial(Material.LAPIS_ORE, 0) -> 8,
      ExchangeableMaterial(Material.DIAMOND_ORE, 0) -> 4,
      ExchangeableMaterial(Material.REDSTONE_ORE, 0) -> 32,
      ExchangeableMaterial(Material.EMERALD_ORE, 0) -> 4,
      ExchangeableMaterial(Material.NETHER_QUARTZ_ORE, 0) -> 16,
      ExchangeableMaterial(Material.COAL, 0) -> 432,
      ExchangeableMaterial(Material.REDSTONE, 0) -> 288,
      ExchangeableMaterial(Material.INK_SAC, 4) -> 64,
      ExchangeableMaterial(Material.DIAMOND, 0) -> 8
    )

    val inventoryContents = inventory.getContents.filter(_ != null)

    val (itemsToExchange, rejectedItems) =
      inventoryContents.partition { stack =>
        requiredAmountPerTicket.contains(
          ExchangeableMaterial(stack.getType, stack.getDurability)
        )
      }

    val exchangingAmount = itemsToExchange
      .groupBy(stacks => ExchangeableMaterial(stacks.getType, stacks.getDurability))
      .toList
      .map { case (key, stacks) => key -> stacks.map(_.getAmount).sum }

    val ticketAmount = exchangingAmount.map {
      case (exchangeableMaterial, amount) =>
        amount / requiredAmountPerTicket(exchangeableMaterial)
    }.sum

    // プレイヤー通知
    if (ticketAmount == 0) {
      player.sendMessage(s"${YELLOW}鉱石を認識しなかったか数が不足しています。全てのアイテムを返却します")
    } else {
      player.sendMessage(s"${DARK_RED}交換券$RESET${GREEN}を${ticketAmount}枚付与しました")
    }

    /*
     * step2 交換券をインベントリへ
     */
    val exchangeTicket = {
      import scala.util.chaining._
      new ItemStack(Material.PAPER).tap {
        _.setItemMeta {
          Bukkit.getItemFactory.getItemMeta(Material.PAPER).tap { m =>
            import m._
            setDisplayName(s"$DARK_RED${BOLD}交換券")
            addEnchant(Enchantment.PROTECTION_FIRE, 1, false)
            addItemFlags(ItemFlag.HIDE_ENCHANTS)
          }
        }
      }
    }

    val ticketsToGive = Seq.fill(ticketAmount)(exchangeTicket)

    if (ticketsToGive.nonEmpty) {
      effectEnvironment.unsafeRunAsyncTargetedEffect(player)(
        SequentialEffect(
          InventoryOperations.grantItemStacksEffect(ticketsToGive: _*),
          FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1f, 1f),
          MessageEffect(s"${GREEN}交換券の付与が終わりました")
        ),
        "交換券を付与する"
      )
    }

    /*
     * step3 非対象・余剰鉱石の返却
     */
    val itemStacksToReturn =
      exchangingAmount
        .flatMap {
          case (exchangedMaterial, exchangedAmount) =>
            val returningAmount = exchangedAmount % requiredAmountPerTicket(exchangedMaterial)
            if (returningAmount != 0)
              Some(
                new ItemStack(
                  exchangedMaterial.materialType,
                  returningAmount,
                  exchangedMaterial.durability
                )
              )
            else
              None
        }
        .++(rejectedItems)

    // 返却処理
    effectEnvironment.unsafeRunAsyncTargetedEffect(player)(
      InventoryOperations.grantItemStacksEffect(itemStacksToReturn: _*),
      "鉱石交換でのアイテム返却を行う"
    )
  }

  @EventHandler
  def onGiganticBerserkMenuEvent(event: InventoryClickEvent): Unit = {
    // 外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    // インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }

    // インベントリが存在しない時終了
    val topinventory = view.getTopInventory.ifNull {
      return
    }

    // インベントリが6列でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap(uuid)

    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "スキルを進化させますか?") {
      event.setCancelled(true)
      if (itemstackcurrent.getType == Material.NETHER_STAR) {
        playerdata.giganticBerserk = GiganticBerserk(0, 0, playerdata.giganticBerserk.stage + 1)
        player.playSound(player.getLocation, Sound.BLOCK_END_GATEWAY_SPAWN, 1f, 0.5f)
        player.playSound(player.getLocation, Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1f, 0.8f)
        player.openInventory(MenuInventoryData.getGiganticBerserkAfterEvolutionMenu(player))
      }
    } else if (topinventory.getTitle == LIGHT_PURPLE.toString + "" + BOLD + "スキルを進化させました") {
      event.setCancelled(true)
    }

  }
}

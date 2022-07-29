package com.github.unchama.seichiassist.listener

import cats.effect.{IO, SyncIO}
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.data.player.GiganticBerserk
import com.github.unchama.seichiassist.data.{GachaSkullData, MenuInventoryData}
import com.github.unchama.seichiassist.listener.invlistener.OnClickTitleMenu
import com.github.unchama.seichiassist.menus.achievement.AchievementMenu
import com.github.unchama.seichiassist.menus.stickmenu.FirstPage
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.util.{InventoryOperations, StaticGachaPrizeFactory}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor._
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryCloseEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.{Bukkit, Material, Sound}

import scala.collection.mutable.ArrayBuffer

class PlayerInventoryListener(
  implicit effectEnvironment: EffectEnvironment,
  manaApi: ManaApi[IO, SyncIO, Player],
  ioCanOpenFirstPage: IO CanOpen FirstPage.type,
  ioCanOpenAchievementMenu: IO CanOpen AchievementMenu.type,
  ioOnMainThread: OnMinecraftServerThread[IO]
) extends Listener {

  import com.github.unchama.targetedeffect._
  import com.github.unchama.util.InventoryUtil._
  import com.github.unchama.util.syntax._

  private val playerMap = SeichiAssist.playermap
  private val gachaDataList = SeichiAssist.gachadatalist

  // ガチャ交換システム
  @EventHandler
  def onGachaTradeEvent(event: InventoryCloseEvent): Unit = {
    val player = event.getPlayer.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap(uuid).ifNull {
      return
    }
    // エラー分岐
    val name = playerdata.lowercaseName
    val inventory = event.getInventory

    // インベントリサイズが36でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.getTitle == s"${LIGHT_PURPLE.toString}${BOLD}交換したい景品を入れてください") {
      var givegacha = 0
      /*
       * step1 for文でinventory内に対象商品がないか検索
       * あったらdurabilityに応じてgivegachaを増やし、非対象商品は返却boxへ
       */
      // ガチャ景品交換インベントリの中身を取得
      val items = inventory.getContents
      // ドロップ用アイテムリスト(返却box)作成
      val dropitem = ArrayBuffer[ItemStack]()
      // カウント用
      var big = 0
      var reg = 0

      // for文で１個ずつ対象アイテムか見る
      // ガチャ景品交換インベントリを一個ずつ見ていくfor文
      items.foreach {
        case null =>
        case item
            if !item.hasItemMeta ||
              !item.getItemMeta.hasLore ||
              item.getType == Material.SKULL_ITEM =>
          dropitem += item
        case item =>
          // ガチャ景品リスト上を線形探索する
          val matchingGachaData = gachaDataList.find { gachadata =>
            // ガチャ景品リストにある商品の場合(Lore=説明文と表示名で判別),無い場合はアイテム返却
            if (
              gachadata.itemStack.hasItemMeta && gachadata
                .itemStack
                .getItemMeta
                .hasLore && gachadata.compare(item, name)
            ) {
              if (SeichiAssist.DEBUG)
                player.sendMessage(gachadata.itemStack.getItemMeta.getDisplayName)
              val amount = item.getAmount

              if (gachadata.probability < 0.001) {
                // ギガンティック大当たりの部分
                // ガチャ券に交換せずそのままアイテムを返す
                dropitem += item
              } else if (gachadata.probability < 0.01) {
                // 大当たりの部分
                givegacha += 12 * amount
                big += amount
              } else if (gachadata.probability < 0.1) {
                // 当たりの部分
                givegacha += 3 * amount
                reg += amount
              } else {
                // それ以外アイテム返却(経験値ポーションとかがここにくるはず)
                dropitem += item
              }
              true
            } else false
          }
          matchingGachaData match {
            // ガチャ景品リストに対象アイテムが無かった場合
            case None => dropitem += item
            case _    =>
          }
      }
      if (big <= 0 && reg <= 0) {
        player.sendMessage(s"${YELLOW}景品を認識しませんでした。全てのアイテムを返却します")
      } else {
        player.sendMessage(s"${GREEN}大当たり景品を${big}個、当たり景品を${reg}個認識しました")
      }
      /*
       * step2 非対象商品をインベントリに戻す
       */
      for (m <- dropitem) {
        if (!InventoryOperations.isPlayerInventoryFull(player)) {
          InventoryOperations.addItem(player, m)
        } else {
          InventoryOperations.dropItem(player, m)
        }
      }
      /*
       * step3 ガチャ券をインベントリへ
       */
      val skull = GachaSkullData.gachaForExchanging
      var count = 0
      while (givegacha > 0) {
        if (
          player.getInventory.contains(skull) || !InventoryOperations.isPlayerInventoryFull(
            player
          )
        ) {
          InventoryOperations.addItem(player, skull)
        } else {
          InventoryOperations.dropItem(player, skull)
        }
        givegacha -= 1
        count += 1
      }
      if (count > 0) {
        player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
        player.sendMessage(
          GREEN.toString + "" + count + "枚の" + GOLD + "ガチャ券" + WHITE + "を受け取りました"
        )
      }
    }

  }

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

    val requiredAmountPerTicket = Map(
      Material.COAL_ORE -> 128,
      Material.IRON_ORE -> 64,
      Material.GOLD_ORE -> 8,
      Material.LAPIS_ORE -> 8,
      Material.DIAMOND_ORE -> 4,
      Material.REDSTONE_ORE -> 32,
      Material.EMERALD_ORE -> 4,
      Material.QUARTZ_ORE -> 16
    )

    val inventoryContents = inventory.getContents.filter(_ != null)

    val (itemsToExchange, rejectedItems) =
      inventoryContents.partition { stack => requiredAmountPerTicket.contains(stack.getType) }

    val exchangingAmount = itemsToExchange.groupBy(_.getType).toList.map {
      case (key, stacks) => key -> stacks.map(_.getAmount).sum
    }

    val ticketAmount = exchangingAmount.map {
      case (material, amount) => amount / requiredAmountPerTicket(material)
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
            import scala.util.chaining._
            if (returningAmount != 0)
              Some(new ItemStack(exchangedMaterial).tap(_.setAmount(returningAmount)))
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

  // ギガンティック→椎名林檎交換システム
  @EventHandler
  def onGachaRingoEvent(event: InventoryCloseEvent): Unit = {
    val player = event.getPlayer.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap(uuid).ifNull {
      return
    }
    // エラー分岐
    val name = playerdata.lowercaseName
    val inventory = event.getInventory

    // インベントリサイズが4列でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.getTitle == GOLD.toString + "" + BOLD + "椎名林檎と交換したい景品を入れてネ") {
      var giveringo = 0
      /*
       * step1 for文でinventory内に対象商品がないか検索
       * あったらdurabilityに応じてgivegachaを増やし、非対象商品は返却boxへ
       */
      // ガチャ景品交換インベントリの中身を取得
      val item = inventory.getContents
      // ドロップ用アイテムリスト(返却box)作成
      val dropitem = ArrayBuffer[ItemStack]()
      // カウント用
      var giga = 0
      // for文で１個ずつ対象アイテムか見る
      // ガチャ景品交換インベントリを一個ずつ見ていくfor文
      item.foreach {
        case null =>
        case m
            if !m.hasItemMeta ||
              !m.getItemMeta.hasLore ||
              m.getType == Material.SKULL_ITEM =>
          dropitem.addOne(m)
        case m =>
          // ガチャ景品リストを一個ずつ見ていくfor文
          gachaDataList.find { gachadata =>
            if (
              gachadata.itemStack.hasItemMeta && gachadata
                .itemStack
                .getItemMeta
                .hasLore && gachadata.compare(m, name)
            ) {
              if (SeichiAssist.DEBUG) {
                player.sendMessage(gachadata.itemStack.getItemMeta.getDisplayName)
              }
              val amount = m.getAmount
              if (gachadata.probability < 0.001) {
                // ギガンティック大当たりの部分
                // 1個につき椎名林檎n個と交換する
                giveringo += SeichiAssist.seichiAssistConfig.rateGiganticToRingo * amount
                giga += 1
              } else {
                // それ以外アイテム返却
                dropitem.addOne(m)
              }
              true
            } else false
          } match {
            case None => dropitem.addOne(m)
            case _    =>
          }
      }
      if (giga <= 0) {
        player.sendMessage(YELLOW.toString + "ギガンティック大当り景品を認識しませんでした。全てのアイテムを返却します")
      } else {
        player.sendMessage(GREEN.toString + "ギガンティック大当り景品を" + giga + "個認識しました")
      }
      /*
       * step2 非対象商品をインベントリに戻す
       */
      for (m <- dropitem) {
        if (!InventoryOperations.isPlayerInventoryFull(player)) {
          InventoryOperations.addItem(player, m)
        } else {
          InventoryOperations.dropItem(player, m)
        }
      }
      /*
       * step3 椎名林檎をインベントリへ
       */
      val ringo = StaticGachaPrizeFactory.getMaxRingo(player.getName)
      var count = 0
      while (giveringo > 0) {
        if (
          player.getInventory.contains(ringo) || !InventoryOperations.isPlayerInventoryFull(
            player
          )
        ) {
          InventoryOperations.addItem(player, ringo)
        } else {
          InventoryOperations.dropItem(player, ringo)
        }
        giveringo -= 1
        count += 1
      }
      if (count > 0) {
        player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
        player.sendMessage(
          GREEN.toString + "" + count + "個の" + GOLD + "椎名林檎" + WHITE + "を受け取りました"
        )
      }
    }

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
        player.playSound(player.getLocation, Sound.ENTITY_ENDERDRAGON_AMBIENT, 1f, 0.8f)
        player.openInventory(MenuInventoryData.getGiganticBerserkAfterEvolutionMenu(player))
      }
    } else if (topinventory.getTitle == LIGHT_PURPLE.toString + "" + BOLD + "スキルを進化させました") {
      event.setCancelled(true)
    }

  }
}

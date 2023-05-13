package com.github.unchama.seichiassist.listener.invlistener

import cats.effect.IO
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.data.MenuInventoryData.MenuType
import com.github.unchama.seichiassist.menus.nicknames.NickNameMenu
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Material, Sound}

object OnClickTitleMenu {
  private final val MAX_LENGTH: Int = 8
  private final val PER_PAGE: Int = 9 * 3
  private final val LENGTH_LIMIT_EXCEEDED: String = s"全パーツ合計で${MAX_LENGTH}文字以内になるよう設定してください。"

  private def clickedSound(player: Player, sound: Sound, pitch: Float): Unit =
    player.playSound(player.getLocation, sound, 1f, pitch)

  private def isApplicableAsNextPageButton(is: ItemStack): Boolean =
    is.getItemMeta.asInstanceOf[SkullMeta].getOwningPlayer.getName == "MHF_ArrowRight"

  def onPlayerClickTitleMenuEvent(
    event: InventoryClickEvent
  )(implicit ioCanOpenNicknameMenu: IO CanOpen NickNameMenu.type): Unit = {
    import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver

    // 外枠のクリック処理なら終了
    event
      .getClickedInventory
      .ifNull(
        return
      )

    // インベントリを開けたのがプレイヤーではない時終了
    val view = event.getView

    val he = view.getPlayer
    if (he.getType != EntityType.PLAYER) {
      return
    }

    // インベントリが存在しない時終了
    val topInventory = view
      .getTopInventory
      .ifNull(
        return
      )

    import com.github.unchama.util.InventoryUtil._

    // インベントリサイズが4列でない時終了
    if (topInventory.row != 4) {
      return
    }
    val current = event.getCurrentItem

    val player = he.asInstanceOf[Player]
    val pd = SeichiAssist.playermap(player.getUniqueId)

    if (event.getClickedInventory.getType == InventoryType.PLAYER) {
      // プレイヤーインベントリのクリックの場合終了
      return
    }

    val mat = current.getType
    val isSkull = mat == Material.PLAYER_HEAD
    topInventory.getTitle match {
      case MenuType.HEAD.invName =>
        event.setCancelled(true)
        mat match {
          case Material.WATER_BUCKET =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

            val id = current.getItemMeta.getDisplayName.toInt
            val length = Nicknames
              .getCombinedNicknameFor(id, pd.settings.nickname.id2, pd.settings.nickname.id3)
              .getOrElse("")
              .length
            if (length > MAX_LENGTH) {
              player.sendMessage(LENGTH_LIMIT_EXCEEDED)
            } else {
              pd.updateNickname(id1 = id)
              player.sendMessage(
                "前パーツ「" + Nicknames
                  .getHeadPartFor(pd.settings.nickname.id1)
                  .getOrElse("") + "」をセットしました。"
              )
            }

          case Material.GRASS =>
            // unselect
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)
            pd.updateNickname(id1 = 0)
            player.sendMessage("前パーツの選択を解除しました。")

          case Material.BARRIER =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            ioCanOpenNicknameMenu.open(NickNameMenu).apply(player).unsafeRunAsyncAndForget()

          case _ if isSkull && isApplicableAsNextPageButton(current) =>
            // 次ページ
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            val uuid = player.getUniqueId
            val menuType = MenuInventoryData.MenuType.HEAD
            MenuInventoryData.setHeadingIndex(
              uuid,
              menuType,
              MenuInventoryData.getHeadingIndex(uuid, menuType).get + PER_PAGE
            )
            player.openInventory(MenuInventoryData.computeHeadPartCustomMenu(player))

          case _ =>
        }

      case MenuType.MIDDLE.invName =>
        event.setCancelled(true)
        mat match {
          case Material.MILK_BUCKET =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

            val id = current.getItemMeta.getDisplayName.toInt
            val length = Nicknames
              .getCombinedNicknameFor(pd.settings.nickname.id1, id, pd.settings.nickname.id3)
              .getOrElse("")
              .length
            if (length > MAX_LENGTH) {
              player.sendMessage(LENGTH_LIMIT_EXCEEDED)
            } else {
              pd.updateNickname(id2 = id)
              player.sendMessage(
                "中パーツ「" + Nicknames
                  .getMiddlePartFor(pd.settings.nickname.id2)
                  .getOrElse("") + "」をセットしました。"
              )
            }

          case Material.GRASS =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)
            pd.updateNickname(id2 = 0)
            player.sendMessage("中パーツの選択を解除しました。")

          case Material.BARRIER =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            ioCanOpenNicknameMenu.open(NickNameMenu).apply(player).unsafeRunAsyncAndForget()

          case _ if isSkull && isApplicableAsNextPageButton(current) =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            val uuid = player.getUniqueId
            val menuType = MenuInventoryData.MenuType.MIDDLE
            MenuInventoryData.setHeadingIndex(
              uuid,
              menuType,
              MenuInventoryData.getHeadingIndex(uuid, menuType).get + PER_PAGE
            )
            player.openInventory(MenuInventoryData.computeMiddlePartCustomMenu(player))

          case _ =>
        }

      case MenuType.TAIL.invName =>
        event.setCancelled(true)
        mat match {
          case Material.LAVA_BUCKET =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

            val id = current.getItemMeta.getDisplayName.toInt
            val length = Nicknames
              .getCombinedNicknameFor(pd.settings.nickname.id1, pd.settings.nickname.id2, id)
              .getOrElse("")
              .length
            if (length > MAX_LENGTH) {
              player.sendMessage(LENGTH_LIMIT_EXCEEDED)
            } else {
              pd.updateNickname(id3 = id)
              player.sendMessage(
                "後パーツ「" + Nicknames
                  .getTailPartFor(pd.settings.nickname.id3)
                  .getOrElse("") + "」をセットしました。"
              )
            }

          case Material.GRASS =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)
            pd.updateNickname(id3 = 0)
            player.sendMessage("後パーツの選択を解除しました。")

          case Material.BARRIER =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            ioCanOpenNicknameMenu.open(NickNameMenu).apply(player).unsafeRunAsyncAndForget()

          case _ if isSkull && isApplicableAsNextPageButton(current) =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            val uuid = player.getUniqueId
            val menuType = MenuInventoryData.MenuType.TAIL
            MenuInventoryData.setHeadingIndex(
              uuid,
              menuType,
              MenuInventoryData.getHeadingIndex(uuid, menuType).get + PER_PAGE
            )
            player.openInventory(MenuInventoryData.computeTailPartCustomMenu(player))

          case _ =>
        }

      case MenuType.SHOP.invName =>
        event.setCancelled(true)
        mat match {
          // 実績ポイント最新化
          case Material.EMERALD_ORE =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)
            pd.recalculateAchievePoint()
            pd.samepageflag = true
            player.openInventory(MenuInventoryData.computePartsShopMenu(player))

          // 購入処理
          case Material.BEDROCK =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

            val num = current.getItemMeta.getDisplayName.toInt
            val isHead = num < 9900
            val required = if (isHead) 20 else 35
            val getPart = if (isHead) { num => Nicknames.getHeadPartFor(num) }
            else { num => Nicknames.getMiddlePartFor(num) }

            if (pd.achievePoint.left >= required) {
              pd.TitleFlags.addOne(num)
              pd.consumeAchievePoint(required)
              player.sendMessage("パーツ「" + getPart(num).getOrElse("") + "」を購入しました。")
              pd.samepageflag = true
              player.openInventory(MenuInventoryData.computePartsShopMenu(player))
            } else {
              player.sendMessage("実績ポイントが不足しています。")
            }

          case Material.BARRIER =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            ioCanOpenNicknameMenu.open(NickNameMenu).apply(player).unsafeRunAsyncAndForget()

          case _ if isSkull && isApplicableAsNextPageButton(current) =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            val uuid = player.getUniqueId
            val menuType = MenuInventoryData.MenuType.SHOP
            MenuInventoryData.setHeadingIndex(
              uuid,
              menuType,
              MenuInventoryData.getHeadingIndex(uuid, menuType).get + PER_PAGE
            )
            player.openInventory(MenuInventoryData.computePartsShopMenu(player))

          case _ =>
        }

      // それ以外のインベントリの名前だった場合何もしない！
      case _ =>
    }
  }
}

package com.github.unchama.seichiassist.listener.invlistener

import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.data.MenuInventoryData.MenuType
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.targetedeffect.SequentialEffect
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Material, Sound}

object OnClickTitleMenu {
  private final val MAX_LENGTH: Int = 8
  private final val PER_PAGE: Int = 9*3
  private final val LENGTH_LIMIT_EXCEEDED: String = s"全パーツ合計で${MAX_LENGTH}文字以内になるよう設定してください。"

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.syncShift

  private def clickedSound(player: Player, sound: Sound, pitch: Float): Unit =
    player.playSound(player.getLocation, sound, 1f, pitch)

  private def isApplicableAsNextPageButton(is: ItemStack): Boolean =
    is.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowRight"

  private def isApplicableAsPrevPageButton(is: ItemStack): Boolean =
    is.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowLeft"

  def onPlayerClickTitleMenuEvent(event: InventoryClickEvent)(implicit effectEnvironment: EffectEnvironment): Unit = {
    import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver

    //外枠のクリック処理なら終了
    event.getClickedInventory.ifNull(return)

    //インベントリを開けたのがプレイヤーではない時終了
    val view = event.getView

    val he = view.getPlayer
    if (he.getType != EntityType.PLAYER) {
      return
    }

    //インベントリが存在しない時終了
    val topInventory = view.getTopInventory.ifNull(return)

    import com.github.unchama.util.InventoryUtil._

    //インベントリサイズが4列でない時終了
    if (topInventory.row != 4) {
      return
    }
    val current = event.getCurrentItem

    val player = he.asInstanceOf[Player]
    val pd = SeichiAssist.playermap(player.getUniqueId)

    if (event.getClickedInventory.getType == InventoryType.PLAYER) {
      //プレイヤーインベントリのクリックの場合終了
      return
    }

    event.setCancelled(true)

    val mat = current.getType
    val isSkull = mat == Material.SKULL_ITEM
    topInventory.getTitle match {
      case MenuType.COMBINE.invName =>
        // 二つ名組み合わせトップ
        mat match {
          //実績ポイント最新化
          case Material.EMERALD_ORE =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

          //エフェクトポイント→実績ポイント変換
          case Material.EMERALD =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)
            if (pd.effectPoint >= 10) {
              pd.convertEffectPointToAchievePoint()
            } else {
              player.sendMessage("エフェクトポイントが不足しています。")
            }

          //パーツショップ
          case Material.ITEM_FRAME =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            player.openInventory(MenuInventoryData.computePartsShopMenu(player))

          //前パーツ
          case Material.WATER_BUCKET =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            player.openInventory(MenuInventoryData.computeHeadPartCustomMenu(player))

          //中パーツ
          case Material.MILK_BUCKET =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            player.openInventory(MenuInventoryData.computeMiddlePartCustomMenu(player))

          //後パーツ
          case Material.LAVA_BUCKET =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            player.openInventory(MenuInventoryData.computeTailPartCustomMenu(player))

          case _ if isSkull && isApplicableAsNextPageButton(current) =>
            import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

            effectEnvironment.runAsyncTargetedEffect(player)(
              SequentialEffect(
                CommonSoundEffects.menuTransitionFenceSound,
                StickMenu.firstPage.open
              ),
              "実績メニューを開く"
            )

          case _ =>
        }

        // refresh if needed
        mat match {
          case Material.EMERALD_ORE | Material.EMERALD =>
            pd.recalculateAchievePoint()
            player.openInventory(MenuInventoryData.computeRefreshedCombineMenu(player))

          case _ =>
        }
        
      case MenuType.HEAD.invName =>
        mat match {
          case Material.WATER_BUCKET =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

            val id = current.getItemMeta.getDisplayName.toInt
            val length = Nicknames.getTitleFor(id,
              pd.settings.nickname.id2, pd.settings.nickname.id3).length
            if (length > MAX_LENGTH) {
              player.sendMessage(LENGTH_LIMIT_EXCEEDED)
            } else {
              pd.updateNickname(id1 = id)
              player.sendMessage("前パーツ「" + Nicknames.getHeadPartFor(pd.settings.nickname.id1).getOrElse("") + "」をセットしました。")
            }

          case Material.GRASS =>
            // unselect
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)
            pd.updateNickname(id1 = 0)
            player.sendMessage("前パーツの選択を解除しました。")

          case Material.BARRIER =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            player.openInventory(MenuInventoryData.computeRefreshedCombineMenu(player))

          case _ if isSkull && isApplicableAsNextPageButton(current) =>
            // 次ページ
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            val uuid = player.getUniqueId
            val menuType = MenuInventoryData.MenuType.HEAD
            MenuInventoryData.setHeadingIndex(uuid, menuType, MenuInventoryData.getHeadingIndex(uuid, menuType).get + PER_PAGE)
            player.openInventory(MenuInventoryData.computeHeadPartCustomMenu(player))

          case _ =>
        }

      case MenuType.MIDDLE.invName =>
        mat match {
          case Material.MILK_BUCKET =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

            val id = current.getItemMeta.getDisplayName.toInt
            val length = Nicknames.getTitleFor(pd.settings.nickname.id1,
              id, pd.settings.nickname.id3).length
            if (length > MAX_LENGTH) {
              player.sendMessage(LENGTH_LIMIT_EXCEEDED)
            } else {
              pd.updateNickname(id2 = id)
              player.sendMessage("中パーツ「" + Nicknames.getMiddlePartFor(pd.settings.nickname.id2).getOrElse("") + "」をセットしました。")
            }

          case Material.GRASS =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)
            pd.updateNickname(id2 = 0)
            player.sendMessage("中パーツの選択を解除しました。")

          case Material.BARRIER =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            player.openInventory(MenuInventoryData.computeRefreshedCombineMenu(player))

          case _ if isSkull && isApplicableAsNextPageButton(current) =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            val uuid = player.getUniqueId
            val menuType = MenuInventoryData.MenuType.MIDDLE
            MenuInventoryData.setHeadingIndex(uuid, menuType, MenuInventoryData.getHeadingIndex(uuid, menuType).get + PER_PAGE)
            player.openInventory(MenuInventoryData.computeMiddlePartCustomMenu(player))

          case _ =>
        }

      case MenuType.TAIL.invName =>
        mat match {
          case Material.LAVA_BUCKET =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

            val id = current.getItemMeta.getDisplayName.toInt
            val length = Nicknames.getTitleFor(pd.settings.nickname.id1,
              pd.settings.nickname.id2, id).length
            if (length > MAX_LENGTH) {
              player.sendMessage(LENGTH_LIMIT_EXCEEDED)
            } else {
              pd.updateNickname(id3 = id)
              player.sendMessage("後パーツ「" + Nicknames.getTailPartFor(pd.settings.nickname.id3).getOrElse("") + "」をセットしました。")
            }

          case Material.GRASS =>
            clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)
            pd.updateNickname(id3 = 0)
            player.sendMessage("後パーツの選択を解除しました。")

          case Material.BARRIER =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            player.openInventory(MenuInventoryData.computeRefreshedCombineMenu(player))

          case _ if isSkull && isApplicableAsNextPageButton(current) =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            val uuid = player.getUniqueId
            val menuType = MenuInventoryData.MenuType.TAIL
            MenuInventoryData.setHeadingIndex(uuid, menuType, MenuInventoryData.getHeadingIndex(uuid, menuType).get + PER_PAGE)
            player.openInventory(MenuInventoryData.computeTailPartCustomMenu(player))

          case _ =>
        }

      case MenuType.SHOP.invName =>
        mat match {
          //実績ポイント最新化
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
            val getPart = if (isHead) {
              num => Nicknames.getHeadPartFor(num)
            } else {
              num => Nicknames.getMiddlePartFor(num)
            }

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
            player.openInventory(MenuInventoryData.computeRefreshedCombineMenu(player))

          case _ if isSkull && isApplicableAsNextPageButton(current) =>
            clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
            val uuid = player.getUniqueId
            val menuType = MenuInventoryData.MenuType.SHOP
            MenuInventoryData.setHeadingIndex(uuid, menuType, MenuInventoryData.getHeadingIndex(uuid, menuType).get + PER_PAGE)
            player.openInventory(MenuInventoryData.computePartsShopMenu(player))

          case _ =>
        }

      // それ以外のインベントリの名前だった場合何もしない！
      case _ =>
    }
  }
}

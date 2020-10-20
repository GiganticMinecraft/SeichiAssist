package com.github.unchama.seichiassist.listener.invlistener

import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.data.MenuInventoryData.MenuType
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.ChatColor._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Material, Sound}

object OnClickTitleMenu {
  private final val MAX_LENGTH: Int = 8
  private final val prefix: String = s"$DARK_PURPLE$BOLD"
  private final val PER_PAGE: Int = ???

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.syncShift

  private def clickedSound(player: Player, sound: Sound, pitch: Float): Unit = player.playSound(player.getLocation, sound, 1f, pitch)

  private def isRightArrow(is: ItemStack): Boolean = is.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowRight"

  def onPlayerClickTitleMenuEvent(event: InventoryClickEvent)(implicit effectEnvironment: EffectEnvironment): Unit = {
    //外枠のクリック処理なら終了
    event.getClickedInventory.ifNull(return)

    //インベントリを開けたのがプレイヤーではない時終了
    val view = event.getView

    val he = view.getPlayer
    if (he.getType != EntityType.PLAYER) {
      return
    }

    //インベントリが存在しない時終了
    val topinventory = view.getTopInventory.ifNull(return)

    import com.github.unchama.util.InventoryUtil._

    //インベントリサイズが4列でない時終了
    if (topinventory.row != 4) {
      return
    }
    val current = event.getCurrentItem

    val player = he.asInstanceOf[Player]
    val pd = SeichiAssist.playermap(player.getUniqueId)

    val title = topinventory.getTitle
    //インベントリ名が以下の時処理
    val isSkull = current.getType == Material.SKULL_ITEM

    if (title == s"${prefix}二つ名組合せシステム") {
      event.setCancelled(true)

      //実績解除処理部分の読みこみ
      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      /*
       * クリックしたボタンに応じた各処理内容の記述ここから
       */
      current.getType match {
        //実績ポイント最新化
        case Material.EMERALD_ORE =>
          clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)
          pd.recalculateAchievePoint()
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))

        //エフェクトポイント→実績ポイント変換
        case Material.EMERALD =>
          clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)
          if (pd.effectPoint >= 10) {
            pd.convertEffectPointToAchievePoint()
          } else {
            player.sendMessage("エフェクトポイントが不足しています。")
          }
          //データ最新化
          pd.recalculateAchievePoint()

          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))

        //パーツショップ
        case Material.ITEM_FRAME =>
          clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
          player.openInventory(MenuInventoryData.setTitleShopData(player))

        //前パーツ
        case Material.WATER_BUCKET =>
          clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
          player.openInventory(MenuInventoryData.setFreeTitle1Data(player))

        //中パーツ
        case Material.MILK_BUCKET =>
          clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
          player.openInventory(MenuInventoryData.setFreeTitle2Data(player))

        //後パーツ
        case Material.LAVA_BUCKET =>
          clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
          player.openInventory(MenuInventoryData.setFreeTitle3Data(player))

        case _ if isSkull && isRightArrow(current) =>
          import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

          effectEnvironment.runAsyncTargetedEffect(player)(
            SequentialEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              StickMenu.firstPage.open
            ),
            "実績メニューを開く"
          )
      }
    } else if (title == s"${prefix}二つ名組合せ「前」") {
      event.setCancelled(true)
      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      current.getType match {
        case Material.WATER_BUCKET =>
          clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

          val id = current.getItemMeta.getDisplayName.toInt
          val length = Nicknames.getTitleFor(id,
            pd.settings.nickname.id2, pd.settings.nickname.id3).length
          if (length > MAX_LENGTH) {
            player.sendMessage(s"全パーツ合計で${MAX_LENGTH}文字以内になるよう設定してください。")
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
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))

        case _ if isSkull && isRightArrow(current) =>
          // 次ページ
          clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
          val uuid = player.getUniqueId
          val k: MenuType = MenuInventoryData.MenuType.HEAD
          MenuInventoryData.setHeadingIndex(uuid, k, MenuInventoryData.getHeadingIndex(uuid, k).get + PER_PAGE)
          player.openInventory(MenuInventoryData.setFreeTitle1Data(player))
      }
    } else if (title == s"${prefix}二つ名組合せ「中」") {
      event.setCancelled(true)
      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      current.getType match {
        case Material.MILK_BUCKET =>
          clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

          val id = current.getItemMeta.getDisplayName.toInt
          val length = Nicknames.getTitleFor(pd.settings.nickname.id1,
            id, pd.settings.nickname.id3).length
          if (length > MAX_LENGTH) {
            player.sendMessage(s"全パーツ合計で${MAX_LENGTH}文字以内になるよう設定してください。")
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
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))

        case _ if isSkull && isRightArrow(current) =>
          clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
          val uuid = player.getUniqueId
          val k: MenuType = MenuInventoryData.MenuType.MIDDLE
          MenuInventoryData.setHeadingIndex(uuid, k, MenuInventoryData.getHeadingIndex(uuid, k).get + PER_PAGE)
          player.openInventory(MenuInventoryData.setFreeTitle2Data(player))
      }
    } else if (title == s"${prefix}二つ名組合せ「後」") {
      event.setCancelled(true)

      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        //プレイヤーインベントリのクリックの場合終了
        return
      }

      current.getType match {
        case Material.LAVA_BUCKET =>
          clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

          val id = current.getItemMeta.getDisplayName.toInt
          val length = Nicknames.getTitleFor(pd.settings.nickname.id1,
            pd.settings.nickname.id2, id).length
          if (length > MAX_LENGTH) {
            player.sendMessage(s"全パーツ合計で${MAX_LENGTH}文字以内になるよう設定してください。")
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
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))

        case _ if isSkull && isRightArrow(current) =>
          clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
          val uuid = player.getUniqueId
          val k: MenuType = MenuInventoryData.MenuType.TAIL
          MenuInventoryData.setHeadingIndex(uuid, k, MenuInventoryData.getHeadingIndex(uuid, k).get + PER_PAGE)
          player.openInventory(MenuInventoryData.setFreeTitle3Data(player))
      }

    } else if (title == s"${prefix}実績ポイントショップ") {
      event.setCancelled(true)

      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      /*
       * クリックしたボタンに応じた各処理内容の記述ここから
       */

      current.getType match {
        //実績ポイント最新化
        case Material.EMERALD_ORE =>
          clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)
          pd.recalculateAchievePoint()
          pd.samepageflag = true
          player.openInventory(MenuInventoryData.setTitleShopData(player))

        // 購入処理
        case Material.BEDROCK =>
          clickedSound(player, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1.0f)

          val num = current.getItemMeta.getDisplayName.toInt
          val curpt = pd.achievePoint.left
          if (num < 9900) {
            val requiredPt = 20
            if (curpt >= requiredPt) {
              pd.TitleFlags.addOne(num)
              pd.consumeAchievePoint(requiredPt)
              player.sendMessage("パーツ「" + Nicknames.getHeadPartFor(num).getOrElse("") + "」を購入しました。")
              pd.samepageflag = true
              player.openInventory(MenuInventoryData.setTitleShopData(player))
            } else {
              player.sendMessage("実績ポイントが不足しています。")
            }
          } else {
            val requiredPt = 35
            if (curpt >= requiredPt) {
              pd.TitleFlags.addOne(num)
              pd.consumeAchievePoint(requiredPt)
              player.sendMessage("パーツ「" + Nicknames.getMiddlePartFor(num).getOrElse("") + "」を購入しました。")
              pd.samepageflag = true
              player.openInventory(MenuInventoryData.setTitleShopData(player))
            } else {
              player.sendMessage("実績ポイントが不足しています。")
            }
          }

        case Material.BARRIER =>
          clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))

        case _ if isSkull && isRightArrow(current) =>
          clickedSound(player, Sound.BLOCK_FENCE_GATE_OPEN, 0.1f)
          val uuid = player.getUniqueId
          val k: MenuType = MenuInventoryData.MenuType.SHOP
          MenuInventoryData.setHeadingIndex(uuid, k, MenuInventoryData.getHeadingIndex(uuid, k).get + PER_PAGE)
          player.openInventory(MenuInventoryData.setTitleShopData(player))
      }
    }
  }
}

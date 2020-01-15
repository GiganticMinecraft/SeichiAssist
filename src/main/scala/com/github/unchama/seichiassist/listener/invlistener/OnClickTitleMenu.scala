package com.github.unchama.seichiassist.listener.invlistener

import com.github.unchama.seichiassist
import com.github.unchama.seichiassist.achievement.Nicknames
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.seichiassist.{CommonSoundEffects, SeichiAssist}
import com.github.unchama.targetedeffect.sequentialEffect
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.ChatColor._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.Listener
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Material, Sound}

object OnClickTitleMenu extends Listener {
  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.sync

  def onPlayerClickTitleMenuEvent(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    //インベントリを開けたのがプレイヤーではない時終了
    val view = event.getView

    val he = view.getPlayer
    if (he.getType != EntityType.PLAYER) {
      return
    }

    //インベントリが存在しない時終了
    val topinventory = view.getTopInventory.ifNull {
      return
    }

    import com.github.unchama.util.InventoryUtil._

    //インベントリサイズが36でない時終了
    if (topinventory.row != 4) {
      return
    }
    val itemstackcurrent = event.getCurrentItem

    val player = he.asInstanceOf[Player]
    val playerdata = SeichiAssist.playermap(player.getUniqueId)

    val title = topinventory.getTitle
    //インベントリ名が以下の時処理
    val isSkull = itemstackcurrent.getType == Material.SKULL_ITEM
    val prefix = s"$DARK_PURPLE$BOLD"

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
      itemstackcurrent.getType match {
        //実績ポイント最新化
        case Material.EMERALD_ORE =>

          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.recalculateAchievePoint()
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))

        //エフェクトポイント→実績ポイント変換
        case Material.EMERALD =>
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          //不足してたらスルー
          if (playerdata.activeskilldata.effectpoint < 10) {
            player.sendMessage("エフェクトポイントが不足しています。")
          } else {
            playerdata.convertEffectPointToAchievePoint()
          }
          //データ最新化
          playerdata.recalculateAchievePoint()

          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))

        //パーツショップ
        case Material.ITEM_FRAME =>

          player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
          player.openInventory(MenuInventoryData.setTitleShopData(player))

        //前パーツ
        case Material.WATER_BUCKET =>

          player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
          player.openInventory(MenuInventoryData.setFreeTitle1Data(player))

        //中パーツ
        case Material.MILK_BUCKET =>

          player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
          player.openInventory(MenuInventoryData.setFreeTitle2Data(player))

        //後パーツ
        case Material.LAVA_BUCKET =>

          player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
          player.openInventory(MenuInventoryData.setFreeTitle3Data(player))
        case _ =>
      }
      if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowLeft") {
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

        seichiassist.unsafe.runAsyncTargetedEffect(player)(
          sequentialEffect(
            CommonSoundEffects.menuTransitionFenceSound,
            StickMenu.firstPage.open
          ),
          "実績メニューを開く"
        )
        return
      } //実績メニューに戻る

    } else if (title == s"${prefix}二つ名組合せ「前」") {
      event.setCancelled(true)

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
        // NOTE: WHEN
      } else if (itemstackcurrent.getType == Material.WATER_BUCKET) {
        val itemmeta = itemstackcurrent.getItemMeta
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

        val length = Nicknames.getTitleFor(Integer.parseInt(itemmeta.getDisplayName),
          playerdata.settings.nickname.id2, playerdata.settings.nickname.id3).length
        if (length < 9) {
          playerdata.updateNickname(id1 = Integer.parseInt(itemmeta.getDisplayName))
          player.sendMessage("前パーツ「" + Nicknames.getHeadPartFor(playerdata.settings.nickname.id1).getOrElse("") + "」をセットしました。")
        } else {
          player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。")
        }
      } else if (itemstackcurrent.getType == Material.GRASS) {
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.updateNickname(id1 = 0)
        player.sendMessage("前パーツの選択を解除しました。")
        return
      } else if (itemstackcurrent.getType == Material.BARRIER) {
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
        player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
        return
      } else if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowRight") {
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
        player.openInventory(MenuInventoryData.setFreeTitle1Data(player))
        return
      } //次ページ
      //組み合わせメイン
      //パーツ未選択に

    } else if (title == s"${prefix}二つ名組合せ「中」") {
      event.setCancelled(true)

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
        // NOTE: WHEN
      } else if (itemstackcurrent.getType == Material.MILK_BUCKET) {
        val itemmeta = itemstackcurrent.getItemMeta
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

        val length = Nicknames.getTitleFor(playerdata.settings.nickname.id1,
          Integer.parseInt(itemmeta.getDisplayName), playerdata.settings.nickname.id3).length
        if (length < 9) {
          playerdata.updateNickname(id2 = Integer.parseInt(itemmeta.getDisplayName))
          player.sendMessage("中パーツ「" + Nicknames.getMiddlePartFor(playerdata.settings.nickname.id2).getOrElse("") + "」をセットしました。")
        } else {
          player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。")
        }
      } else if (itemstackcurrent.getType == Material.GRASS) {
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.updateNickname(id2 = 0)
        player.sendMessage("中パーツの選択を解除しました。")
        return
      } else if (itemstackcurrent.getType == Material.BARRIER) {
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
        player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
        return
      } else if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowRight") {
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
        player.openInventory(MenuInventoryData.setFreeTitle2Data(player))
        return
      } //次ページ
      //組み合わせメインへ移動
      //パーツ未選択に

    } else if (title == s"${prefix}二つ名組合せ「後」") {
      event.setCancelled(true)

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      // NOTE: when
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      } else if (itemstackcurrent.getType == Material.LAVA_BUCKET) {
        val itemmeta = itemstackcurrent.getItemMeta
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

        val length = Nicknames.getTitleFor(playerdata.settings.nickname.id1,
          playerdata.settings.nickname.id2, Integer.parseInt(itemmeta.getDisplayName)).length
        if (length < 9) {
          playerdata.updateNickname(id3 = Integer.parseInt(itemmeta.getDisplayName))
          player.sendMessage("後パーツ「" + Nicknames.getTailPartFor(playerdata.settings.nickname.id3).getOrElse("") + "」をセットしました。")
        } else {
          player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。")
        }
      } else if (itemstackcurrent.getType == Material.GRASS) {
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.updateNickname(id3 = 0)
        player.sendMessage("後パーツの選択を解除しました。")
        return
      } else if (itemstackcurrent.getType == Material.BARRIER) {
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
        player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
        return
      } else if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowRight") {
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
        player.openInventory(MenuInventoryData.setFreeTitle3Data(player))
        return
      } //次ページ
      //組み合わせメイン
      //パーツ未選択に

    } else if (title == s"${prefix}実績ポイントショップ") {
      event.setCancelled(true)

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      /*
       * クリックしたボタンに応じた各処理内容の記述ここから
       */

      //実績ポイント最新化
      if (itemstackcurrent.getType == Material.EMERALD_ORE) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.recalculateAchievePoint()
        playerdata.samepageflag = true
        player.openInventory(MenuInventoryData.setTitleShopData(player))
      }

      //購入処理
      if (itemstackcurrent.getType == Material.BEDROCK) {
        val itemmeta = itemstackcurrent.getItemMeta
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

        if (Integer.parseInt(itemmeta.getDisplayName) < 9900) {
          if (playerdata.achievePoint.left < 20) {
            player.sendMessage("実績ポイントが不足しています。")
          } else {
            playerdata.TitleFlags.addOne(Integer.parseInt(itemmeta.getDisplayName))
            playerdata.consumeAchievePoint(20)
            player.sendMessage("パーツ「" + Nicknames.getHeadPartFor(Integer.parseInt(itemmeta.getDisplayName)).getOrElse("") + "」を購入しました。")
            playerdata.samepageflag = true
            player.openInventory(MenuInventoryData.setTitleShopData(player))
          }
        } else {
          if (playerdata.achievePoint.left < 35) {
            player.sendMessage("実績ポイントが不足しています。")
          } else {
            playerdata.TitleFlags.addOne(Integer.parseInt(itemmeta.getDisplayName))
            playerdata.consumeAchievePoint(35)
            player.sendMessage("パーツ「" + Nicknames.getMiddlePartFor(Integer.parseInt(itemmeta.getDisplayName)).getOrElse("") + "」を購入しました。")
            playerdata.samepageflag = true
            player.openInventory(MenuInventoryData.setTitleShopData(player))
          }
        }


      } else if (itemstackcurrent.getType == Material.BARRIER) {
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
        player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
        return
      } else if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowRight") {
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
        player.openInventory(MenuInventoryData.setTitleShopData(player))
        return
      } //次ページ
      //組み合わせメイン
    }
  }
}

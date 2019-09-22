package com.github.unchama.seichiassist.listener.invlistener

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.data.player.PlayerNickName
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import org.bukkit.ChatColor._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.Listener
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Material, Sound}

object OnClickTitleMenu extends Listener {
  def onPlayerClickTitleMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    //インベントリを開けたのがプレイヤーではない時終了
    val view = event.getView

    val he = view.player
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    //インベントリが存在しない時終了
    val topinventory = view.getTopInventory.ifNull { return }
    //インベントリサイズが36でない時終了
    if (topinventory.row != 4) {
      return
    }
    val itemstackcurrent = event.currentItem

    val player = he.asInstanceOf[Player]
    val playerdata = SeichiAssist.playermap[player.uniqueId]

    def setTitle(first: Int = 0, second: Int = 0, third: Int = 0, message: String =
        """二つ名$first「
          |${getTitle(1, first)}
          |${if (second != 0) getTitle(2, second) else ""}
          |${if (third != 0) getTitle(3, third) else ""}
          |」が設定されました。""".trimMargin().filter { it != '\n' }) {
      playerdata.updateNickname(first, second, third)
      player.sendMessage(message)
    }

    //経験値変更用のクラスを設定
    //ExperienceManager expman = new ExperienceManager(player);

    val title = topinventory.title
    //インベントリ名が以下の時処理
    val isSkull = itemstackcurrent.getType == Material.SKULL_ITEM
    val prefix = s"${DARK_PURPLE}${BOLD}"
    when (title) {
      s"${prefix}実績・二つ名システム" => {
        event.isCancelled = true

        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */
        //val isSkull = current === Material.SKULL_ITEM

        //表示内容をLVに変更
        if (itemstackcurrent.getType == Material.REDSTONE_TORCH_ON) {
          // Zero clear
          playerdata.updateNickname(style = PlayerNickName.Style.Level)
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.titleMenuData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_Present2") {
          SeichiAchievement.tryAchieve(player, playerdata.giveachvNo)
          playerdata.giveachvNo = 0
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.titleMenuData(player))
        } else if (itemstackcurrent.getType == Material.ANVIL) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
        } else if (itemstackcurrent.getType == Material.GOLD_PICKAXE) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.titleSeichi(player))
        } else if (itemstackcurrent.getType == Material.COMPASS) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.titleLogin(player))
        } else if (itemstackcurrent.getType == Material.BLAZE_POWDER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.titleSuperTry(player))
        } else if (itemstackcurrent.getType == Material.EYE_OF_ENDER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.titleSpecial(player))
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          GlobalScope.launch(Schedulers.async) {
            sequentialEffect(
                CommonSoundEffects.menuTransitionFenceSound,
                StickMenu.firstPage.open
            ).runFor(player)
          }
          return
        }//ホームメニューに戻る
        //カテゴリ「特殊」を開く
        //カテゴリ「やりこみ」を開く
        /*
        //カテゴリ「建築」を開く ※未実装
        else if(itemstackcurrent.getType().equals(Material.WOODEN_DOOR)){
          player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
          playerdata.titlepage = 1 ;
          player.openInventory(MenuInventoryData.titleBuild(player));
        }
        *///カテゴリ「ログイン」を開く
        //カテゴリ「整地」を開く
        //「二つ名組合せシステム」を開く
        //予約付与システム受け取り処理
      }

      s"${prefix}カテゴリ「整地」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        //クリックしたボタンに応じた各処理内容の記述ここから

        //実績「整地量」
        if (itemstackcurrent.getType === Material.IRON_PICKAXE) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleAmountData(player))
        }

        //実績「整地神ランキング」
        if (itemstackcurrent.getType === Material.DIAMOND_PICKAXE) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleRankData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleMenuData(player))
          return
        }//実績メニューに戻る
      }

      s"${prefix}カテゴリ「建築」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType == InventoryType.PLAYER) {
          return
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleMenuData(player))
          return
        }//クリックしたボタンに応じた各処理内容の記述ここから
        //実績未実装のカテゴリです。
        //実績メニューに戻る

      }

      s"${prefix}カテゴリ「ログイン」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
          // NOTE: WHEN
        } else if (itemstackcurrent.getType === Material.COMPASS) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.titleTimeData(player))
        } else if (itemstackcurrent.getType === Material.BOOK) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.titleJoinAmountData(player))
        } else if (itemstackcurrent.getType === Material.BOOK_AND_QUILL) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.titleJoinChainData(player))
        } else if (itemstackcurrent.getType === Material.NETHER_STAR) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.titleExtraData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleMenuData(player))
          return
        }//実績メニューに戻る
        //実績「記念日」を開く
        //実績「連続ログイン」を開く
        //実績「通算ログイン」を開く
        //クリックしたボタンに応じた各処理内容の記述ここから
        //実績「参加時間」を開く

      }

      s"${prefix}カテゴリ「やりこみ」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleMenuData(player))
          return
        }//クリックしたボタンに応じた各処理内容の記述ここから
        //実績未実装のカテゴリです。
        //実績メニューに戻る

      }

      s"${prefix}カテゴリ「特殊」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        } else if (itemstackcurrent.getType === Material.BLAZE_POWDER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.titleEventData(player))
        } else if (itemstackcurrent.getType === Material.YELLOW_FLOWER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.titleSupportData(player))
        } else if (itemstackcurrent.getType === Material.DIAMOND_BARDING) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.titleSecretData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleMenuData(player))
          return
        }//実績メニューに戻る
        //実績「極秘任務」を開く
        //実績「JMS投票数」を開く
        //クリックしたボタンに応じた各処理内容の記述ここから
        //実績「公式イベント」を開く

      }

      s"${prefix}二つ名組合せシステム" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */
        when (itemstackcurrent.type) {
          //実績ポイント最新化
          Material.EMERALD_ORE => {

            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
            playerdata.recalculateAchievePoint()
            player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
          }

          //エフェクトポイント→実績ポイント変換
          Material.EMERALD => {
            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
            //不足してたらスルー
            if (playerdata.activeskilldata.effectpoint < 10) {
              player.sendMessage("エフェクトポイントが不足しています。")
            } else {
              playerdata.convertEffectPointToAchievePoint()
            }
            //データ最新化
            playerdata.recalculateAchievePoint()

            player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
          }

          //パーツショップ
          Material.ITEM_FRAME => {

            player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
            player.openInventory(MenuInventoryData.setTitleShopData(player))
          }

          //前パーツ
          Material.WATER_BUCKET => {

            player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
            player.openInventory(MenuInventoryData.setFreeTitle1Data(player))
          }

          //中パーツ
          Material.MILK_BUCKET => {

            player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
            player.openInventory(MenuInventoryData.setFreeTitle2Data(player))
          }

          //後パーツ
          Material.LAVA_BUCKET => {

            player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
            player.openInventory(MenuInventoryData.setFreeTitle3Data(player))}
          else => {
            // NOP
          }
        }
        if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleMenuData(player))
          return
        }//実績メニューに戻る

      }

      s"${prefix}二つ名組合せ「前」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
          // NOTE: WHEN
        } else if (itemstackcurrent.getType === Material.WATER_BUCKET) {
          val itemmeta = itemstackcurrent.itemMeta
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

          val forcheck = (SeichiAssist.seichiAssistConfig.getTitle1(Integer.parseInt(itemmeta.displayName))
              + SeichiAssist.seichiAssistConfig.getTitle2(playerdata.settings.nickName.id2)
              + SeichiAssist.seichiAssistConfig.getTitle3(playerdata.settings.nickName.id3))
          if (forcheck.length < 9) {
            playerdata.updateNickname(id1 = Integer.parseInt(itemmeta.displayName))
            player.sendMessage("前パーツ「" + SeichiAssist.seichiAssistConfig.getTitle1(playerdata.settings.nickName.id1) + "」をセットしました。")
          } else {
            player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。")
          }
        } else if (itemstackcurrent.getType === Material.GRASS) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.updateNickname(id1 = 0)
          player.sendMessage("前パーツの選択を解除しました。")
          return
        } else if (itemstackcurrent.getType === Material.BARRIER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
          return
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowRight") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitle1Data(player))
          return
        }//次ページ
        //組み合わせメイン
        //パーツ未選択に


      }

      s"${prefix}二つ名組合せ「中」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
          // NOTE: WHEN
        } else if (itemstackcurrent.getType === Material.MILK_BUCKET) {
          val itemmeta = itemstackcurrent.itemMeta
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

          val forcheck = (SeichiAssist.seichiAssistConfig.getTitle1(playerdata.settings.nickName.id1)
              + SeichiAssist.seichiAssistConfig.getTitle2(Integer.parseInt(itemmeta.displayName))
              + SeichiAssist.seichiAssistConfig.getTitle3(playerdata.settings.nickName.id3))
          if (forcheck.length < 9) {
            playerdata.updateNickname(id2 = Integer.parseInt(itemmeta.displayName))
            player.sendMessage("中パーツ「" + SeichiAssist.seichiAssistConfig.getTitle2(playerdata.settings.nickName.id2) + "」をセットしました。")
          } else {
            player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。")
          }
        } else if (itemstackcurrent.getType === Material.GRASS) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.updateNickname(id2 = 0)
          player.sendMessage("中パーツの選択を解除しました。")
          return
        } else if (itemstackcurrent.getType === Material.BARRIER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
          return
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowRight") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitle2Data(player))
          return
        }//次ページ
        //組み合わせメインへ移動
        //パーツ未選択に


      }

      s"${prefix}二つ名組合せ「後」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        // NOTE: when
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        } else if (itemstackcurrent.getType === Material.LAVA_BUCKET) {
          val itemmeta = itemstackcurrent.itemMeta
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

          val forcheck = (SeichiAssist.seichiAssistConfig.getTitle1(playerdata.settings.nickName.id1)
              + SeichiAssist.seichiAssistConfig.getTitle2(playerdata.settings.nickName.id2)
              + SeichiAssist.seichiAssistConfig.getTitle3(Integer.parseInt(itemmeta.displayName)))
          if (forcheck.length < 9) {
            playerdata.updateNickname(id3 = Integer.parseInt(itemmeta.displayName))
            player.sendMessage("後パーツ「" + SeichiAssist.seichiAssistConfig.getTitle3(playerdata.settings.nickName.id3) + "」をセットしました。")
          } else {
            player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。")
          }
        } else if (itemstackcurrent.getType === Material.GRASS) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.updateNickname(id3 = 0)
          player.sendMessage("後パーツの選択を解除しました。")
          return
        } else if (itemstackcurrent.getType === Material.BARRIER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
          return
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowRight") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitle3Data(player))
          return
        }//次ページ
        //組み合わせメイン
        //パーツ未選択に


      }

      s"${prefix}実績ポイントショップ" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        //実績ポイント最新化
        if (itemstackcurrent.getType === Material.EMERALD_ORE) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.recalculateAchievePoint()
          playerdata.samepageflag = true
          player.openInventory(MenuInventoryData.setTitleShopData(player))
        }

        //購入処理
        if (itemstackcurrent.getType === Material.BEDROCK) {
          val itemmeta = itemstackcurrent.itemMeta
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

          if (Integer.parseInt(itemmeta.displayName) < 9900) {
            if (playerdata.achievePoint.left < 20) {
              player.sendMessage("実績ポイントが不足しています。")
            } else {
              playerdata.TitleFlags.set(Integer.parseInt(itemmeta.displayName))
              playerdata.consumeAchievePoint(20)
              player.sendMessage("パーツ「" + SeichiAssist.seichiAssistConfig.getTitle1(Integer.parseInt(itemmeta.displayName)) + "」を購入しました。")
              playerdata.samepageflag = true
              player.openInventory(MenuInventoryData.setTitleShopData(player))
            }
          } else {
            if (playerdata.achievePoint.left < 35) {
              player.sendMessage("実績ポイントが不足しています。")
            } else {
              playerdata.TitleFlags.set(Integer.parseInt(itemmeta.displayName))
              playerdata.consumeAchievePoint(35)
              player.sendMessage("パーツ「" + SeichiAssist.seichiAssistConfig.getTitle2(Integer.parseInt(itemmeta.displayName)) + "」を購入しました。")
              playerdata.samepageflag = true
              player.openInventory(MenuInventoryData.setTitleShopData(player))
            }
          }


        } else if (itemstackcurrent.getType === Material.BARRIER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
          return
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowRight") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setTitleShopData(player))
          return
        }//次ページ
        //組み合わせメイン

      }

      s"${prefix}実績「整地神ランキング」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.getType === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.titleRankData(player))
        } else if (itemstackcurrent.getType === Material.DIAMOND_BLOCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemstackcurrent.itemMeta.displayName
          when {
            "No1001「" + SeichiAssist.seichiAssistConfig.getTitle1(1001) + "」" in name => setTitle(first = 1001)
            "No1002「" + SeichiAssist.seichiAssistConfig.getTitle1(1002) + "」" in name => setTitle(first = 1002)
            "No1003「" + SeichiAssist.seichiAssistConfig.getTitle1(1003) + "」" in name => setTitle(first = 1003)
            "No1004「" + SeichiAssist.seichiAssistConfig.getTitle1(1004) + "」" in name => setTitle(first = 1004)
            "No1010「" + SeichiAssist.seichiAssistConfig.getTitle1(1010) + "」" in name => setTitle(first = 1010)
            ("No1011「" + SeichiAssist.seichiAssistConfig.getTitle1(1011)
                + SeichiAssist.seichiAssistConfig.getTitle2(9904) + SeichiAssist.seichiAssistConfig.getTitle3(1011) + "」") in name => {
              setTitle(1011, 9904, 1011)
            }
            ("No1012「" + SeichiAssist.seichiAssistConfig.getTitle1(1012)
                + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(1012) + "」") in name => {
              setTitle(1012, 9901, 1012)
            }
            ("No1005「" + SeichiAssist.seichiAssistConfig.getTitle1(1005)
                + SeichiAssist.seichiAssistConfig.getTitle3(1005) + "」") in name => {
              setTitle(first = 1005, third = 1012)
            }
            ("No1006「" + SeichiAssist.seichiAssistConfig.getTitle1(1006) + "」") in name => setTitle(first = 1006)
            ("No1007「" + SeichiAssist.seichiAssistConfig.getTitle1(1007)
                + SeichiAssist.seichiAssistConfig.getTitle2(9904) + SeichiAssist.seichiAssistConfig.getTitle3(1007) + "」") in name => {
              setTitle(1007, 9904, 1007)
            }
            ("No1008「" + SeichiAssist.seichiAssistConfig.getTitle1(1008)
                + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(1008) + "」") in name => {
              setTitle(1008, 9901, 1008)
            }
            ("No1009「" + SeichiAssist.seichiAssistConfig.getTitle1(1009)
                + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(1009) + "」") in name => {
              setTitle(1009, 9909, 1009)
            }
          }
          player.openInventory(MenuInventoryData.titleRankData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleSeichi(player))
          return
        }//実績メニューに戻る
      }

      s"${prefix}実績「整地量」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.getType === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.titleAmountData(player))
        } else if (itemstackcurrent.getType === Material.DIAMOND_BLOCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemstackcurrent.itemMeta.displayName
          when {
            "No3001「" + SeichiAssist.seichiAssistConfig.getTitle1(3001) + "」" in name => setTitle(first = 3001)
            ("No3002「" + SeichiAssist.seichiAssistConfig.getTitle1(3002)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(3002) + "」") in name => setTitle(3002, 9905, 3002)
            "No3003「" + SeichiAssist.seichiAssistConfig.getTitle1(3003) + "」" in name => setTitle(first = 3003)
            ("No3004「" + SeichiAssist.seichiAssistConfig.getTitle1(3004)
                + SeichiAssist.seichiAssistConfig.getTitle2(9902) + "」") in name => setTitle(first = 3004, second = 9902)
            ("No3005「" + SeichiAssist.seichiAssistConfig.getTitle1(3005)
                + SeichiAssist.seichiAssistConfig.getTitle3(3005) + "」") in name => setTitle(first = 3005, third = 3005)
            "No3006「" + SeichiAssist.seichiAssistConfig.getTitle1(3006) + "」" in name => setTitle(first = 3006)
            ("No3007「" + SeichiAssist.seichiAssistConfig.getTitle1(3007)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」") in name => setTitle(first = 3007, second = 9905)
            "No3008「" + SeichiAssist.seichiAssistConfig.getTitle1(3008) + "」" in name => setTitle(first = 3008)
            ("No3009「" + SeichiAssist.seichiAssistConfig.getTitle1(3009)
                + SeichiAssist.seichiAssistConfig.getTitle3(3009) + "」") in name => setTitle(first = 3009, third = 3009)
            ("No3010「" + SeichiAssist.seichiAssistConfig.getTitle1(3010)
                + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(3010) + "」") in name => setTitle(3010, 9909, 3010)
            "No3011「" + SeichiAssist.seichiAssistConfig.getTitle1(3011) + "」" in name => setTitle(first = 3011)
            ("No3012「" + SeichiAssist.seichiAssistConfig.getTitle1(3012)
                + SeichiAssist.seichiAssistConfig.getTitle3(3012) + "」") in name => setTitle(first = 3012, third = 3012)
            ("No3013「" + SeichiAssist.seichiAssistConfig.getTitle1(3013)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(3013) + "」") in name => setTitle(3013, 9905, 3013)
            ("No3014「" + SeichiAssist.seichiAssistConfig.getTitle1(3014)
                + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(3014) + "」") in name => setTitle(3014, 9909, 3014)
            "No3015「" + SeichiAssist.seichiAssistConfig.getTitle1(3015) + "」" in name => setTitle(first = 3015)
            "No3016「" + SeichiAssist.seichiAssistConfig.getTitle1(3016) + "」" in name => setTitle(first = 3016)
            "No3017「" + SeichiAssist.seichiAssistConfig.getTitle1(3017) + "」" in name => setTitle(first = 3017)
            "No3018「" + SeichiAssist.seichiAssistConfig.getTitle1(3018) + "」" in name => setTitle(first = 3018)
            "No3019「" + SeichiAssist.seichiAssistConfig.getTitle1(3019) + "」" in name => setTitle(first = 3019)
          }
          player.openInventory(MenuInventoryData.titleAmountData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleSeichi(player))
          return
        }//実績メニューに戻る
      }

      s"${prefix}実績「参加時間」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.getType === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.titleTimeData(player))
        } else if (itemstackcurrent.getType === Material.DIAMOND_BLOCK) {
          val itemmeta = itemstackcurrent.itemMeta

          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemmeta.displayName
          if (name.contains("No4001「" + SeichiAssist.seichiAssistConfig.getTitle1(4001)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4001) + "」")) {
            setTitle(4001, 9905, 4001)
          } else if (name.contains("No4002「" + SeichiAssist.seichiAssistConfig.getTitle1(4002)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4002) + "」")) {
            setTitle(first = 4002, third = 4002)
          } else if (name.contains("No4003「" + SeichiAssist.seichiAssistConfig.getTitle1(4003)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4003) + "」")) {
            setTitle(first = 4003, third = 4003)
            player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4003)
                + SeichiAssist.seichiAssistConfig.getTitle3(4003) + "」が設定されました。")
          } else if (name.contains("No4004「" + SeichiAssist.seichiAssistConfig.getTitle1(4004)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4004) + "」")) {
            setTitle(4004, 9905, 4004)
          } else if (name.contains("No4005「" + SeichiAssist.seichiAssistConfig.getTitle1(4005)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4005) + "」")) {
            setTitle(first = 4005, third = 4005)
          } else if (name.contains("No4006「" + SeichiAssist.seichiAssistConfig.getTitle1(4006)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4006) + "」")) {
            setTitle(4006, 9905, 4006)
          } else if (name.contains("No4007「" + SeichiAssist.seichiAssistConfig.getTitle1(4007)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4007) + "」")) {
            setTitle(first = 4007, third = 4007)
          } else if (name.contains("No4008「" + SeichiAssist.seichiAssistConfig.getTitle1(4008)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4008) + "」")) {
            setTitle(first = 4008, third = 4008)
          } else if (name.contains("No4009「" + SeichiAssist.seichiAssistConfig.getTitle1(4009)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4009) + "」")) {
            setTitle(first = 4009, third = 4009)
          } else if (name.contains("No4010「" + SeichiAssist.seichiAssistConfig.getTitle1(4010)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4010) + "」")) {
            setTitle(4010, 9905, 4010)
          } else if (name.contains("No4011「" + SeichiAssist.seichiAssistConfig.getTitle1(4011)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(4011) + "」")) {
            setTitle(4011, 9901, 4011)
          } else if (name.contains("No4012「" + SeichiAssist.seichiAssistConfig.getTitle1(4012)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4012) + "」")) {
            setTitle(4012, 0, 4012)
          } else if (name.contains("No4013「" + SeichiAssist.seichiAssistConfig.getTitle1(4013)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4013) + "」")) {
            setTitle(4013, 0, 4013)
          } else if (name.contains("No4014「" + SeichiAssist.seichiAssistConfig.getTitle1(4014)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4014) + "」")) {
            setTitle(4014, 9905, 4014)
          } else if (name.contains("No4015「" + SeichiAssist.seichiAssistConfig.getTitle1(4015) + "」")) {
            setTitle(first = 4015)
            player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4015) + "」が設定されました。")
          } else if (name.contains("No4016「" + SeichiAssist.seichiAssistConfig.getTitle1(4016)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4016) + "」")) {
            setTitle(first = 4016, third = 4016)
          } else if (name.contains("No4017「" + SeichiAssist.seichiAssistConfig.getTitle1(4017) + "」")) {
            setTitle(first = 4017)
          } else if (name.contains("No4018「" + SeichiAssist.seichiAssistConfig.getTitle1(4018)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4018) + "」")) {
            setTitle(first = 4018, third = 4018)
          } else if (name.contains("No4019「" + SeichiAssist.seichiAssistConfig.getTitle1(4019)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4019) + "」")) {
            setTitle(first = 4019, third = 4019)
          } else if (name.contains("No4020「" + SeichiAssist.seichiAssistConfig.getTitle1(4020)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4020) + "」")) {
            setTitle(first = 4020, third = 4020)
          } else if (name.contains("No4021「" + SeichiAssist.seichiAssistConfig.getTitle1(4021)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4021) + "」")) {
            setTitle(first = 4021, third = 4021)
            player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4021)
                + SeichiAssist.seichiAssistConfig.getTitle3(4021) + "」が設定されました。")
          } else if (name.contains("No4022「" + SeichiAssist.seichiAssistConfig.getTitle1(4022)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(4022) + "」")) {
            setTitle(4022, 9903, 4022)
          } else if (name.contains("No4023「" + SeichiAssist.seichiAssistConfig.getTitle1(4023)
                  + SeichiAssist.seichiAssistConfig.getTitle3(4023) + "」")) {
            setTitle(first = 4023, third = 4023)
          }
          player.openInventory(MenuInventoryData.titleTimeData(player))
        } else if (itemstackcurrent.getType === Material.EMERALD_BLOCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.TitleFlags.set(8003)
          player.sendMessage("お疲れ様でした！今日のお給料の代わりに二つ名をどうぞ！")
          player.openInventory(MenuInventoryData.titleTimeData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleLogin(player))
          return
        }//実績メニューに戻る
      }

      s"${prefix}実績「通算ログイン」" => {
        event.isCancelled = true

        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }
        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.getType === Material.BEDROCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.titleJoinAmountData(player))
        } else if (itemstackcurrent.getType === Material.DIAMOND_BLOCK) {
          val itemmeta = itemstackcurrent.itemMeta
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemmeta.displayName
          if (name.contains("No5101「" + SeichiAssist.seichiAssistConfig.getTitle1(5101)
                  + SeichiAssist.seichiAssistConfig.getTitle3(5101) + "」")) {
            setTitle(first = 5101, third = 5101)
          } else if (name.contains("No5102「" + SeichiAssist.seichiAssistConfig.getTitle1(5102)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9907) + SeichiAssist.seichiAssistConfig.getTitle3(5102) + "」")) {
            setTitle(5102, 9907, 5102)
          } else if (name.contains("No5103「" + SeichiAssist.seichiAssistConfig.getTitle1(5103)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」")) {
            setTitle(first = 5103, second = 9905)
          } else if (name.contains("No5104「" + SeichiAssist.seichiAssistConfig.getTitle1(5104)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5104) + "」")) {
            setTitle(5104, 9905, 5104)
          } else if (name.contains("No5105「" + SeichiAssist.seichiAssistConfig.getTitle1(5105)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9907) + SeichiAssist.seichiAssistConfig.getTitle3(5105) + "」")) {
            setTitle(5105, 9907, 5105)
          } else if (name.contains("No5106「" + SeichiAssist.seichiAssistConfig.getTitle1(5106) + "」")) {
            setTitle(first = 5105)
          } else if (name.contains("No5107「" + SeichiAssist.seichiAssistConfig.getTitle1(5107)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(5107) + "」")) {
            setTitle(5107, 9909, 5107)
          } else if (name.contains("No5108「" + SeichiAssist.seichiAssistConfig.getTitle1(5108)
                  + SeichiAssist.seichiAssistConfig.getTitle3(5108) + "」")) {
            setTitle(first = 5108)
          } else if (name.contains("No5109「" + SeichiAssist.seichiAssistConfig.getTitle1(5109) + "」")) {
            setTitle(first = 5109)
          } else if (name.contains("No5110「" + SeichiAssist.seichiAssistConfig.getTitle1(5110) + "」")) {
            setTitle(first = 5110)
          } else if (name.contains("No5111「" + SeichiAssist.seichiAssistConfig.getTitle1(5111) + "」")) {
            setTitle(first = 5111)
          } else if (name.contains("No5112「" + SeichiAssist.seichiAssistConfig.getTitle1(5112)
                  + SeichiAssist.seichiAssistConfig.getTitle3(5112) + "」")) {
            setTitle(first = 5112, third = 5112)
          } else if (name.contains("No5113「" + SeichiAssist.seichiAssistConfig.getTitle1(5113)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5113) + "」")) {
            setTitle(5113, 9905, 5113)
          } else if (name.contains("No5114「" + SeichiAssist.seichiAssistConfig.getTitle1(5114)
                  + SeichiAssist.seichiAssistConfig.getTitle3(5114) + "」")) {
            setTitle(first = 5114, third = 5114)
          } else if (name.contains("No5115「" + SeichiAssist.seichiAssistConfig.getTitle1(5115) + "」")) {
            setTitle(first = 5115)
          } else if (name.contains("No5116「" + SeichiAssist.seichiAssistConfig.getTitle1(5116)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5116) + "」")) {
            setTitle(5116, 9905, 5116)
          } else if (name.contains("No5117「" + SeichiAssist.seichiAssistConfig.getTitle1(5117)
                  + SeichiAssist.seichiAssistConfig.getTitle3(5117) + "」")) {
            setTitle(first = 5117, third = 5117)
          } else if (name.contains("No5118「" + SeichiAssist.seichiAssistConfig.getTitle1(5118)
                  + SeichiAssist.seichiAssistConfig.getTitle3(5118) + "」")) {
            setTitle(first = 5118, third = 5118)
          } else if (name.contains("No5119「" + SeichiAssist.seichiAssistConfig.getTitle1(5119)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5119) + "」")) {
            setTitle(5119, 9905, 5119)
          } else if (name.contains("No5120「" + SeichiAssist.seichiAssistConfig.getTitle1(5120)
                  + SeichiAssist.seichiAssistConfig.getTitle2(5120) + SeichiAssist.seichiAssistConfig.getTitle3(5120) + "」")) {
            setTitle(5120, 5120, 5120)
          }

          player.openInventory(MenuInventoryData.titleJoinAmountData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleLogin(player))
          return
        }//実績メニューに戻る
      }

      s"${prefix}実績「連続ログイン」" => {
        event.isCancelled = true

        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.getType === Material.BEDROCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.titleJoinChainData(player))
        } else if (itemstackcurrent.getType === Material.DIAMOND_BLOCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemstackcurrent.itemMeta.displayName

          when {
            ("No5001「" + SeichiAssist.seichiAssistConfig.getTitle1(5001)
                + SeichiAssist.seichiAssistConfig.getTitle2(5001) + "」") in name => setTitle(first = 5001, second = 5001)
            ("No5002「" + SeichiAssist.seichiAssistConfig.getTitle1(5002)
                + SeichiAssist.seichiAssistConfig.getTitle3(5002) + "」") in name => setTitle(first = 5002, third = 5002)
            "No5003「" + SeichiAssist.seichiAssistConfig.getTitle1(5003) + "」" in name => setTitle(first = 5003)
            ("No5004「" + SeichiAssist.seichiAssistConfig.getTitle1(5004)
                + SeichiAssist.seichiAssistConfig.getTitle3(5004) + "」") in name => setTitle(first = 5004, third = 5004)
            ("No5005「" + SeichiAssist.seichiAssistConfig.getTitle1(5005)
                + SeichiAssist.seichiAssistConfig.getTitle3(5005) + "」") in name => setTitle(first = 5005, third = 5005)
            ("No5006「" + SeichiAssist.seichiAssistConfig.getTitle1(5006)
                + SeichiAssist.seichiAssistConfig.getTitle3(5006) + "」") in name => setTitle(first = 5006, third = 5006)
            "No5007「" + SeichiAssist.seichiAssistConfig.getTitle1(5007) + "」" in name => setTitle(first = 5007)
            ("No5008「" + SeichiAssist.seichiAssistConfig.getTitle1(5008)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」") in name => setTitle(first = 5008, second = 9905)
          }

          player.openInventory(MenuInventoryData.titleJoinChainData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f)
          player.openInventory(MenuInventoryData.titleLogin(player))
          return
        }//実績メニューに戻る
      }

      s"${prefix}実績「JMS投票数」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.getType === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.titleSupportData(player))
        } else if (itemstackcurrent.getType === Material.DIAMOND_BLOCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemstackcurrent.itemMeta.displayName

          if (name.contains("No6001「" + SeichiAssist.seichiAssistConfig.getTitle1(6001) + "」")) {
            setTitle(first = 6001)
          } else if (name.contains("No6002「" + SeichiAssist.seichiAssistConfig.getTitle1(6002)
                  + SeichiAssist.seichiAssistConfig.getTitle3(6002) + "」")) {
            setTitle(first = 6002, third = 6002)
          } else if (name.contains("No6003「" + SeichiAssist.seichiAssistConfig.getTitle1(6003) + "」")) {
            setTitle(first = 6003)
          } else if (name.contains("No6004「" + SeichiAssist.seichiAssistConfig.getTitle1(6004)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(6004) + "」")) {
            setTitle(first = 6004, second = 9903, third = 6004)
          } else if (name.contains("No6005「" + SeichiAssist.seichiAssistConfig.getTitle1(6005)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」")) {
            setTitle(first = 6005, second = 9905)
          } else if (name.contains("No6006「" + SeichiAssist.seichiAssistConfig.getTitle1(6006)
                  + SeichiAssist.seichiAssistConfig.getTitle3(6006) + "」")) {
            setTitle(first = 6006, third = 6006)
          } else if (name.contains("No6007「" + SeichiAssist.seichiAssistConfig.getTitle1(6007)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9902) + "」")) {
            setTitle(first = 6007, second = 9902)
          } else if (name.contains("No6008「" + SeichiAssist.seichiAssistConfig.getTitle1(6008) + "」")) {
            setTitle(first = 6008)
          }
          player.openInventory(MenuInventoryData.titleSupportData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleSpecial(player))
          return
        }//実績メニューに戻る
      }

      s"${prefix}実績「公式イベント」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.getType === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は配布解禁式です。運営チームからの配布タイミングを逃さないようご注意ください。")
          player.openInventory(MenuInventoryData.titleEventData(player))
        } else if (itemstackcurrent.getType === Material.DIAMOND_BLOCK) {
          val itemmeta = itemstackcurrent.itemMeta
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemmeta.displayName

          if (name.contains("No7001「" + SeichiAssist.seichiAssistConfig.getTitle1(7001)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(7001) + "」")) {
            setTitle(7001, 9901, 7001)
          } else if (name.contains("No7002「" + SeichiAssist.seichiAssistConfig.getTitle1(7002)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7002) + "」")) {
            setTitle(7002, 9905, 7002)
          } else if (name.contains("No7003「" + SeichiAssist.seichiAssistConfig.getTitle1(7003)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7003) + "」")) {
            setTitle(7003, 9905, 7003)
          } else if (name.contains("No7004「" + SeichiAssist.seichiAssistConfig.getTitle2(7004) + "」")) {
            setTitle(second = 7704)
          } else if (name.contains("No7005「" + SeichiAssist.seichiAssistConfig.getTitle1(7005)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9902) + SeichiAssist.seichiAssistConfig.getTitle3(7005) + "」")) {
            setTitle(7005, 9902, 7005)
          } else if (name.contains("No7006「" + SeichiAssist.seichiAssistConfig.getTitle1(7006)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7006) + "」")) {
            setTitle(7006, 9905, 7006)
          } else if (name.contains("No7007「" + SeichiAssist.seichiAssistConfig.getTitle1(7007)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7007) + "」")) {
            setTitle(7007, 9905, 7007)
          } else if (name.contains("No7008「" + SeichiAssist.seichiAssistConfig.getTitle1(7008)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7008) + "」")) {
            setTitle(7008, 9905, 7008)
          } else if (name.contains("No7009「" + SeichiAssist.seichiAssistConfig.getTitle1(7009)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7009) + "」")) {
            setTitle(7009, 9905, 7009)
          } else if (name.contains("No7010「" + SeichiAssist.seichiAssistConfig.getTitle1(7010)
                  + SeichiAssist.seichiAssistConfig.getTitle3(7010) + "」")) {
            setTitle(first = 7010, third = 7010)
          } else if (name.contains("No7011「" + SeichiAssist.seichiAssistConfig.getTitle1(7011)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7011) + "」")) {
            setTitle(7011, 9905, 7011)
          } else if (name.contains("No7012「" + SeichiAssist.seichiAssistConfig.getTitle1(7012)
                  + SeichiAssist.seichiAssistConfig.getTitle3(7012) + "」")) {
            setTitle(first = 7012, third = 7012)
          } else if (name.contains("No7013「" + SeichiAssist.seichiAssistConfig.getTitle1(7013) + "」")) {
            setTitle(first = 7013)
          } else if (name.contains("No7014「" + SeichiAssist.seichiAssistConfig.getTitle1(7014) + "」")) {
            setTitle(first = 7014)
          } else if (name.contains("No7015「" + SeichiAssist.seichiAssistConfig.getTitle1(7015)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9904) + SeichiAssist.seichiAssistConfig.getTitle3(7015) + "」")) {
            setTitle(7015, 9904, 7015)
          } else if (name.contains("No7016「" + SeichiAssist.seichiAssistConfig.getTitle1(7016)
                  + SeichiAssist.seichiAssistConfig.getTitle3(7016) + "」")) {
            setTitle(first = 7016, third = 7016)
          } else if (name.contains("No7017「" + SeichiAssist.seichiAssistConfig.getTitle1(7017)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7017) + "」")) {
            setTitle(7017, 9905, 7017)
          } else if (name.contains("No7018「" + SeichiAssist.seichiAssistConfig.getTitle1(7018)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9904) + SeichiAssist.seichiAssistConfig.getTitle3(7018) + "」")) {
            setTitle(7018, 9904, 7018)
          } else if (name.contains("No7019「" + SeichiAssist.seichiAssistConfig.getTitle1(7019)
                  + SeichiAssist.seichiAssistConfig.getTitle3(7019) + "」")) {
            setTitle(first = 7019, third = 7019)
          } else if (name.contains("No7020「" + SeichiAssist.seichiAssistConfig.getTitle1(7020)
                  + SeichiAssist.seichiAssistConfig.getTitle3(7020) + "」")) {
            setTitle(first = 7020, third = 7020)
          } else if (name.contains("No7021「" + SeichiAssist.seichiAssistConfig.getTitle1(7021)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7021) + "」")) {
            setTitle(7021, 9905, 7021)
          } else if (name.contains("No7022「" + SeichiAssist.seichiAssistConfig.getTitle1(7022)
                  + SeichiAssist.seichiAssistConfig.getTitle3(7022) + "」")) {
            setTitle(first = 7022, third = 7022)
          } else if (name.contains("No7023「" + SeichiAssist.seichiAssistConfig.getTitle1(7023)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7023) + "」")) {
            setTitle(7023, 9905, 7023)
          } else if (name.contains("No7024「" + SeichiAssist.seichiAssistConfig.getTitle1(7024)
                  + SeichiAssist.seichiAssistConfig.getTitle3(7024) + "」")) {
            setTitle(first = 7024, third = 7024)
            player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7024)
                + SeichiAssist.seichiAssistConfig.getTitle3(7024) + "」が設定されました。")
          } else if (name.contains("No7025「" + SeichiAssist.seichiAssistConfig.getTitle1(7025)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7025) + "」")) {
            setTitle(7025, 9905, 7025)
          } else if (name.contains("No7026「" + SeichiAssist.seichiAssistConfig.getTitle1(7026)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7026) + "」")) {
            setTitle(7026, 9905, 7026)
          } else if (name.contains("No7027「" + SeichiAssist.seichiAssistConfig.getTitle1(7027)
                  + SeichiAssist.seichiAssistConfig.getTitle3(7027) + "」")) {
            player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7027)
                + SeichiAssist.seichiAssistConfig.getTitle3(7027) + "」が設定されました。")
          } else if (name.contains("No7901「" + SeichiAssist.seichiAssistConfig.getTitle1(7901)
                  + SeichiAssist.seichiAssistConfig.getTitle2(7901) + SeichiAssist.seichiAssistConfig.getTitle3(7901) + "」")) {
            setTitle(7901, 7901, 7901)
          } else if (name.contains("No7902「" + SeichiAssist.seichiAssistConfig.getTitle1(7902)
                  + SeichiAssist.seichiAssistConfig.getTitle3(7902) + "」")) {
            setTitle(first = 7902, third = 7902)
          } else if (name.contains("No7903「" + SeichiAssist.seichiAssistConfig.getTitle1(7903)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7903) + "」")) {
            setTitle(7903, 9905, 7903)
          } else if (name.contains("No7904「" + SeichiAssist.seichiAssistConfig.getTitle1(7904)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9907) + SeichiAssist.seichiAssistConfig.getTitle3(7904) + "」")) {
            setTitle(7904, 9907, 7904)
          } else if (name.contains("No7905「" + SeichiAssist.seichiAssistConfig.getTitle1(7905)
                  + SeichiAssist.seichiAssistConfig.getTitle3(7905) + "」")) {
            setTitle(first = 7905, third = 7905)
          } else if (name.contains("No7906「" + SeichiAssist.seichiAssistConfig.getTitle1(7906)
                  + SeichiAssist.seichiAssistConfig.getTitle3(7906) + "」")) {
            setTitle(first = 7906, third = 7906)
          }
          player.openInventory(MenuInventoryData.titleEventData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleSpecial(player))
          return
        }//実績メニューに戻る
      }

      s"${prefix}実績「記念日」" => {
        event.isCancelled = true

        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */
        if (itemstackcurrent.getType === Material.BEDROCK) {
          val itemmeta = itemstackcurrent.itemMeta
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemmeta.displayName
          for (i in 9001..9036) {
            if (s"No$i「???」" in name) {
              SeichiAchievement.tryAchieve(player, i)
            }
          }

          player.openInventory(MenuInventoryData.titleExtraData(player))
        } else if (itemstackcurrent.getType === Material.DIAMOND_BLOCK) {
          val itemmeta = itemstackcurrent.itemMeta
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemmeta.displayName
          if (name.contains("No9001「" + SeichiAssist.seichiAssistConfig.getTitle1(9001) + "」")) {
            setTitle(first = 9001)
          } else if (name.contains("No9002「" + SeichiAssist.seichiAssistConfig.getTitle1(9002)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9002) + "」")) {
            setTitle(first = 9002, third = 9002)
          } else if (name.contains("No9003「" + SeichiAssist.seichiAssistConfig.getTitle1(9003) + "」")) {
            setTitle(first = 9003)
          } else if (name.contains("No9004「" + SeichiAssist.seichiAssistConfig.getTitle1(9004)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9004) + SeichiAssist.seichiAssistConfig.getTitle3(9004) + "」")) {
            setTitle(9004, 9004, 9004)
          } else if (name.contains("No9005「" + SeichiAssist.seichiAssistConfig.getTitle1(9005)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9005) + "」")) {
            setTitle(first = 9005, third = 9005)

          } else if (name.contains("No9006「" + SeichiAssist.seichiAssistConfig.getTitle1(9006) + "」")) {
            setTitle(first = 9006)
          } else if (name.contains("No9007「" + SeichiAssist.seichiAssistConfig.getTitle1(9007) + "」")) {
            setTitle(first = 9007)
          } else if (name.contains("No9008「" + SeichiAssist.seichiAssistConfig.getTitle1(9008)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9008) + "」")) {
            setTitle(first = 9008, third = 9008)
          } else if (name.contains("No9009「" + SeichiAssist.seichiAssistConfig.getTitle1(9009) + "」")) {
            setTitle(first = 9009)
            player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9009) + "」が設定されました。")
          } else if (name.contains("No9010「" + SeichiAssist.seichiAssistConfig.getTitle1(9010)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(9010) + "」")) {
            setTitle(9010, 9903, 9010)
          } else if (name.contains("No9011「" + SeichiAssist.seichiAssistConfig.getTitle1(9011)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9011) + "」")) {
            setTitle(first = 9011, third = 9011)
          } else if (name.contains("No9012「" + SeichiAssist.seichiAssistConfig.getTitle1(9012)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9012) + "」")) {
            setTitle(first = 9012, third = 9012)
          } else if (name.contains("No9013「" + SeichiAssist.seichiAssistConfig.getTitle1(9013) + "」")) {
            setTitle(first = 9013)
          } else if (name.contains("No9014「" + SeichiAssist.seichiAssistConfig.getTitle2(9014) + "」")) {
            setTitle(second = 9014)
          } else if (name.contains("No9015「" + SeichiAssist.seichiAssistConfig.getTitle1(9015)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9015) + "」")) {
            setTitle(first = 9015, third = 9015)
          } else if (name.contains("No9016「" + SeichiAssist.seichiAssistConfig.getTitle1(9016)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9016) + SeichiAssist.seichiAssistConfig.getTitle3(9016) + "」")) {
            setTitle(9016, 9016, 9016)
          } else if (name.contains("No9017「" + SeichiAssist.seichiAssistConfig.getTitle1(9017)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9017) + "」")) {
            setTitle(first = 9017, third = 9017)
          } else if (name.contains("No9018「" + SeichiAssist.seichiAssistConfig.getTitle1(9018) + "」")) {
            setTitle(first = 9018)
          } else if (name.contains("No9019「" + SeichiAssist.seichiAssistConfig.getTitle1(9019)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(9019) + "」")) {
            setTitle(9019, 9901, 9019)
          } else if (name.contains("No9020「" + SeichiAssist.seichiAssistConfig.getTitle1(9020)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9020) + "」")) {
            setTitle(first = 9020, third = 9020)
          } else if (name.contains("No9021「" + SeichiAssist.seichiAssistConfig.getTitle1(9021)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(9021) + "」")) {
            setTitle(9021, 9901, 9021)
          } else if (name.contains("No9022「" + SeichiAssist.seichiAssistConfig.getTitle1(9022)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9022) + "」")) {
            setTitle(first = 9022, third = 9022)
          } else if (name.contains("No9023「" + SeichiAssist.seichiAssistConfig.getTitle1(9023)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9023) + "」")) {
            setTitle(first = 9023, third = 9023)
          } else if (name.contains("No9024「" + SeichiAssist.seichiAssistConfig.getTitle1(9024)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9024) + "」")) {
            setTitle(first = 9024, third = 9024)
          } else if (name.contains("No9025「" + SeichiAssist.seichiAssistConfig.getTitle1(9025)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9025) + SeichiAssist.seichiAssistConfig.getTitle3(9025) + "」")) {
            setTitle(9025, 9025, 9025)
          } else if (name.contains("No9026「" + SeichiAssist.seichiAssistConfig.getTitle1(9026)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9026) + "」")) {
            setTitle(first = 9026, third = 9026)
          } else if (name.contains("No9027「" + SeichiAssist.seichiAssistConfig.getTitle1(9027)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9027) + "」")) {
            setTitle(first = 9027, third = 9027)
          } else if (name.contains("No9028「" + SeichiAssist.seichiAssistConfig.getTitle1(9028)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9028) + SeichiAssist.seichiAssistConfig.getTitle3(9028) + "」")) {
            setTitle(9028, 9028, 9028)
          } else if (name.contains("No9029「" + SeichiAssist.seichiAssistConfig.getTitle1(9029)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9029) + SeichiAssist.seichiAssistConfig.getTitle3(9029) + "」")) {
            setTitle(9029, 9029, 9029)
          } else if (name.contains("No9030「" + SeichiAssist.seichiAssistConfig.getTitle1(9030)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(9030) + "」")) {
            setTitle(9030, 9905, 9030)
          } else if (name.contains("No9031「" + SeichiAssist.seichiAssistConfig.getTitle1(9031)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9908) + SeichiAssist.seichiAssistConfig.getTitle3(9031) + "」")) {
            setTitle(9031, 9908, 9031)
          } else if (name.contains("No9032「" + SeichiAssist.seichiAssistConfig.getTitle1(9032)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9032) + "」")) {
            setTitle(first = 9032, third = 9032)
          } else if (name.contains("No9033「" + SeichiAssist.seichiAssistConfig.getTitle1(9033)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(9033) + "」")) {
            setTitle(9033, 9903, 9033)
          } else if (name.contains("No9034「" + SeichiAssist.seichiAssistConfig.getTitle1(9034)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9034) + "」")) {
            setTitle(first = 9034, third = 9034)
          } else if (name.contains("No9035「" + SeichiAssist.seichiAssistConfig.getTitle1(9035)
                  + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(9035) + "」")) {
            setTitle(9035, 9905, 9035)
          } else if (name.contains("No9036「" + SeichiAssist.seichiAssistConfig.getTitle1(9036)
                  + SeichiAssist.seichiAssistConfig.getTitle3(9036) + "」")) {
            setTitle(first = 9036, third = 9036)
          }
          player.openInventory(MenuInventoryData.titleExtraData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleLogin(player))
          return
        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowRight") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = playerdata.titlepage + 1
          player.openInventory(MenuInventoryData.titleExtraData(player))
          return
        }//次ページ
        //実績メニューに戻る

      }

      s"${prefix}実績「極秘任務」" => {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.getType === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.getType === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は「極秘実績」です。いろいろやってみましょう！")
          player.openInventory(MenuInventoryData.titleSecretData(player))
        } else if (itemstackcurrent.getType == Material.DIAMOND_BLOCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemstackcurrent.itemMeta.displayName

          when {
            ("No8001「" + SeichiAssist.seichiAssistConfig.getTitle1(8001)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(8001) + "」") in name => setTitle(8001, 9905, 8001)
            ("No8002「" + SeichiAssist.seichiAssistConfig.getTitle1(8002)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(8002) + "」") in name => setTitle(8002, 9905, 8002)
            ("No8003「" + SeichiAssist.seichiAssistConfig.getTitle1(8003)
                + SeichiAssist.seichiAssistConfig.getTitle3(8003) + "」") in name => setTitle(first = 8003, third = 8003)
          }
          player.openInventory(MenuInventoryData.titleSecretData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.titleSpecial(player))
        }//実績メニューに戻る
      }
      else => {
        // NOP
      }
    }
  }

  private def getTitle(where: Int, id: Int) = when (where) {
    1 => SeichiAssist.seichiAssistConfig.getTitle1(id)
    2 => SeichiAssist.seichiAssistConfig.getTitle2(id)
    3 => SeichiAssist.seichiAssistConfig.getTitle3(id)
    else => throw RuntimeException("メソッドの呼び出し規約違反: whereは、1..3のいずれかでなければいけません。")
  }
}
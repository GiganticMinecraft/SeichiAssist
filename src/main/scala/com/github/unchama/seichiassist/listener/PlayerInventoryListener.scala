package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist
import com.github.unchama.seichiassist._
import com.github.unchama.seichiassist.activeskill.effect.{ActiveSkillNormalEffect, ActiveSkillPremiumEffect}
import com.github.unchama.seichiassist.data.player.GiganticBerserk
import com.github.unchama.seichiassist.data.{ActiveSkillInventoryData, ItemData, MenuInventoryData}
import com.github.unchama.seichiassist.listener.invlistener.{OnActiveSkillUnselect, OnClickTitleMenu}
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.exp.ExperienceManager
import com.github.unchama.seichiassist.util.{StaticGachaPrizeFactory, Util}
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.util.ActionStatus
import org.bukkit.ChatColor._
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryCloseEvent, InventoryType}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.meta.{PotionMeta, SkullMeta}
import org.bukkit.inventory.{ItemFlag, ItemStack}
import org.bukkit.{Bukkit, Material, Sound}

import scala.collection.mutable.ArrayBuffer

class PlayerInventoryListener extends Listener {

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.sync
  import com.github.unchama.targetedeffect._
  import com.github.unchama.util.InventoryUtil._
  import com.github.unchama.util.syntax._

  private val playerMap = SeichiAssist.playermap
  private val gachaDataList = SeichiAssist.gachadatalist
  private val databaseGateway = SeichiAssist.databaseGateway

  //スキルメニューの処理
  @EventHandler
  def onPlayerClickActiveSkillSellectEvent(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }


    val topinventory = view.getTopInventory.ifNull {
      return
    }
    //インベントリが存在しない時終了
    //インベントリサイズが45でない時終了
    if (topinventory.row != 5) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap(uuid)

    //経験値変更用のクラスを設定
    val expman = new ExperienceManager(player)


    //インベントリ名が以下の時処理
    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "整地スキル選択") {
      val isSkull = itemstackcurrent.getType == Material.SKULL_ITEM

      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      //ARROWSKILL
      {
        val typeNum = ActiveSkill.ARROW.gettypenum()
        (4 to 9).foreach { skilllevel =>
          val name = ActiveSkill.ARROW.getName(skilllevel)
          if (itemstackcurrent.getType == ActiveSkill.ARROW.getMaterial(skilllevel)) {
            val potionmeta = itemstackcurrent.getItemMeta.asInstanceOf[PotionMeta]
            if (potionmeta.getBasePotionData.getType == ActiveSkill.ARROW.getPotionType(skilllevel)) {
              if (playerdata.activeskilldata.skilltype == typeNum && playerdata.activeskilldata.skillnum == skilllevel) {
                player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
                player.sendMessage(s"${YELLOW}選択を解除しました")
                playerdata.activeskilldata.skilltype = 0
                playerdata.activeskilldata.skillnum = 0
              } else {
                playerdata.activeskilldata.updateSkill(typeNum, skilllevel, 1)
                player.sendMessage(s"${GREEN}アクティブスキル:$name  が選択されました")
                player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat)
              }
            }
          }
        }
      }

      //MULTISKILL
      {
        val typeNum = ActiveSkill.MULTI.gettypenum()
        (4 to 9).foreach { skilllevel =>
          val name = ActiveSkill.MULTI.getName(skilllevel)
          if (itemstackcurrent.getType == ActiveSkill.MULTI.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.skilltype == typeNum && playerdata.activeskilldata.skillnum == skilllevel) {
              player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
              player.sendMessage(s"${YELLOW}選択を解除しました")
              playerdata.activeskilldata.skilltype = 0
              playerdata.activeskilldata.skillnum = 0
            } else {
              playerdata.activeskilldata.updateSkill(typeNum, skilllevel, 1)
              player.sendMessage(s"${GREEN}アクティブスキル:$name  が選択されました")
              player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat)
            }
          }
        }
      }

      //BREAKSKILL
      {
        val typeNum = ActiveSkill.BREAK.gettypenum()
        (1 to 9).foreach { skilllevel =>
          val name = ActiveSkill.BREAK.getName(skilllevel)
          if (itemstackcurrent.getType == ActiveSkill.BREAK.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.skilltype == typeNum && playerdata.activeskilldata.skillnum == skilllevel) {
              player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
              player.sendMessage(s"${YELLOW}選択を解除しました")
              playerdata.activeskilldata.skilltype = 0
              playerdata.activeskilldata.skillnum = 0
            } else {
              playerdata.activeskilldata.updateSkill(typeNum, skilllevel, 1)
              player.sendMessage(s"${GREEN}アクティブスキル:$name  が選択されました")
              player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat)
            }
          }
        }
      }

      //CONDENSKILL
      //WATER
      {
        val typeNum = ActiveSkill.WATERCONDENSE.gettypenum()

        (7 to 9).foreach { skilllevel =>
          val name = ActiveSkill.WATERCONDENSE.getName(skilllevel)
          if (itemstackcurrent.getType == ActiveSkill.WATERCONDENSE.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.assaulttype == typeNum && playerdata.activeskilldata.assaultnum == skilllevel) {
              player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
              player.sendMessage(s"${YELLOW}選択を解除しました")
              playerdata.activeskilldata.assaulttype = 0
              playerdata.activeskilldata.assaultnum = 0
            } else {
              playerdata.activeskilldata.updateAssaultSkill(player, typeNum, skilllevel, 1)
              player.sendMessage(s"${DARK_GREEN}アサルトスキル:$name  が選択されました")
              player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat)
            }
          }
        }
      }

      //LAVA
      {
        val typeNum = ActiveSkill.LAVACONDENSE.gettypenum()
        (7 to 9).foreach { skilllevel =>
          val name = ActiveSkill.LAVACONDENSE.getName(skilllevel)
          if (itemstackcurrent.getType == ActiveSkill.LAVACONDENSE.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.assaulttype == typeNum && playerdata.activeskilldata.assaultnum == skilllevel) {
              player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
              player.sendMessage(s"${YELLOW}選択を解除しました")
              playerdata.activeskilldata.assaulttype = 0
              playerdata.activeskilldata.assaultnum = 0
            } else {
              playerdata.activeskilldata.updateAssaultSkill(player, typeNum, skilllevel, 1)
              player.sendMessage(s"${DARK_GREEN}アサルトスキル:$name  が選択されました")
              player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat)
            }
          }
        }
      }

      {
        val typeNum = ActiveSkill.FLUIDCONDENSE.gettypenum()
        val skillLevel = 10
        if (itemstackcurrent.getType == ActiveSkill.FLUIDCONDENSE.getMaterial(skillLevel)) {
          if (playerdata.activeskilldata.assaultnum == skillLevel && playerdata.activeskilldata.assaulttype == typeNum) {
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
            player.sendMessage(s"${YELLOW}選択を解除しました")
            playerdata.activeskilldata.assaulttype = 0
            playerdata.activeskilldata.assaultnum = 0
          } else {
            playerdata.activeskilldata.updateAssaultSkill(player, typeNum, skillLevel, 1)
            player.sendMessage(s"${DARK_GREEN}アサルトスキル:ヴェンダー・ブリザード が選択されました")
            player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat)
          }
        }
      }

      //アサルトアーマー
      {
        val typeNum = ActiveSkill.ARMOR.gettypenum()
        val skilllevel = 10
        if (itemstackcurrent.getType == ActiveSkill.ARMOR.getMaterial(skilllevel)) {
          if (playerdata.activeskilldata.assaultnum == skilllevel && playerdata.activeskilldata.assaulttype == typeNum) {
            player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
            player.sendMessage(s"${YELLOW}選択を解除しました")
            playerdata.activeskilldata.assaulttype = 0
            playerdata.activeskilldata.assaultnum = 0
          } else {
            playerdata.activeskilldata.updateAssaultSkill(player, typeNum, skilllevel, 1)
            player.sendMessage(s"${DARK_GREEN}アサルトスキル:アサルト・アーマー が選択されました")
            player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat)
          }
        }
      }

      //ページ変更処理
      if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowLeft") {
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

        seichiassist.unsafe.runAsyncTargetedEffect(player)(
          sequentialEffect(
            CommonSoundEffects.menuTransitionFenceSound,
            StickMenu.firstPage.open
          ),
          "棒メニューの1ページ目を開く"
        )
      } else {
        itemstackcurrent.getType match {
          case Material.STONE_BUTTON =>
            if (itemstackcurrent.getItemMeta.getDisplayName.contains("リセット")) {
              //経験値変更用のクラスを設定
              //経験値が足りなかったら処理を終了
              if (!expman.hasExp(10000)) {
                player.sendMessage(RED.toString + "必要な経験値が足りません")
                player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
                return
              }
              //経験値消費
              expman.changeExp(-10000)

              //リセット処理
              playerdata.activeskilldata.reset()
              //スキルポイント更新
              playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
              //リセット音を流す
              player.playSound(player.getLocation, Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1f, 0.1.toFloat)
              //メッセージを流す
              player.sendMessage(LIGHT_PURPLE.toString + "アクティブスキルポイントをリセットしました")
              //メニューを開く
              player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
            }

          case Material.GLASS =>
            if (playerdata.activeskilldata.skilltype == 0 && playerdata.activeskilldata.skillnum == 0
              && playerdata.activeskilldata.assaulttype == 0 && playerdata.activeskilldata.assaultnum == 0) {
              player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
              player.sendMessage(YELLOW.toString + "既に全ての選択は削除されています")
            } else {
              playerdata.activeskilldata.clearSelection(player)
            }

          case Material.BOOKSHELF =>
            //開く音を再生
            player.playSound(player.getLocation, Sound.BLOCK_BREWING_STAND_BREW, 1f, 0.5.toFloat)
            player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player))

          case _ =>
        }
      }
    }
  }

  //スキルエフェクトメニューの処理 + エフェクト開放の処理
  @EventHandler
  def onPlayerClickActiveSkillEffectSellectEvent(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }
    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }

    val topinventory = view.getTopInventory.ifNull {
      return
    }
    //インベントリが存在しない時終了
    //インベントリサイズ終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap(uuid)

    //インベントリ名が以下の時処理
    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "整地スキルエフェクト選択") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.getType == Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      val currentType = itemstackcurrent.getType
      if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowLeft") {
        //開く音を再生
        player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1.toFloat)
        player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
        return
      } else if (currentType == Material.GLASS) {
        if (playerdata.activeskilldata.effectnum == 0) {
          player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          player.sendMessage(YELLOW.toString + "既に選択されています")
        } else {
          playerdata.activeskilldata.effectnum = 0
          player.sendMessage(GREEN.toString + "エフェクト:未設定  が選択されました")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat)
        }
        return
      } else if (currentType == Material.BOOK_AND_QUILL) {
        //開く音を再生
        player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1.toFloat)
        player.openInventory(MenuInventoryData.getBuyRecordMenuData(player))
        return
      } else {
        val skilleffect = ActiveSkillNormalEffect.values
        skilleffect.foreach { activeSkillEffect =>
          if (currentType == activeSkillEffect.material) {
            if (playerdata.activeskilldata.effectnum == activeSkillEffect.num) {
              player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
              player.sendMessage(YELLOW.toString + "既に選択されています")
            } else {
              playerdata.activeskilldata.effectnum = activeSkillEffect.num
              player.sendMessage(GREEN.toString + "エフェクト:" + activeSkillEffect.nameOnUI + RESET + "" + GREEN + " が選択されました")
              player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat)
            }
          }
        }
        ActiveSkillPremiumEffect.values.foreach { activeSkillPremiumEffect =>
          if (currentType == activeSkillPremiumEffect.material) {
            if (playerdata.activeskilldata.effectnum == activeSkillPremiumEffect.num) {
              player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
              player.sendMessage(YELLOW.toString + "既に選択されています")
            } else {
              playerdata.activeskilldata.effectnum = activeSkillPremiumEffect.num + 100
              player.sendMessage(GREEN.toString + "" + BOLD + "プレミアムエフェクト:" + activeSkillPremiumEffect.desc + RESET + "" + GREEN + "" + BOLD + " が選択されました")
              player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat)
            }
          }
        }
      }


      //ここからエフェクト開放の処理
      if (currentType == Material.BEDROCK) {
        val itemmeta = itemstackcurrent.getItemMeta
        val skilleffect = ActiveSkillNormalEffect.values
        skilleffect.foreach { activeSkillEffect =>
          if (itemmeta.getDisplayName.contains(activeSkillEffect.nameOnUI)) {
            if (playerdata.activeskilldata.effectpoint < activeSkillEffect.usePoint) {
              player.sendMessage(DARK_RED.toString + "エフェクトポイントが足りません")
              player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat)
            } else {
              playerdata.activeskilldata.obtainedSkillEffects.add(activeSkillEffect)
              player.sendMessage(LIGHT_PURPLE.toString + "エフェクト：" + activeSkillEffect.nameOnUI + RESET + "" + LIGHT_PURPLE + "" + BOLD + "" + " を解除しました")
              player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat)
              playerdata.activeskilldata.effectpoint -= activeSkillEffect.usePoint
              player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player))
            }
          }
        }
      }

      //ここからプレミアムエフェクト開放の処理
      if (currentType == Material.BEDROCK) {
        val itemmeta = itemstackcurrent.getItemMeta
        val premiumeffect = ActiveSkillPremiumEffect.values
        premiumeffect.foreach { activeSkillPremiumEffect =>
          if (itemmeta.getDisplayName.contains(activeSkillPremiumEffect.desc)) {
            if (playerdata.activeskilldata.premiumeffectpoint < activeSkillPremiumEffect.usePoint) {
              player.sendMessage(DARK_RED.toString + "プレミアムエフェクトポイントが足りません")
              player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.5f)
            } else {
              playerdata.activeskilldata.obtainedSkillPremiumEffects.add(activeSkillPremiumEffect)
              player.sendMessage(LIGHT_PURPLE.toString + "" + BOLD + "プレミアムエフェクト：" + activeSkillPremiumEffect.desc + RESET + "" + LIGHT_PURPLE + "" + BOLD + "" + " を解除しました")
              if (databaseGateway.donateDataManipulator.addPremiumEffectBuy(playerdata, activeSkillPremiumEffect) == ActionStatus.Fail) {
                player.sendMessage("購入履歴が正しく記録されませんでした。管理者に報告してください。")
              }
              player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2f)
              playerdata.activeskilldata.premiumeffectpoint -= activeSkillPremiumEffect.usePoint
              player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player))
            }
          }
        }
      }
    }
  }

  //スキル解放の処理
  @EventHandler
  def onPlayerClickActiveSkillReleaseEvent(event: InventoryClickEvent): Unit = {
    OnActiveSkillUnselect.onPlayerClickActiveSkillReleaseEvent(event)
  }

  //ランキングメニュー
  @EventHandler
  def onPlayerClickSeichiRankingMenuEvent(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }

    val topinventory = view.getTopInventory.ifNull {
      return
    }
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]

    val isSkull = itemstackcurrent.getType == Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "整地神ランキング") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull) {
        // safe cast
        val skullMeta = itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta]
        val name = skullMeta.getDisplayName
        skullMeta.getOwner match {
          case "MHF_ArrowLeft" =>
            import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

            seichiassist.unsafe.runAsyncTargetedEffect(player)(
              sequentialEffect(
                CommonSoundEffects.menuTransitionFenceSound,
                StickMenu.firstPage.open
              ),
              "棒メニューの1ページ目を開く"
            )

          case "MHF_ArrowDown" =>
            itemstackcurrent.getItemMeta
            if (name.contains("整地神ランキング") && name.contains("ページ目")) { //移動するページの種類を判定
              val page_display = Integer.parseInt(name.replaceAll("[^0-9]", "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
              player.openInventory(MenuInventoryData.getRankingList(page_display - 1))
            }

          case "MHF_ArrowUp" =>
            itemstackcurrent.getItemMeta
            if (name.contains("整地神ランキング") && name.contains("ページ目")) { //移動するページの種類を判定
              val page_display = Integer.parseInt(name.replaceAll("[^0-9]", "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
              player.openInventory(MenuInventoryData.getRankingList(page_display - 1))
            }

          case _ =>
        }
      }
    }
  }

  //ランキングメニュー
  @EventHandler
  def onPlayerClickSeichiRankingMenuEvent1(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }

    val topinventory = view.getTopInventory.ifNull {
      return
    }
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]

    val isSkull = itemstackcurrent.getType == Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "ログイン神ランキング") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowLeft") {
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

        seichiassist.unsafe.runAsyncTargetedEffect(player)(
          sequentialEffect(
            CommonSoundEffects.menuTransitionFenceSound,
            StickMenu.firstPage.open
          ),
          "棒メニューの1ページ目を開く"
        )
      } else if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowDown") {
        val itemmeta = itemstackcurrent.getItemMeta
        if (itemmeta.getDisplayName.contains("ログイン神ランキング") && itemmeta.getDisplayName.contains("ページ目")) { //移動するページの種類を判定
          val page_display = Integer.parseInt(itemmeta.getDisplayName.replaceAll("[^0-9]", "")) //数字以外を全て消す

          //開く音を再生
          player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
          player.openInventory(MenuInventoryData.getRankingList_playtick(page_display - 1))
        }
      } else if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowUp") {
        val itemmeta = itemstackcurrent.getItemMeta
        if (itemmeta.getDisplayName.contains("ログイン神ランキング") && itemmeta.getDisplayName.contains("ページ目")) { //移動するページの種類を判定
          val page_display = Integer.parseInt(itemmeta.getDisplayName.replaceAll("[^0-9]", "")) //数字以外を全て消す

          //開く音を再生
          player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
          player.openInventory(MenuInventoryData.getRankingList_playtick(page_display - 1))
        }
      }
    }
  }

  //ランキングメニュー
  @EventHandler
  def onPlayerClickSeichiRankingMenuEvent2(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }

    val topinventory = view.getTopInventory.ifNull {
      return
    }
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]

    val isSkull = itemstackcurrent.getType == Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "投票神ランキング") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull) {
        val skullMeta = itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta]
        skullMeta.getOwner match {
          case "MHF_ArrowLeft" =>
            import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, sync}

            seichiassist.unsafe.runAsyncTargetedEffect(player)(
              sequentialEffect(
                CommonSoundEffects.menuTransitionFenceSound,
                StickMenu.firstPage.open
              ),
              "棒メニューの1ページ目を開く"
            )

          case "MHF_ArrowDown" =>
            val itemmeta = itemstackcurrent.getItemMeta
            if (itemmeta.getDisplayName.contains("投票神ランキング") && itemmeta.getDisplayName.contains("ページ目")) { //移動するページの種類を判定
              val page_display = Integer.parseInt(itemmeta.getDisplayName.replaceAll("[^0-9]", "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
              player.openInventory(MenuInventoryData.getRankingList_p_vote(page_display - 1))
            }

          case "MHF_ArrowUp" =>
            val itemmeta = itemstackcurrent.getItemMeta
            if (itemmeta.getDisplayName.contains("投票神ランキング") && itemmeta.getDisplayName.contains("ページ目")) { //移動するページの種類を判定
              val page_display = Integer.parseInt(itemmeta.getDisplayName.replaceAll("[^0-9]", "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
              player.openInventory(MenuInventoryData.getRankingList_p_vote(page_display - 1))
            }
        }
      }
    }
  }

  //ランキングメニュー
  @EventHandler
  def onOpenDonationRanking(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }

    val topinventory = view.getTopInventory.ifNull {
      return
    }
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]

    //インベントリ名が以下の時処理
    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "寄付神ランキング") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.getType == Material.SKULL_ITEM
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull) {
        val skullMeta = itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta]
        val name = skullMeta.getDisplayName
        skullMeta.getOwner match {
          case "MHF_ArrowLeft" =>
            import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, sync}

            seichiassist.unsafe.runAsyncTargetedEffect(player)(
              sequentialEffect(
                CommonSoundEffects.menuTransitionFenceSound,
                StickMenu.firstPage.open
              ),
              "棒メニューの1ページ目を開く"
            )

          case "MHF_ArrowDown" =>
            if (name.contains("寄付神ランキング") && name.contains("ページ目")) { //移動するページの種類を判定
              val page_display = Integer.parseInt(name.replaceAll("[^0-9]", "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
              player.openInventory(MenuInventoryData.getRankingList_premiumeffectpoint(page_display - 1))
            }

          case "MHF_ArrowUp" =>
            if (name.contains("寄付神ランキング") && name.contains("ページ目")) { //移動するページの種類を判定
              val page_display = Integer.parseInt(name.replaceAll("[^0-9]", "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat)
              player.openInventory(MenuInventoryData.getRankingList_premiumeffectpoint(page_display - 1))
            }
        }
      }
    }
  }

  //購入履歴メニュー
  @EventHandler
  def onPlayerClickPremiumLogMenuEvent(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }

    val topinventory = view.getTopInventory.ifNull {
      return
    }
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 4) {
      return
    }
    val player = he.asInstanceOf[Player]

    //インベントリ名が以下の時処理
    if (topinventory.getTitle == BLUE.toString + "" + BOLD + "プレミアムエフェクト購入履歴") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.getType == Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowLeft") {
        //開く音を再生
        player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1.toFloat)
        player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player))
      }
    }
  }

  //ガチャ交換システム
  @EventHandler
  def onGachaTradeEvent(event: InventoryCloseEvent): Unit = {
    val player = event.getPlayer.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap(uuid).ifNull {
      return
    }
    //エラー分岐
    val name = playerdata.lowercaseName
    val inventory = event.getInventory

    //インベントリサイズが36でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.getTitle == s"${LIGHT_PURPLE.toString}${BOLD}交換したい景品を入れてください") {
      var givegacha = 0
      /*
			 * step1 for文でinventory内に対象商品がないか検索
			 * あったらdurabilityに応じてgivegachaを増やし、非対象商品は返却boxへ
			 */
      //ガチャ景品交換インベントリの中身を取得
      val item = inventory.getContents
      //ドロップ用アイテムリスト(返却box)作成
      val dropitem = ArrayBuffer[ItemStack]()
      //カウント用
      var big = 0
      var reg = 0

      //for文で１個ずつ対象アイテムか見る
      //ガチャ景品交換インベントリを一個ずつ見ていくfor文
      item.foreach {
        case null =>
        case m if
        SeichiAssist.gachamente ||
          !m.hasItemMeta ||
          !m.getItemMeta.hasLore ||
          m.getType == Material.SKULL_ITEM =>
          dropitem += m
        case m =>
          //ガチャ景品リスト上を線形探索する
          val matchingGachaData = gachaDataList.find { gachadata =>
            //ガチャ景品リストにある商品の場合(Lore=説明文と表示名で判別),無い場合はアイテム返却
            if (gachadata.itemStack.hasItemMeta && gachadata.itemStack.getItemMeta.hasLore && gachadata.compare(m, name)) {
              if (SeichiAssist.DEBUG) player.sendMessage(gachadata.itemStack.getItemMeta.getDisplayName)
              val amount = m.getAmount

              if (gachadata.probability < 0.001) {
                //ギガンティック大当たりの部分
                //ガチャ券に交換せずそのままアイテムを返す
                dropitem += m
              } else if (gachadata.probability < 0.01) {
                //大当たりの部分
                givegacha += 12 * amount
                big += 1
              } else if (gachadata.probability < 0.1) {
                //当たりの部分
                givegacha += 3 * amount
                reg += 1
              } else {
                //それ以外アイテム返却(経験値ポーションとかがここにくるはず)
                dropitem += m
              }
              true
            } else false
          }
          matchingGachaData match {
            //ガチャ景品リストに対象アイテムが無かった場合
            case None => dropitem += m
            case _ =>
          }
      }
      //ガチャシステムメンテナンス中は全て返却する
      if (SeichiAssist.gachamente) {
        player.sendMessage(RED.toString + "ガチャシステムメンテナンス中の為全てのアイテムを返却します")
      } else if (big <= 0 && reg <= 0) {
        player.sendMessage(YELLOW.toString + "景品を認識しませんでした。全てのアイテムを返却します")
      } else {
        player.sendMessage(GREEN.toString + "大当たり景品を" + big + "個、当たり景品を" + reg + "個認識しました")
      }
      /*
			 * step2 非対象商品をインベントリに戻す
			 */
      for (m <- dropitem) {
        if (!Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, m)
        } else {
          Util.dropItem(player, m)
        }
      }
      /*
			 * step3 ガチャ券をインベントリへ
			 */
      val skull = Util.getExchangeskull(player.getName)
      var count = 0
      while (givegacha > 0) {
        if (player.getInventory.contains(skull) || !Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, skull)
        } else {
          Util.dropItem(player, skull)
        }
        givegacha -= 1
        count += 1
      }
      if (count > 0) {
        player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
        player.sendMessage(GREEN.toString + "" + count + "枚の" + GOLD + "ガチャ券" + WHITE + "を受け取りました")
      }
    }

  }

  //実績メニューの処理
  @EventHandler
  def onPlayerClickTitleMenuEvent(event: InventoryClickEvent): Unit = {
    OnClickTitleMenu.onPlayerClickTitleMenuEvent(event)
  }

  //鉱石・交換券変換システム
  @EventHandler
  def onOreTradeEvent(event: InventoryCloseEvent): Unit = {
    val player = event.getPlayer.asInstanceOf[Player]

    //エラー分岐
    val inventory = event.getInventory

    //インベントリサイズが36でない時終了
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
      inventoryContents
        .partition { stack => requiredAmountPerTicket.contains(stack.getType) }

    val exchangingAmount = itemsToExchange
      .groupBy(_.getType)
      .toList
      .map { case (key, stacks) => key -> stacks.map(_.getAmount).sum }

    val ticketAmount = exchangingAmount
      .map { case (material, amount) => amount / requiredAmountPerTicket(material) }
      .sum

    //プレイヤー通知
    if (ticketAmount == 0) {
      player.sendMessage(s"${YELLOW}鉱石を認識しなかったか数が不足しています。全てのアイテムを返却します")
    } else {
      player.sendMessage(s"${DARK_RED}交換券$RESET${GREEN}を${ticketAmount}枚付与しました")
    }

    /*
     * step2 交換券をインベントリへ
     */
    val exchangeTicket = {
      new ItemStack(Material.PAPER).modify {
        _.setItemMeta {
          Bukkit.getItemFactory.getItemMeta(Material.PAPER).modify { m =>
            import m._
            setDisplayName(s"$DARK_RED${BOLD}交換券")
            addEnchant(Enchantment.PROTECTION_FIRE, 1, false)
            addItemFlags(ItemFlag.HIDE_ENCHANTS)
          }
        }
      }
    }

    val ticketsToGive = Seq.fill(ticketAmount)(exchangeTicket)

    import syntax._
    if (ticketsToGive.nonEmpty) {
      unsafe.runAsyncTargetedEffect(player)(
        sequentialEffect(
          Util.grantItemStacksEffect(ticketsToGive: _*),
          FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1f, 1f),
          s"${GREEN}交換券の付与が終わりました".asMessageEffect()
        ),
        "交換券を付与する"
      )
    }

    /*
     * step3 非対象・余剰鉱石の返却
     */
    val itemStacksToReturn =
      exchangingAmount
        .flatMap { case (exchangedMaterial, exchangedAmount) =>
          val returningAmount = exchangedAmount % requiredAmountPerTicket(exchangedMaterial)

          if (returningAmount != 0)
            Some(new ItemStack(exchangedMaterial).modify(_.setAmount(returningAmount)))
          else
            None
        }.++(rejectedItems)

    //返却処理
    unsafe.runAsyncTargetedEffect(player)(
      Util.grantItemStacksEffect(itemStacksToReturn: _*),
      "鉱石交換でのアイテム返却を行う"
    )
  }

  //ギガンティック→椎名林檎交換システム
  @EventHandler
  def onGachaRingoEvent(event: InventoryCloseEvent): Unit = {
    val player = event.getPlayer.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap(uuid).ifNull {
      return
    }
    //エラー分岐
    val name = playerdata.lowercaseName
    val inventory = event.getInventory

    //インベントリサイズが4列でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.getTitle == GOLD.toString + "" + BOLD + "椎名林檎と交換したい景品を入れてネ") {
      var giveringo = 0
      /*
			 * step1 for文でinventory内に対象商品がないか検索
			 * あったらdurabilityに応じてgivegachaを増やし、非対象商品は返却boxへ
			 */
      //ガチャ景品交換インベントリの中身を取得
      val item = inventory.getContents
      //ドロップ用アイテムリスト(返却box)作成
      val dropitem = ArrayBuffer[ItemStack]()
      //カウント用
      var giga = 0
      //for文で１個ずつ対象アイテムか見る
      //ガチャ景品交換インベントリを一個ずつ見ていくfor文
      item.foreach {
        case null =>
        case m if
        SeichiAssist.gachamente ||
          !m.hasItemMeta ||
          !m.getItemMeta.hasLore ||
          m.getType == Material.SKULL_ITEM =>
          dropitem.addOne(m)
        case m =>
          //ガチャ景品リストを一個ずつ見ていくfor文
          gachaDataList.find { gachadata =>
            if (gachadata.itemStack.hasItemMeta && gachadata.itemStack.getItemMeta.hasLore && gachadata.compare(m, name)) {
              if (SeichiAssist.DEBUG) {
                player.sendMessage(gachadata.itemStack.getItemMeta.getDisplayName)
              }
              val amount = m.getAmount
              if (gachadata.probability < 0.001) {
                //ギガンティック大当たりの部分
                //1個につき椎名林檎n個と交換する
                giveringo += SeichiAssist.seichiAssistConfig.rateGiganticToRingo() * amount
                giga += 1
              } else {
                //それ以外アイテム返却
                dropitem.addOne(m)
              }
              true
            } else false
          } match {
            case None => dropitem.addOne(m)
            case _ =>
          }
      }
      //ガチャシステムメンテナンス中は全て返却する
      if (SeichiAssist.gachamente) {
        player.sendMessage(RED.toString + "ガチャシステムメンテナンス中の為全てのアイテムを返却します")
      } else if (giga <= 0) {
        player.sendMessage(YELLOW.toString + "ギガンティック大当り景品を認識しませんでした。全てのアイテムを返却します")
      } else {
        player.sendMessage(GREEN.toString + "ギガンティック大当り景品を" + giga + "個認識しました")
      }
      /*
			 * step2 非対象商品をインベントリに戻す
			 */
      for (m <- dropitem) {
        if (!Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, m)
        } else {
          Util.dropItem(player, m)
        }
      }
      /*
			 * step3 椎名林檎をインベントリへ
			 */
      val ringo = StaticGachaPrizeFactory.getMaxRingo(player.getName)
      var count = 0
      while (giveringo > 0) {
        if (player.getInventory.contains(ringo) || !Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, ringo)
        } else {
          Util.dropItem(player, ringo)
        }
        giveringo -= 1
        count += 1
      }
      if (count > 0) {
        player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
        player.sendMessage(GREEN.toString + "" + count + "個の" + GOLD + "椎名林檎" + WHITE + "を受け取りました")
      }
    }

  }

  @EventHandler
  def onTitanRepairEvent(event: InventoryCloseEvent): Unit = {
    val player = event.getPlayer.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap(uuid).ifNull {
      return
    }
    //エラー分岐
    val inventory = event.getInventory

    //インベントリサイズが36でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.getTitle == GOLD.toString + "" + BOLD + "修繕したい限定タイタンを入れてネ") {
      //インベントリの中身を取得
      val item = inventory.getContents

      var count = 0
      //for文で１個ずつ対象アイテムか見る
      //インベントリを一個ずつ見ていくfor文
      for (m <- item) {
        if (m != null) {
          if (m.getItemMeta.hasLore) {
            if (Util.isLimitedTitanItem(m)) {
              m.setDurability(1.toShort)
              count += 1
            }
          }

          if (!Util.isPlayerInventoryFull(player)) {
            Util.addItem(player, m)
          } else {
            Util.dropItem(player, m)
          }
        }
      }
      if (count < 1) {
        player.sendMessage(GREEN.toString + "限定タイタンを認識しませんでした。すべてのアイテムを返却します")
      } else {
        player.sendMessage(GREEN.toString + "限定タイタンを" + count + "個認識し、修繕しました。")
      }
    }
  }

  //投票ptメニュー
  @EventHandler
  def onVotingMenuEvent(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }

    val topinventory = view.getTopInventory.ifNull {
      return
    }
    //インベントリが存在しない時終了
    //インベントリサイズが4列でない時終了
    if (topinventory.row != 4) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap(uuid)

    //インベントリ名が以下の時処理
    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "投票ptメニュー") {
      event.setCancelled(true)

      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.getType == Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      //投票pt受取
      if (itemstackcurrent.getType == Material.DIAMOND) {
        //nは特典をまだ受け取ってない投票分
        var n = databaseGateway.playerDataManipulator.compareVotePoint(player, playerdata)
        //投票数に変化が無ければ処理終了
        if (n == 0) {
          return
        }
        //先にp_voteの値を更新しておく
        playerdata.p_givenvote = playerdata.p_givenvote + n

        var count = 0
        while (n > 0) {
          //ここに投票1回につきプレゼントする特典の処理を書く

          //ガチャ券プレゼント処理
          val skull = Util.getVoteskull(player.getName)
          for {i <- 0 to 9} {
            if (player.getInventory.contains(skull) || !Util.isPlayerInventoryFull(player)) {
              Util.addItem(player, skull)
            } else {
              Util.dropItem(player, skull)
            }
          }

          //ピッケルプレゼント処理(レベル50になるまで)
          if (playerdata.level < 50) {
            val pickaxe = ItemData.getSuperPickaxe(1)
            if (Util.isPlayerInventoryFull(player)) {
              Util.dropItem(player, pickaxe)
            } else {
              Util.addItem(player, pickaxe)
            }
          }

          //投票ギフト処理(レベル50から)
          if (playerdata.level >= 50) {
            val gift = ItemData.getVotingGift(1)
            if (Util.isPlayerInventoryFull(player)) {
              Util.dropItem(player, gift)
            } else {
              Util.addItem(player, gift)
            }
          }
          //エフェクトポイント加算処理
          playerdata.activeskilldata.effectpoint += 10

          n -= 1
          count += 1
        }

        player.sendMessage(GOLD.toString + "投票特典" + WHITE + "(" + count + "票分)を受け取りました")
        player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)

        val itemmeta = itemstackcurrent.getItemMeta
        itemstackcurrent.setItemMeta(itemmeta)
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.getType == Material.BOOK_AND_QUILL) {
        // 投票リンク表示
        player.sendMessage(RED.toString + "" + UNDERLINE + "https://minecraft.jp/servers/54d3529e4ddda180780041a7/vote")
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.closeInventory()
      } else if (isSkull && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowLeft") {
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

        seichiassist.unsafe.runAsyncTargetedEffect(player)(
          sequentialEffect(
            CommonSoundEffects.menuTransitionFenceSound,
            StickMenu.firstPage.open
          ),
          "棒メニューの1ページ目を開く"
        )

        // NOTE: WHEN
      } else if (itemstackcurrent.getType == Material.WATCH) {
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleVotingFairy = playerdata.toggleVotingFairy % 4 + 1
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.getType == Material.PAPER) {
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleGiveApple = playerdata.toggleGiveApple % 4 + 1
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.getType == Material.JUKEBOX) {
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleVFSound = !playerdata.toggleVFSound
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.getType == Material.GHAST_TEAR) {
        player.closeInventory()

        //プレイヤーレベルが10に達していないとき
        if (playerdata.level < 10) {
          player.sendMessage(GOLD.toString + "プレイヤーレベルが足りません")
          player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          return
        }

        //既に妖精召喚している場合終了
        if (playerdata.usingVotingFairy) {
          player.sendMessage(GOLD.toString + "既に妖精を召喚しています")
          player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          return
        }

        //投票ptが足りない場合終了
        if (playerdata.activeskilldata.effectpoint < playerdata.toggleVotingFairy * 2) {
          player.sendMessage(GOLD.toString + "投票ptが足りません")
          player.playSound(player.getLocation, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat)
          return
        }

        VotingFairyListener.summon(player)
        player.closeInventory()
      } else if (itemstackcurrent.getType == Material.COMPASS) {
        VotingFairyTask.speak(player, "僕は" + Util.showHour(playerdata.votingFairyEndTime) + "には帰るよー。", playerdata.toggleVFSound)
        player.closeInventory()
      } //妖精召喚
      //妖精音トグル
      //妖精リンゴトグル
      //妖精時間トグル
      //棒メニューに戻る

    }
  }

  @EventHandler
  def onGiganticBerserkMenuEvent(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }

    //インベントリが存在しない時終了
    val topinventory = view.getTopInventory.ifNull {
      return
    }

    //インベントリが6列でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap(uuid)

    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "スキルを進化させますか?") {
      event.setCancelled(true)
      if (itemstackcurrent.getType == Material.NETHER_STAR) {
        playerdata.giganticBerserk = GiganticBerserk(0, 0, playerdata.giganticBerserk.stage + 1, canEvolve = false)
        player.playSound(player.getLocation, Sound.BLOCK_END_GATEWAY_SPAWN, 1f, 0.5.toFloat)
        player.playSound(player.getLocation, Sound.ENTITY_ENDERDRAGON_AMBIENT, 1f, 0.8.toFloat)
        player.openInventory(MenuInventoryData.getGiganticBerserkEvolution2Menu(player))
      }
    } else if (topinventory.getTitle == LIGHT_PURPLE.toString + "" + BOLD + "スキルを進化させました") {
      event.setCancelled(true)
    }
  }
}

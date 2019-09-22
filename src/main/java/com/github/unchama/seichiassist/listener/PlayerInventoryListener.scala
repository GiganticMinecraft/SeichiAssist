package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.data.player.GiganticBerserk
import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver
import com.google.common.io.ByteStreams
import org.bukkit.ChatColor._
import org.bukkit.entity.EntityType
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{Material, Sound}

class PlayerInventoryListener  extends  Listener {
  private val playerMap = SeichiAssist.playermap
  private val gachaDataList = SeichiAssist.gachadatalist
  private val databaseGateway = SeichiAssist.databaseGateway

  //サーバー選択メニュー
  @EventHandler
  def onPlayerClickServerSwitchMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.getView
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.getTopInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 2) {
      return
    }
    val player = he.asInstanceOf[Player]

    //インベントリ名が以下の時処理
    if (topinventory.title == s"$DARK_RED${BOLD}サーバーを選択してください") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.getType === InventoryType.PLAYER) return

      val meta = itemstackcurrent.itemMeta

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      val byteArrayDataOutput = ByteStreams.newDataOutput()

      //ページ変更処理
      val displayName = meta.displayName
      val targetServerName = when {
        "アルカディアサーバー" in displayName => "s1"
        "エデンサーバー" in displayName => "s2"
        "ヴァルハラサーバー" in displayName => "s3"
        "建築サーバー" in displayName => "s8"
        "公共施設サーバー" in displayName => "s7"
        else => throw IllegalStateException("Reached unreachable segment.")
      }

      byteArrayDataOutput.writeUTF("Connect")
      byteArrayDataOutput.writeUTF(targetServerName)
      player.sendPluginMessage(SeichiAssist.instance, "BungeeCord", byteArrayDataOutput.toByteArray())
    }
  }

  //追加!!!
  //スキルメニューの処理
  @EventHandler
  def onPlayerClickPassiveSkillSellectEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory === null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 4) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]

    //経験値変更用のクラスを設定
    //ExperienceManager expman = new ExperienceManager(player);


    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "整地スキル切り替え") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.getType === InventoryType.PLAYER) {
        return
      }
      val isSkull = itemstackcurrent.getType === Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      //ページ変更処理
      // =>
      // val swords = EnumSet.of(Material.WOOD_SWORD, Material.STONE_SWORD, Material.GOLD_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD)
      if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else {
        val type = itemstackcurrent.getType
        when (type) {
          Material.DIAMOND_PICKAXE => {
            // 複数破壊トグル

            if (playerdata.level >= SeichiAssist.seichiAssistConfig.multipleIDBlockBreaklevel) {
              playerdata.settings.multipleidbreakflag = !playerdata.settings.multipleidbreakflag
              if (playerdata.settings.multipleidbreakflag) {
                player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
                player.sendMessage(GREEN.toString() + "複数種類同時破壊:ON")
              } else {
                player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5.toFloat())
                player.sendMessage(RED.toString() + "複数種類同時破壊:OFF")
              }
              val itemmeta = itemstackcurrent.itemMeta
              itemstackcurrent.itemMeta = MenuInventoryData.MultipleIDBlockBreakToggleMeta(playerdata, itemmeta)
            } else {
              player.sendMessage("整地レベルが足りません")
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
            }
          }

          Material.DIAMOND_AXE => {
            playerdata.chestflag = false
            player.sendMessage(GREEN.toString() + "スキルでのチェスト破壊を無効化しました。")
            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5.toFloat())
            player.openInventory(MenuInventoryData.passiveSkillMenuData(player))
          }

          Material.CHEST => {
            playerdata.chestflag = true
            player.sendMessage(RED.toString() + "スキルでのチェスト破壊を有効化しました。")
            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
            player.openInventory(MenuInventoryData.passiveSkillMenuData(player))
          }

          Material.STICK => {
            player.sendMessage(WHITE.toString() + "パッシブスキル:" + YELLOW + "" + UNDERLINE + "" + BOLD + "Gigantic" + RED + UNDERLINE + "" + BOLD + "Berserk" + WHITE + "はレベル10以上から使用可能です")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          }

          Material.WOOD_SWORD, Material.STONE_SWORD, Material.GOLD_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD => {
            if (playerdata.giganticBerserk.canEvolve) {
              player.openInventory(MenuInventoryData.giganticBerserkEvolutionMenu(player))
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5.toFloat())
            } else {
              player.sendMessage(RED.toString() + "進化条件を満たしていません")
            }
          }

          else => {

          }
        }
      }
    }
  }

  //スキルメニューの処理
  @EventHandler
  def onPlayerClickActiveSkillSellectEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory === null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }


    val topinventory = view.topInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズが45でない時終了
    if (topinventory.row != 5) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]

    //経験値変更用のクラスを設定
    val expman = ExperienceManager(player)


    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "整地スキル選択") {
      val isSkull = itemstackcurrent.getType === Material.SKULL_ITEM

      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.getType === InventoryType.PLAYER) {
        return
      }

      //ARROWSKILL
      ActiveSkill.ARROW.gettypenum().let { type =>
        (4..9).forEach { skilllevel =>
          val name = ActiveSkill.ARROW.getName(skilllevel)
          if (itemstackcurrent.getType == ActiveSkill.ARROW.getMaterial(skilllevel)) {
            val potionmeta = itemstackcurrent.itemMeta.asInstanceOf[PotionMeta]
            if (potionmeta.basePotionData.getType == ActiveSkill.ARROW.getPotionType(skilllevel)) {
              if (playerdata.activeskilldata.skilltype == type && playerdata.activeskilldata.skillnum == skilllevel) {
                player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
                player.sendMessage(s"${YELLOW}選択を解除しました")
                playerdata.activeskilldata.skilltype = 0
                playerdata.activeskilldata.skillnum = 0
              } else {
                playerdata.activeskilldata.updateSkill(player, type, skilllevel, 1)
                player.sendMessage(s"${GREEN}アクティブスキル:$name  が選択されました")
                player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
              }
            }
          }
        }
      }

      //MULTISKILL
      ActiveSkill.MULTI.gettypenum().let { type =>
        (4..9).forEach { skilllevel =>
          val name = ActiveSkill.MULTI.getName(skilllevel)
          if (itemstackcurrent.getType == ActiveSkill.MULTI.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.skilltype == type && playerdata.activeskilldata.skillnum == skilllevel) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(s"${YELLOW}選択を解除しました")
              playerdata.activeskilldata.skilltype = 0
              playerdata.activeskilldata.skillnum = 0
            } else {
              playerdata.activeskilldata.updateSkill(player, type, skilllevel, 1)
              player.sendMessage(s"${GREEN}アクティブスキル:$name  が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      //BREAKSKILL
      ActiveSkill.BREAK.gettypenum().let { type =>
        (1..9).forEach { skilllevel =>
          val name = ActiveSkill.BREAK.getName(skilllevel)
          if (itemstackcurrent.getType == ActiveSkill.BREAK.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.skilltype == type && playerdata.activeskilldata.skillnum == skilllevel) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(s"${YELLOW}選択を解除しました")
              playerdata.activeskilldata.skilltype = 0
              playerdata.activeskilldata.skillnum = 0
            } else {
              playerdata.activeskilldata.updateSkill(player, type, skilllevel, 1)
              player.sendMessage(s"${GREEN}アクティブスキル:$name  が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      //CONDENSKILL
      //WATER
      ActiveSkill.WATERCONDENSE.gettypenum().let { type =>
        (7..9).forEach { skilllevel =>
          val name = ActiveSkill.WATERCONDENSE.getName(skilllevel)
          if (itemstackcurrent.getType == ActiveSkill.WATERCONDENSE.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.assaulttype == type && playerdata.activeskilldata.assaultnum == skilllevel) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(s"${YELLOW}選択を解除しました")
              playerdata.activeskilldata.assaulttype = 0
              playerdata.activeskilldata.assaultnum = 0
            } else {
              playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
              player.sendMessage(s"${DARK_GREEN}アサルトスキル:$name  が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      //LAVA
      ActiveSkill.LAVACONDENSE.gettypenum().let { type =>
        (7..9).forEach { skilllevel =>
          val name = ActiveSkill.LAVACONDENSE.getName(skilllevel)
          if (itemstackcurrent.getType == ActiveSkill.LAVACONDENSE.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.assaulttype == type && playerdata.activeskilldata.assaultnum == skilllevel) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(s"${YELLOW}選択を解除しました")
              playerdata.activeskilldata.assaulttype = 0
              playerdata.activeskilldata.assaultnum = 0
            } else {
              playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
              player.sendMessage(s"${DARK_GREEN}アサルトスキル:$name  が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      ActiveSkill.FLUIDCONDENSE.gettypenum().let { type =>
        (10).let { skilllevel =>
          if (itemstackcurrent.getType == ActiveSkill.FLUIDCONDENSE.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.assaultnum == skilllevel && playerdata.activeskilldata.assaulttype == type) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(s"${YELLOW}選択を解除しました")
              playerdata.activeskilldata.assaulttype = 0
              playerdata.activeskilldata.assaultnum = 0
            } else {
              playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
              player.sendMessage(s"${DARK_GREEN}アサルトスキル:ヴェンダー・ブリザード が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      //アサルトアーマー
      ActiveSkill.ARMOR.gettypenum().let { type =>
        (10).let { skilllevel =>
          if (itemstackcurrent.getType == ActiveSkill.ARMOR.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.assaultnum == skilllevel && playerdata.activeskilldata.assaulttype == type) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(s"${YELLOW}選択を解除しました")
              playerdata.activeskilldata.assaulttype = 0
              playerdata.activeskilldata.assaultnum = 0
            } else {
              playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
              player.sendMessage(s"${DARK_GREEN}アサルトスキル:アサルト・アーマー が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else {
        when (itemstackcurrent.getType) {
          Material.STONE_BUTTON => {
            if (itemstackcurrent.itemMeta.displayName.contains("リセット")) {
              //経験値変更用のクラスを設定
              //経験値が足りなかったら処理を終了
              if (!expman.hasExp(10000)) {
                player.sendMessage(RED.toString() + "必要な経験値が足りません")
                player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
                return
              }
              //経験値消費
              expman.changeExp(-10000)

              //リセット処理
              playerdata.activeskilldata.reset()
              //スキルポイント更新
              playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
              //リセット音を流す
              player.playSound(player.location, Sound.ITEM_BOTTLE_FILL_DRAGONBREATH, 1f, 0.1.toFloat())
              //メッセージを流す
              player.sendMessage(LIGHT_PURPLE.toString() + "アクティブスキルポイントをリセットしました")
              //メニューを開く
              player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
            }
          }

          Material.GLASS => {
            if (playerdata.activeskilldata.skilltype == 0 && playerdata.activeskilldata.skillnum == 0
                && playerdata.activeskilldata.assaulttype == 0 && playerdata.activeskilldata.assaultnum == 0) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(YELLOW.toString() + "既に全ての選択は削除されています")
            } else {
              playerdata.activeskilldata.clearSelection(player)
            }
          }

          Material.BOOKSHELF => {
            //開く音を再生
            player.playSound(player.location, Sound.BLOCK_BREWING_STAND_BREW, 1f, 0.5.toFloat())
            player.openInventory(MenuInventoryData.activeSkillEffectMenuData(player))
          }
        }
      }
    }
  }

  //スキルエフェクトメニューの処理 + エフェクト開放の処理
  @EventHandler
  def onPlayerClickActiveSkillEffectSellectEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }
    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズ終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]

    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "整地スキルエフェクト選択") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.getType === InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.getType === Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      val currentType = itemstackcurrent.getType
      if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
        //開く音を再生
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1.toFloat())
        player.openInventory(ActiveSkillInventoryData.activeSkillMenuData(player))
        return
      } else if (currentType === Material.GLASS) {
        if (playerdata.activeskilldata.effectnum == 0) {
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          player.sendMessage(YELLOW.toString() + "既に選択されています")
        } else {
          playerdata.activeskilldata.effectnum = 0
          player.sendMessage(GREEN.toString() + "エフェクト:未設定  が選択されました")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
        }
        return
      } else if (currentType === Material.BOOK_AND_QUILL) {
        //開く音を再生
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.buyRecordMenuData(player))
        return
      } else {
        val skilleffect = ActiveSkillEffect.values()
        for (activeSkillEffect in skilleffect) {
          if (currentType === activeSkillEffect.material) {
            if (playerdata.activeskilldata.effectnum == activeSkillEffect.num) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(YELLOW.toString() + "既に選択されています")
            } else {
              playerdata.activeskilldata.effectnum = activeSkillEffect.num
              player.sendMessage(GREEN.toString() + "エフェクト:" + activeSkillEffect.nameOnUI + RESET + "" + GREEN + " が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
        val premiumeffect = ActiveSkillPremiumEffect.values()
        for (activeSkillPremiumEffect in premiumeffect) {
          if (currentType === activeSkillPremiumEffect.material) {
            if (playerdata.activeskilldata.effectnum == activeSkillPremiumEffect.num) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(YELLOW.toString() + "既に選択されています")
            } else {
              playerdata.activeskilldata.effectnum = activeSkillPremiumEffect.num + 100
              player.sendMessage(GREEN.toString() + "" + BOLD + "プレミアムエフェクト:" + activeSkillPremiumEffect.desc + RESET + "" + GREEN + "" + BOLD + " が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }


      //ここからエフェクト開放の処理
      if (currentType === Material.BEDROCK) {
        val itemmeta = itemstackcurrent.itemMeta
        val skilleffect = ActiveSkillEffect.values()
        for (activeSkillEffect in skilleffect) {
          if (activeSkillEffect.nameOnUI in itemmeta.displayName) {
            if (playerdata.activeskilldata.effectpoint < activeSkillEffect.usePoint) {
              player.sendMessage(DARK_RED.toString() + "エフェクトポイントが足りません")
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat())
            } else {
              playerdata.activeskilldata.obtainedSkillEffects.add(activeSkillEffect)
              player.sendMessage(LIGHT_PURPLE.toString() + "エフェクト：" + activeSkillEffect.nameOnUI + RESET + "" + LIGHT_PURPLE + "" + BOLD + "" + " を解除しました")
              player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
              playerdata.activeskilldata.effectpoint -= activeSkillEffect.usePoint
              player.openInventory(MenuInventoryData.activeSkillEffectMenuData(player))
            }
          }
        }
      }

      //ここからプレミアムエフェクト開放の処理
      if (currentType === Material.BEDROCK) {
        val itemmeta = itemstackcurrent.itemMeta
        val premiumeffect = ActiveSkillPremiumEffect.values()
        for (activeSkillPremiumEffect in premiumeffect) {
          if (activeSkillPremiumEffect.desc in itemmeta.displayName) {
            if (playerdata.activeskilldata.premiumeffectpoint < activeSkillPremiumEffect.usePoint) {
              player.sendMessage(DARK_RED.toString() + "プレミアムエフェクトポイントが足りません")
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.5f)
            } else {
              playerdata.activeskilldata.obtainedSkillPremiumEffects.add(activeSkillPremiumEffect)
              player.sendMessage(LIGHT_PURPLE.toString() + "" + BOLD + "プレミアムエフェクト：" + activeSkillPremiumEffect.desc + RESET + "" + LIGHT_PURPLE + "" + BOLD + "" + " を解除しました")
              if (databaseGateway.donateDataManipulator.addPremiumEffectBuy(playerdata, activeSkillPremiumEffect) === Fail) {
                player.sendMessage("購入履歴が正しく記録されませんでした。管理者に報告してください。")
              }
              player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2f)
              playerdata.activeskilldata.premiumeffectpoint -= activeSkillPremiumEffect.usePoint
              player.openInventory(MenuInventoryData.activeSkillEffectMenuData(player))
            }
          }
        }
      }
    }
  }

  //スキル解放の処理
  @EventHandler
  def onPlayerClickActiveSkillReleaseEvent(event: InventoryClickEvent) {
    OnActiveSkillUnselect.onPlayerClickActiveSkillReleaseEvent(event)
  }

  //ランキングメニュー
  @EventHandler
  def onPlayerClickSeichiRankingMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]

    val isSkull = itemstackcurrent.getType === Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "整地神ランキング") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.getType === InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull) {
        // safe cast
        val skullMeta = itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]
        val name = skullMeta.displayName
        when (skullMeta.owner) {
          "MHF_ArrowLeft" => {
            GlobalScope.launch(Schedulers.async) {
              sequentialEffect(
                  CommonSoundEffects.menuTransitionFenceSound,
                  StickMenu.firstPage.open
              ).runFor(player)
            }
          }

          "MHF_ArrowDown" => {
            itemstackcurrent.itemMeta
            if (name.contains("整地神ランキング") && name.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(name.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.rankingList(page_display - 1))
            }
          }

          "MHF_ArrowUp" => {
            itemstackcurrent.itemMeta
            if (name.contains("整地神ランキング") && name.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(name.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.rankingList(page_display - 1))
            }
          }

          else => {
            // NOP
          }
        }
      }
    }
  }

  //ランキングメニュー
  @EventHandler
  def onPlayerClickSeichiRankingMenuEvent1(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]

    val isSkull = itemstackcurrent.getType === Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "ログイン神ランキング") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.getType === InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowDown") {
        val itemmeta = itemstackcurrent.itemMeta
        if (itemmeta.displayName.contains("ログイン神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
          val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

          //開く音を再生
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.rankingList_playtick(page_display - 1))
        }
      } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowUp") {
        val itemmeta = itemstackcurrent.itemMeta
        if (itemmeta.displayName.contains("ログイン神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
          val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

          //開く音を再生
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.rankingList_playtick(page_display - 1))
        }
      }
    }
  }

  //ランキングメニュー
  @EventHandler
  def onPlayerClickSeichiRankingMenuEvent2(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]

    val isSkull = itemstackcurrent.getType === Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "投票神ランキング") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.getType === InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull) {
        val skullMeta = (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta])
        when (skullMeta.owner) {
          "MHF_ArrowLeft" => {
            GlobalScope.launch(Schedulers.async) {
              sequentialEffect(
                  CommonSoundEffects.menuTransitionFenceSound,
                  StickMenu.firstPage.open
              ).runFor(player)
            }
          }

          "MHF_ArrowDown" => {
            val itemmeta = itemstackcurrent.itemMeta
            if (itemmeta.displayName.contains("投票神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.rankingList_p_vote(page_display - 1))
            }
          }

          "MHF_ArrowUp" => {
            val itemmeta = itemstackcurrent.itemMeta
            if (itemmeta.displayName.contains("投票神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.rankingList_p_vote(page_display - 1))
            }
          }
        }
      }
    }
  }

  //ランキングメニュー
  @EventHandler
  def onOpenDonationRanking(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]

    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "寄付神ランキング") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.getType === InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.getType === Material.SKULL_ITEM
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull) {
        val skullMeta = itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]
        val name = skullMeta.displayName
        when(skullMeta.owner) {
          "MHF_ArrowLeft" => {
            GlobalScope.launch(Schedulers.async) {
              sequentialEffect(
                  CommonSoundEffects.menuTransitionFenceSound,
                  StickMenu.firstPage.open
              ).runFor(player)
            }
          }

          "MHF_ArrowDown" => {
            if (name.contains("寄付神ランキング") && name.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(name.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.rankingList_premiumeffectpoint(page_display - 1))
            }
          }

          "MHF_ArrowUp" => {
            if (name.contains("寄付神ランキング") && name.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(name.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.rankingList_premiumeffectpoint(page_display - 1))
            }
          }
        }
      }
    }
  }

  //購入履歴メニュー
  @EventHandler
  def onPlayerClickPremiumLogMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 4) {
      return
    }
    val player = he.asInstanceOf[Player]

    //インベントリ名が以下の時処理
    if (topinventory.title == BLUE.toString() + "" + BOLD + "プレミアムエフェクト購入履歴") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.getType === InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.getType === Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
        //開く音を再生
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.activeSkillEffectMenuData(player))
      }
    }
  }

  //ガチャ交換システム
  @EventHandler
  def onGachaTradeEvent(event: InventoryCloseEvent) {
    val player = event.player.asInstanceOf[Player]
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid].ifNull { return }
    //エラー分岐
    val name = playerdata.lowercaseName
    val inventory = event.inventory

    //インベントリサイズが36でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.title == LIGHT_PURPLE.toString() + "" + BOLD + "交換したい景品を入れてください") {
      var givegacha = 0
      /*
			 * step1 for文でinventory内に対象商品がないか検索
			 * あったらdurabilityに応じてgivegachaを増やし、非対象商品は返却boxへ
			 */
      //ガチャ景品交換インベントリの中身を取得
      val item = inventory.contents
      //ドロップ用アイテムリスト(返却box)作成
      val dropitem = ArrayList[ItemStack]()
      //カウント用
      var big = 0
      var reg = 0
      //for文で１個ずつ対象アイテムか見る
      //ガチャ景品交換インベントリを一個ずつ見ていくfor文
      for (m in item) {
        //無いなら次へ
        if (m == null) {
          continue
        } else if (SeichiAssist.gachamente) {
          //ガチャシステムメンテナンス中は全て返却する
          dropitem.add(m)
          continue
        } else if (!m.hasItemMeta()) {
          //丁重にお返しする
          dropitem.add(m)
          continue
        } else if (!m.itemMeta.hasLore()) {
          //丁重にお返しする
          dropitem.add(m)
          continue
        } else if (m.getType === Material.SKULL_ITEM) {
          //丁重にお返しする
          dropitem.add(m)
          continue
        }
        //ガチャ景品リストにアイテムがあった時にtrueになるフラグ
        var flag = false
        //ガチャ景品リストを一個ずつ見ていくfor文
        for (gachadata in gachaDataList) {
          if (!gachadata.itemStack.hasItemMeta()) {
            continue
          } else if (!gachadata.itemStack.itemMeta.hasLore()) {
            continue
          }
          //ガチャ景品リストにある商品の場合(Lore=説明文と表示名で判別),無い場合はアイテム返却
          if (gachadata.compare(m, name)) {
            if (SeichiAssist.DEBUG) {
              player.sendMessage(gachadata.itemStack.itemMeta.displayName)
            }
            flag = true
            val amount = m.amount
            if (gachadata.probability < 0.001) {
              //ギガンティック大当たりの部分
              //ガチャ券に交換せずそのままアイテムを返す
              dropitem.add(m)
            } else if (gachadata.probability < 0.01) {
              //大当たりの部分
              givegacha += 12 * amount
              big++
            } else if (gachadata.probability < 0.1) {
              //当たりの部分
              givegacha += 3 * amount
              reg++
            } else {
              //それ以外アイテム返却(経験値ポーションとかがここにくるはず)
              dropitem.add(m)
            }
            break
          }
        }
        //ガチャ景品リストに対象アイテムが無かった場合
        if (!flag) {
          //丁重にお返しする
          dropitem.add(m)
        }
      }
      //ガチャシステムメンテナンス中は全て返却する
      if (SeichiAssist.gachamente) {
        player.sendMessage(RED.toString() + "ガチャシステムメンテナンス中の為全てのアイテムを返却します")
      } else if (big <= 0 && reg <= 0) {
        player.sendMessage(YELLOW.toString() + "景品を認識しませんでした。全てのアイテムを返却します")
      } else {
        player.sendMessage(GREEN.toString() + "大当たり景品を" + big + "個、当たり景品を" + reg + "個認識しました")
      }
      /*
			 * step2 非対象商品をインベントリに戻す
			 */
      for (m in dropitem) {
        if (!Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, m)
        } else {
          Util.dropItem(player, m)
        }
      }
      /*
			 * step3 ガチャ券をインベントリへ
			 */
      val skull = Util.exchangeskull(player.name)
      var count = 0
      while (givegacha > 0) {
        if (player.inventory.contains(skull) || !Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, skull)
        } else {
          Util.dropItem(player, skull)
        }
        givegacha--
        count++
      }
      if (count > 0) {
        player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
        player.sendMessage(GREEN.toString() + "" + count + "枚の" + GOLD + "ガチャ券" + WHITE + "を受け取りました")
      }
    }

  }

  //実績メニューの処理
  @EventHandler
  def onPlayerClickTitleMenuEvent(event: InventoryClickEvent) {
    OnClickTitleMenu.onPlayerClickTitleMenuEvent(event)
  }

  //鉱石・交換券変換システム
  @EventHandler
  def onOreTradeEvent(event: InventoryCloseEvent) {
    val player = event.player.asInstanceOf[Player]

    //エラー分岐
    val inventory = event.inventory

    //インベントリサイズが36でない時終了
    if (inventory.row != 4) return

    if (inventory.title != s"$LIGHT_PURPLE${BOLD}交換したい鉱石を入れてください") return

    /*
     * step1 for文でinventory内の対象商品の個数を計算
     * 非対象商品は返却boxへ
     */

    val requiredAmountPerTicket = mapOf(
        Material.COAL_ORE to 128,
        Material.IRON_ORE to 64,
        Material.GOLD_ORE to 8,
        Material.LAPIS_ORE to 8,
        Material.DIAMOND_ORE to 4,
        Material.REDSTONE_ORE to 32,
        Material.EMERALD_ORE to 4,         
        Material.QUARTZ_ORE to 16
    )

    val inventoryContents = inventory.contents.filterNotNull()

    val (itemsToExchange, rejectedItems) =
        inventoryContents
            .partition { it.getType in requiredAmountPerTicket }

    val exchangingAmount = itemsToExchange
        .groupBy { it.getType }
        .mapValues { (_, stacks) => stacks.map { it.amount }.sum() }

    val ticketAmount = exchangingAmount
        .map { (material, amount) => amount / requiredAmountPerTicket[material] }
        .sum()

    //プレイヤー通知
    if (ticketAmount == 0) {
      player.sendMessage(s"${YELLOW}鉱石を認識しなかったか数が不足しています。全てのアイテムを返却します")
    } else {
      player.sendMessage(s"${DARK_RED}交換券$RESET${GREEN}を${ticketAmount}枚付与しました")
    }

    /*
     * step2 交換券をインベントリへ
     */
    val exchangeTicket = run {
      ItemStack(Material.PAPER).apply {
        itemMeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER).apply {
          displayName = s"$DARK_RED${BOLD}交換券"
          addEnchant(Enchantment.PROTECTION_FIRE, 1, false)
          addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }
      }
    }

    repeat(ticketAmount) {
      Util.addItemToPlayerSafely(player, exchangeTicket)
    }

    if (ticketAmount > 0) {
      player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
      player.sendMessage(s"${GREEN}交換券の付与が終わりました")
    }

    /*
     * step3 非対象・余剰鉱石の返却
     */
    val itemStacksToReturn =
        exchangingAmount
            .mapNotNull { (exchangedMaterial, exchangedAmount) =>
              val returningAmount = exchangedAmount % requiredAmountPerTicket[exchangedMaterial]

              if (returningAmount != 0)
                ItemStack(exchangedMaterial).apply { amount = returningAmount }
              else
                null
            } + rejectedItems

    //返却処理
    itemStacksToReturn.forEach { itemStack =>
      Util.addItemToPlayerSafely(player, itemStack)
    }
  }

  //ギガンティック→椎名林檎交換システム
  @EventHandler
  def onGachaRingoEvent(event: InventoryCloseEvent) {
    val player = event.player.asInstanceOf[Player]
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid].ifNull { return }
    //エラー分岐
    val name = playerdata.lowercaseName
    val inventory = event.inventory

    //インベントリサイズが4列でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.title == GOLD.toString() + "" + BOLD + "椎名林檎と交換したい景品を入れてネ") {
      var giveringo = 0
      /*
			 * step1 for文でinventory内に対象商品がないか検索
			 * あったらdurabilityに応じてgivegachaを増やし、非対象商品は返却boxへ
			 */
      //ガチャ景品交換インベントリの中身を取得
      val item = inventory.contents
      //ドロップ用アイテムリスト(返却box)作成
      val dropitem = ArrayList[ItemStack]()
      //カウント用
      var giga = 0
      //for文で１個ずつ対象アイテムか見る
      //ガチャ景品交換インベントリを一個ずつ見ていくfor文
      for (m in item) {
        //無いなら次へ
        if (m == null) {
          continue
        } else if (SeichiAssist.gachamente) {
          //ガチャシステムメンテナンス中は全て返却する
          dropitem.add(m)
          continue
        } else if (!m.hasItemMeta()) {
          //丁重にお返しする
          dropitem.add(m)
          continue
        } else if (!m.itemMeta.hasLore()) {
          //丁重にお返しする
          dropitem.add(m)
          continue
        } else if (m.getType === Material.SKULL_ITEM) {
          //丁重にお返しする
          dropitem.add(m)
          continue
        }
        //ガチャ景品リストにアイテムがあった時にtrueになるフラグ
        var flag = false
        //ガチャ景品リストを一個ずつ見ていくfor文
        for (gachadata in gachaDataList) {
          if (!gachadata.itemStack.hasItemMeta()) {
            continue
          } else if (!gachadata.itemStack.itemMeta.hasLore()) {
            continue
          }
          //ガチャ景品リストにある商品の場合(Lore=説明文と表示名で判別),無い場合はアイテム返却
          if (gachadata.compare(m, name)) {
            if (SeichiAssist.DEBUG) {
              player.sendMessage(gachadata.itemStack.itemMeta.displayName)
            }
            flag = true
            val amount = m.amount
            if (gachadata.probability < 0.001) {
              //ギガンティック大当たりの部分
              //1個につき椎名林檎n個と交換する
              giveringo += SeichiAssist.seichiAssistConfig.rateGiganticToRingo() * amount
              giga++
            } else {
              //それ以外アイテム返却
              dropitem.add(m)
            }
            break
          }
        }
        //ガチャ景品リストに対象アイテムが無かった場合
        if (!flag) {
          //丁重にお返しする
          dropitem.add(m)
        }
      }
      //ガチャシステムメンテナンス中は全て返却する
      if (SeichiAssist.gachamente) {
        player.sendMessage(RED.toString() + "ガチャシステムメンテナンス中の為全てのアイテムを返却します")
      } else if (giga <= 0) {
        player.sendMessage(YELLOW.toString() + "ギガンティック大当り景品を認識しませんでした。全てのアイテムを返却します")
      } else {
        player.sendMessage(GREEN.toString() + "ギガンティック大当り景品を" + giga + "個認識しました")
      }
      /*
			 * step2 非対象商品をインベントリに戻す
			 */
      for (m in dropitem) {
        if (!Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, m)
        } else {
          Util.dropItem(player, m)
        }
      }
      /*
			 * step3 椎名林檎をインベントリへ
			 */
      val ringo = StaticGachaPrizeFactory.maxRingo(player.name)
      var count = 0
      while (giveringo > 0) {
        if (player.inventory.contains(ringo) || !Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, ringo)
        } else {
          Util.dropItem(player, ringo)
        }
        giveringo--
        count++
      }
      if (count > 0) {
        player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
        player.sendMessage(GREEN.toString() + "" + count + "個の" + GOLD + "椎名林檎" + WHITE + "を受け取りました")
      }
    }

  }

  @EventHandler
  def onTitanRepairEvent(event: InventoryCloseEvent) {
    val player = event.player.asInstanceOf[Player]
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid].ifNull { return }
    //エラー分岐
    val inventory = event.inventory

    //インベントリサイズが36でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.title == GOLD.toString() + "" + BOLD + "修繕したい限定タイタンを入れてネ") {
      //インベントリの中身を取得
      val item = inventory.contents

      var count = 0
      //for文で１個ずつ対象アイテムか見る
      //インベントリを一個ずつ見ていくfor文
      for (m in item) {
        //無いなら次へ
        if (m == null) {
          continue
        }
        if (m.itemMeta.hasLore()) {
          if (Util.isLimitedTitanItem(m)) {
            m.durability = 1.toShort()
            count++
          }
        }

        if (!Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, m)
        } else {
          Util.dropItem(player, m)
        }
      }
      if (count < 1) {
        player.sendMessage(GREEN.toString() + "限定タイタンを認識しませんでした。すべてのアイテムを返却します")
      } else {
        player.sendMessage(GREEN.toString() + "限定タイタンを" + count + "個認識し、修繕しました。")
      }
    }
  }

  //投票ptメニュー
  @EventHandler
  def onVotingMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズが4列でない時終了
    if (topinventory.row != 4) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]

    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "投票ptメニュー") {
      event.setCancelled(true)

      if (event.clickedInventory.getType === InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.getType === Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      //投票pt受取
      if (itemstackcurrent.getType === Material.DIAMOND) {
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
          val skull = Util.voteskull(player.name)
          for (i in 0..9) {
            if (player.inventory.contains(skull) || !Util.isPlayerInventoryFull(player)) {
              Util.addItem(player, skull)
            } else {
              Util.dropItem(player, skull)
            }
          }

          //ピッケルプレゼント処理(レベル50になるまで)
          if (playerdata.level < 50) {
            val pickaxe = ItemData.superPickaxe(1)
            if (Util.isPlayerInventoryFull(player)) {
              Util.dropItem(player, pickaxe)
            } else {
              Util.addItem(player, pickaxe)
            }
          }

          //投票ギフト処理(レベル50から)
          if (playerdata.level >= 50) {
            val gift = ItemData.votingGift(1)
            if (Util.isPlayerInventoryFull(player)) {
              Util.dropItem(player, gift)
            } else {
              Util.addItem(player, gift)
            }
          }
          //エフェクトポイント加算処理
          playerdata.activeskilldata.effectpoint += 10

          n--
          count++
        }

        player.sendMessage(GOLD.toString() + "投票特典" + WHITE + "(" + count + "票分)を受け取りました")
        player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)

        val itemmeta = itemstackcurrent.itemMeta
        itemstackcurrent.itemMeta = itemmeta
        player.openInventory(MenuInventoryData.votingMenuData(player))
      } else if (itemstackcurrent.getType === Material.BOOK_AND_QUILL) {
        // 投票リンク表示
        player.sendMessage(RED.toString() + "" + UNDERLINE + "https://minecraft.jp/servers/54d3529e4ddda180780041a7/vote")
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.closeInventory()
      } else if (isSkull && (itemstackcurrent.itemMeta.asInstanceOf[SkullMeta]).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              StickMenu.firstPage.open
          ).runFor(player)
        }
        // NOTE: WHEN
      } else if (itemstackcurrent.getType === Material.WATCH) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleVotingFairy = playerdata.toggleVotingFairy % 4 + 1
        player.openInventory(MenuInventoryData.votingMenuData(player))
      } else if (itemstackcurrent.getType === Material.PAPER) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleGiveApple = playerdata.toggleGiveApple % 4 + 1
        player.openInventory(MenuInventoryData.votingMenuData(player))
      } else if (itemstackcurrent.getType === Material.JUKEBOX) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleVFSound = !playerdata.toggleVFSound
        player.openInventory(MenuInventoryData.votingMenuData(player))
      } else if (itemstackcurrent.getType === Material.GHAST_TEAR) {
        player.closeInventory()

        //プレイヤーレベルが10に達していないとき
        if (playerdata.level < 10) {
          player.sendMessage(GOLD.toString() + "プレイヤーレベルが足りません")
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          return
        }

        //既に妖精召喚している場合終了
        if (playerdata.usingVotingFairy) {
          player.sendMessage(GOLD.toString() + "既に妖精を召喚しています")
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          return
        }

        //投票ptが足りない場合終了
        if (playerdata.activeskilldata.effectpoint < playerdata.toggleVotingFairy * 2) {
          player.sendMessage(GOLD.toString() + "投票ptが足りません")
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          return
        }

        VotingFairyListener.summon(player)
        player.closeInventory()
      } else if (itemstackcurrent.getType === Material.COMPASS) {
        VotingFairyTask.speak(player, "僕は" + Util.showHour(playerdata.votingFairyEndTime) + "には帰るよー。", playerdata.toggleVFSound)
        player.closeInventory()
      }//妖精召喚
      //妖精音トグル
      //妖精リンゴトグル
      //妖精時間トグル
      //棒メニューに戻る

    }
  }

  @EventHandler
  def onHomeMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory.ifNull { return }
    //インベントリが存在しない時終了
    //インベントリサイズが3列でない時終了
    if (topinventory.row != 3) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]
    val itemmeta = itemstackcurrent.itemMeta

    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "ホームメニュー") {
      event.setCancelled(true)

      if (event.clickedInventory.getType === InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      if (itemmeta.displayName.contains("ホームポイントにワープ")) {
        player.chat("/home")
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
      } else if (itemmeta.displayName.contains("ホームポイントを設定")) {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        playerdata.selectHomeNum = 0
        player.openInventory(MenuInventoryData.checkSetHomeMenuData(player))
      }
      for (x in 1..SeichiAssist.seichiAssistConfig.subHomeMax) {
        if (itemmeta.displayName.contains("サブホームポイント" + x + "にワープ")) {
          player.chat(s"/subhome warp $x")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        } else if (itemmeta.displayName.contains("サブホームポイント" + x + "の情報")) {
          player.chat(s"/subhome name $x")
          player.closeInventory()
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        } else if (itemmeta.displayName.contains("サブホームポイント" + x + "を設定")) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.selectHomeNum = x
          player.openInventory(MenuInventoryData.checkSetHomeMenuData(player))
        }
      }

    } else if (topinventory.title.contains("ホームポイントを変更しますか?")) {
      event.setCancelled(true)

      if (event.clickedInventory.getType === InventoryType.PLAYER) {
        return
      }

      if (itemmeta.displayName.contains("変更する")) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (playerdata.selectHomeNum == 0)
          player.chat("/sethome")
        else
          player.chat("/subhome set " + playerdata.selectHomeNum)
        player.closeInventory()
      } else if (itemmeta.displayName.contains("変更しない")) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.closeInventory()
      }
    }
  }

  @EventHandler
  def onGiganticBerserkMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType !== EntityType.PLAYER) {
      return
    }

    //インベントリが存在しない時終了
    val topinventory = view.topInventory.ifNull { return }

    //インベントリが6列でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]

    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "スキルを進化させますか?") {
      event.setCancelled(true)
      if (itemstackcurrent.getType == Material.NETHER_STAR) {
        playerdata.giganticBerserk = GiganticBerserk(0, 0, playerdata.giganticBerserk.stage + 1, false)
        player.playSound(player.location, Sound.BLOCK_END_GATEWAY_SPAWN, 1f, 0.5.toFloat())
        player.playSound(player.location, Sound.ENTITY_ENDERDRAGON_AMBIENT, 1f, 0.8.toFloat())
        player.openInventory(MenuInventoryData.giganticBerserkEvolution2Menu(player))
      }
    } else if (topinventory.title == LIGHT_PURPLE.toString() + "" + BOLD + "スキルを進化させました") {
      event.setCancelled(true)
    }
  }
}

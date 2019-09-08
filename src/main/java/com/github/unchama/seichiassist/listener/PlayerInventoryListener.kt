package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.*
import com.github.unchama.seichiassist.SeichiAssist.Companion.playermap
import com.github.unchama.seichiassist.achievement.SeichiAchievement
import com.github.unchama.seichiassist.data.ActiveSkillInventoryData
import com.github.unchama.seichiassist.data.ItemData
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.data.player.GiganticBerserk
import com.github.unchama.seichiassist.data.player.PlayerNickName
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.seichiassist.menus.stickmenu.firstPage
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.util.exp.ExperienceManager
import com.github.unchama.targetedeffect.sequentialEffect
import com.github.unchama.util.ActionStatus.Fail
import com.github.unchama.util.row
import com.google.common.io.ByteStreams
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatColor.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

class PlayerInventoryListener : Listener {
  private val playerMap = SeichiAssist.playermap
  private val gachaDataList = SeichiAssist.gachadatalist
  private val databaseGateway = SeichiAssist.databaseGateway

  //サーバー選択メニュー
  @EventHandler
  fun onPlayerClickServerSwitchMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 2) {
      return
    }
    val player = he as Player

    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_RED.toString() + "" + BOLD + "サーバーを選択してください") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type === InventoryType.PLAYER) {
        return
      }
      val meta = itemstackcurrent.itemMeta

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      val byteArrayDataOutput = ByteStreams
          .newDataOutput()
      //ページ変更処理
      val displayName = meta.displayName
      when {
        "アルカディアサーバー" in displayName -> {
          byteArrayDataOutput.writeUTF("Connect")
          byteArrayDataOutput.writeUTF("s1")
          player.sendPluginMessage(SeichiAssist.instance, "BungeeCord",
              byteArrayDataOutput.toByteArray())
        }
        "エデンサーバー" in displayName -> {
          byteArrayDataOutput.writeUTF("Connect")
          byteArrayDataOutput.writeUTF("s2")
          player.sendPluginMessage(SeichiAssist.instance, "BungeeCord",
              byteArrayDataOutput.toByteArray())
        }
        "ヴァルハラサーバー" in displayName -> {
          byteArrayDataOutput.writeUTF("Connect")
          byteArrayDataOutput.writeUTF("s3")
          player.sendPluginMessage(SeichiAssist.instance, "BungeeCord",
              byteArrayDataOutput.toByteArray())
        }
        "建築サーバー" in displayName -> {
          byteArrayDataOutput.writeUTF("Connect")
          byteArrayDataOutput.writeUTF("s8")
          player.sendPluginMessage(SeichiAssist.instance, "BungeeCord",
              byteArrayDataOutput.toByteArray())
        }
        "公共施設サーバー" in displayName -> {
          byteArrayDataOutput.writeUTF("Connect")
          byteArrayDataOutput.writeUTF("s7")
          player.sendPluginMessage(SeichiAssist.instance, "BungeeCord",
              byteArrayDataOutput.toByteArray())
        }
      }
    }
  }

  //追加!!!
  //スキルメニューの処理
  @EventHandler
  fun onPlayerClickPassiveSkillSellectEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory === null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 4) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]!!

    //経験値変更用のクラスを設定
    //ExperienceManager expman = new ExperienceManager(player);


    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "整地スキル切り替え") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type === InventoryType.PLAYER) {
        return
      }
      val isSkull = itemstackcurrent.type === Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      //ページ変更処理
      // ->
      // val swords = EnumSet.of(Material.WOOD_SWORD, Material.STONE_SWORD, Material.GOLD_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD)
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else {
        val type = itemstackcurrent.type
        when (type) {
          Material.DIAMOND_PICKAXE -> {
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

          Material.DIAMOND_AXE -> {
            playerdata.chestflag = false
            player.sendMessage(GREEN.toString() + "スキルでのチェスト破壊を無効化しました。")
            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5.toFloat())
            player.openInventory(MenuInventoryData.getPassiveSkillMenuData(player))
          }

          Material.CHEST -> {
            playerdata.chestflag = true
            player.sendMessage(RED.toString() + "スキルでのチェスト破壊を有効化しました。")
            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
            player.openInventory(MenuInventoryData.getPassiveSkillMenuData(player))
          }

          Material.STICK -> {
            player.sendMessage(WHITE.toString() + "パッシブスキル:" + YELLOW + "" + UNDERLINE + "" + BOLD + "Gigantic" + RED + UNDERLINE + "" + BOLD + "Berserk" + WHITE + "はレベル10以上から使用可能です")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          }

          Material.WOOD_SWORD, Material.STONE_SWORD, Material.GOLD_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD -> {
            if (playerdata.giganticBerserk.canEvolve) {
              player.openInventory(MenuInventoryData.getGiganticBerserkEvolutionMenu(player))
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5.toFloat())
            } else {
              player.sendMessage(RED.toString() + "進化条件を満たしていません")
            }
          }

          else -> {

          }
        }
      }
    }
  }

  //スキルメニューの処理
  @EventHandler
  fun onPlayerClickActiveSkillSellectEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory === null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }


    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが45でない時終了
    if (topinventory.row != 5) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]!!

    //経験値変更用のクラスを設定
    val expman = ExperienceManager(player)


    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "整地スキル選択") {
      val isSkull = itemstackcurrent.type === Material.SKULL_ITEM

      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type === InventoryType.PLAYER) {
        return
      }

      //ARROWSKILL
      ActiveSkill.ARROW.gettypenum().let { type ->
        (4..9).forEach { skilllevel ->
          val name = ActiveSkill.ARROW.getName(skilllevel)
          if (itemstackcurrent.type == ActiveSkill.ARROW.getMaterial(skilllevel)) {
            val potionmeta = itemstackcurrent.itemMeta as PotionMeta
            if (potionmeta.basePotionData.type == ActiveSkill.ARROW.getPotionType(skilllevel)) {
              if (playerdata.activeskilldata.skilltype == type && playerdata.activeskilldata.skillnum == skilllevel) {
                player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
                player.sendMessage("${YELLOW}選択を解除しました")
                playerdata.activeskilldata.skilltype = 0
                playerdata.activeskilldata.skillnum = 0
              } else {
                playerdata.activeskilldata.updateSkill(player, type, skilllevel, 1)
                player.sendMessage("${GREEN}アクティブスキル:$name  が選択されました")
                player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
              }
            }
          }
        }
      }

      //MULTISKILL
      ActiveSkill.MULTI.gettypenum().let { type ->
        (4..9).forEach { skilllevel ->
          val name = ActiveSkill.MULTI.getName(skilllevel)
          if (itemstackcurrent.type == ActiveSkill.MULTI.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.skilltype == type && playerdata.activeskilldata.skillnum == skilllevel) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage("${YELLOW}選択を解除しました")
              playerdata.activeskilldata.skilltype = 0
              playerdata.activeskilldata.skillnum = 0
            } else {
              playerdata.activeskilldata.updateSkill(player, type, skilllevel, 1)
              player.sendMessage("${GREEN}アクティブスキル:$name  が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      //BREAKSKILL
      ActiveSkill.BREAK.gettypenum().let { type ->
        (1..9).forEach { skilllevel ->
          val name = ActiveSkill.BREAK.getName(skilllevel)
          if (itemstackcurrent.type == ActiveSkill.BREAK.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.skilltype == type && playerdata.activeskilldata.skillnum == skilllevel) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage("${YELLOW}選択を解除しました")
              playerdata.activeskilldata.skilltype = 0
              playerdata.activeskilldata.skillnum = 0
            } else {
              playerdata.activeskilldata.updateSkill(player, type, skilllevel, 1)
              player.sendMessage("${GREEN}アクティブスキル:$name  が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      //CONDENSKILL
      //WATER
      ActiveSkill.WATERCONDENSE.gettypenum().let { type ->
        (7..9).forEach { skilllevel ->
          val name = ActiveSkill.WATERCONDENSE.getName(skilllevel)
          if (itemstackcurrent.type == ActiveSkill.WATERCONDENSE.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.assaulttype == type && playerdata.activeskilldata.assaultnum == skilllevel) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage("${YELLOW}選択を解除しました")
              playerdata.activeskilldata.assaulttype = 0
              playerdata.activeskilldata.assaultnum = 0
            } else {
              playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
              player.sendMessage("${DARK_GREEN}アサルトスキル:$name  が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      //LAVA
      ActiveSkill.LAVACONDENSE.gettypenum().let { type ->
        (7..9).forEach { skilllevel ->
          val name = ActiveSkill.LAVACONDENSE.getName(skilllevel)
          if (itemstackcurrent.type == ActiveSkill.LAVACONDENSE.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.assaulttype == type && playerdata.activeskilldata.assaultnum == skilllevel) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage("${YELLOW}選択を解除しました")
              playerdata.activeskilldata.assaulttype = 0
              playerdata.activeskilldata.assaultnum = 0
            } else {
              playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
              player.sendMessage("${DARK_GREEN}アサルトスキル:$name  が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      ActiveSkill.FLUIDCONDENSE.gettypenum().let { type ->
        (10).let { skilllevel ->
          if (itemstackcurrent.type == ActiveSkill.FLUIDCONDENSE.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.assaultnum == skilllevel && playerdata.activeskilldata.assaulttype == type) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage("${YELLOW}選択を解除しました")
              playerdata.activeskilldata.assaulttype = 0
              playerdata.activeskilldata.assaultnum = 0
            } else {
              playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
              player.sendMessage("${DARK_GREEN}アサルトスキル:ヴェンダー・ブリザード が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      //アサルトアーマー
      ActiveSkill.ARMOR.gettypenum().let { type ->
        (10).let { skilllevel ->
          if (itemstackcurrent.type == ActiveSkill.ARMOR.getMaterial(skilllevel)) {
            if (playerdata.activeskilldata.assaultnum == skilllevel && playerdata.activeskilldata.assaulttype == type) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage("${YELLOW}選択を解除しました")
              playerdata.activeskilldata.assaulttype = 0
              playerdata.activeskilldata.assaultnum = 0
            } else {
              playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
              player.sendMessage("${DARK_GREEN}アサルトスキル:アサルト・アーマー が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }

      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else {
        when (itemstackcurrent.type) {
          Material.STONE_BUTTON -> {
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
              player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
            }
          }

          Material.GLASS -> {
            if (playerdata.activeskilldata.skilltype == 0 && playerdata.activeskilldata.skillnum == 0
                && playerdata.activeskilldata.assaulttype == 0 && playerdata.activeskilldata.assaultnum == 0) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(YELLOW.toString() + "既に全ての選択は削除されています")
            } else {
              playerdata.activeskilldata.clearSelection(player)
            }
          }

          Material.BOOKSHELF -> {
            //開く音を再生
            player.playSound(player.location, Sound.BLOCK_BREWING_STAND_BREW, 1f, 0.5.toFloat())
            player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player))
          }
        }
      }
    }
  }

  //スキルエフェクトメニューの処理 + エフェクト開放の処理
  @EventHandler
  fun onPlayerClickActiveSkillEffectSellectEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }
    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズ終了
    if (topinventory.row != 6) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]!!

    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "整地スキルエフェクト選択") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type === InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.type === Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      val currentType = itemstackcurrent.type
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        //開く音を再生
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1.toFloat())
        player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
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
        player.openInventory(MenuInventoryData.getBuyRecordMenuData(player))
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
              player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player))
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
              player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player))
            }
          }
        }
      }
    }
  }

  //スキル解放の処理
  @EventHandler
  fun onPlayerClickActiveSkillReleaseEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 5) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]!!

    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "整地スキル選択") {
      event.isCancelled = true
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type === InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.type === Material.BEDROCK) {
        val itemmeta = itemstackcurrent.itemMeta
        val skilllevel: Int
        val skilltype: Int
        val name = itemmeta.displayName
        if (name.contains("エビフライ・ドライブ")) {
          skilllevel = 4
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ホーリー・ショット")) {
          skilllevel = 5
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ツァーリ・ボンバ")) {
          skilllevel = 6
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("アーク・ブラスト")) {
          skilllevel = 7
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ファンタズム・レイ")) {
          skilllevel = 8
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("スーパー・ノヴァ")) {
          skilllevel = 9
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString() + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(GOLD.toString() + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("トム・ボウイ")) {
          skilllevel = 4
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("サンダー・ストーム")) {
          skilllevel = 5
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("スターライト・ブレイカー")) {
          skilllevel = 6
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("アース・ディバイド")) {
          skilllevel = 7
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ヘヴン・ゲイボルグ")) {
          skilllevel = 8
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ディシジョン")) {
          skilllevel = 9
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString() + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(GOLD.toString() + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("デュアル・ブレイク")) {
          skilllevel = 1
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("トリアル・ブレイク")) {
          skilllevel = 2
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("エクスプロージョン")) {
          skilllevel = 3
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ミラージュ・フレア")) {
          skilllevel = 4
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ドッ・カーン")) {
          skilllevel = 5
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ギガンティック・ボム")) {
          skilllevel = 6
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ブリリアント・デトネーション")) {
          skilllevel = 7
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("レムリア・インパクト")) {
          skilllevel = 8
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("エターナル・ヴァイス")) {
          skilllevel = 9
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString() + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(GOLD.toString() + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ホワイト・ブレス")) {
          skilllevel = 7
          skilltype = 4
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.watercondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("アブソリュート・ゼロ")) {
          skilllevel = 8
          skilltype = 4
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.watercondenskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.watercondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ダイアモンド・ダスト")) {
          skilllevel = 9
          skilltype = 4
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.watercondenskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.watercondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString() + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(GOLD.toString() + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("ラヴァ・コンデンセーション")) {
          skilllevel = 7
          skilltype = 5
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.lavacondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }/*else if(playerdata.activeskilldata.condenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}*/
        } else if (name.contains("モエラキ・ボールダーズ")) {
          skilllevel = 8
          skilltype = 5
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.lavacondenskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.lavacondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("エルト・フェットル")) {
          skilllevel = 9
          skilltype = 5
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.lavacondenskill < skilllevel - 1) {
            player.sendMessage(DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.lavacondenskill = skilllevel
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(YELLOW.toString() + "" + BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(GOLD.toString() + "" + BOLD + playerdata.lowercaseName + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (name.contains("アサルト・アーマー")) {

        } else if (name.contains("ヴェンダー・ブリザード")) {
          if (playerdata.activeskilldata.skillpoint < 110) {
            player.sendMessage(DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.fluidcondenskill = 10
            player.sendMessage(AQUA.toString() + "" + BOLD + "" + "ヴェンダー・ブリザードを解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        }


      }
    }
  }

  //ランキングメニュー
  @EventHandler
  fun onPlayerClickSeichiRankingMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he as Player

    val isSkull = itemstackcurrent.type === Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "整地神ランキング") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type === InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull) {
        // safe cast
        val skullMeta = itemstackcurrent.itemMeta as SkullMeta
        val name = skullMeta.displayName
        when (skullMeta.owner) {
          "MHF_ArrowLeft" -> {
            GlobalScope.launch(Schedulers.async) {
              sequentialEffect(
                  CommonSoundEffects.menuTransitionFenceSound,
                  StickMenu.firstPage.open
              ).runFor(player)
            }
          }

          "MHF_ArrowDown" -> {
            itemstackcurrent.itemMeta
            if (name.contains("整地神ランキング") && name.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(name.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.getRankingList(page_display - 1))
            }
          }

          "MHF_ArrowUp" -> {
            itemstackcurrent.itemMeta
            if (name.contains("整地神ランキング") && name.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(name.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.getRankingList(page_display - 1))
            }
          }

          else -> {
            // NOP
          }
        }
      }
    }
  }

  //ランキングメニュー
  @EventHandler
  fun onPlayerClickSeichiRankingMenuEvent1(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he as Player

    val isSkull = itemstackcurrent.type === Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "ログイン神ランキング") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type === InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowDown") {
        val itemmeta = itemstackcurrent.itemMeta
        if (itemmeta.displayName.contains("ログイン神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
          val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

          //開く音を再生
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getRankingList_playtick(page_display - 1))
        }
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowUp") {
        val itemmeta = itemstackcurrent.itemMeta
        if (itemmeta.displayName.contains("ログイン神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
          val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

          //開く音を再生
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getRankingList_playtick(page_display - 1))
        }
      }
    }
  }

  //ランキングメニュー
  @EventHandler
  fun onPlayerClickSeichiRankingMenuEvent2(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he as Player

    val isSkull = itemstackcurrent.type === Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "投票神ランキング") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type === InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull) {
        val skullMeta = (itemstackcurrent.itemMeta as SkullMeta)
        when (skullMeta.owner) {
          "MHF_ArrowLeft" -> {
            GlobalScope.launch(Schedulers.async) {
              sequentialEffect(
                  CommonSoundEffects.menuTransitionFenceSound,
                  StickMenu.firstPage.open
              ).runFor(player)
            }
          }

          "MHF_ArrowDown" -> {
            val itemmeta = itemstackcurrent.itemMeta
            if (itemmeta.displayName.contains("投票神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.getRankingList_p_vote(page_display - 1))
            }
          }

          "MHF_ArrowUp" -> {
            val itemmeta = itemstackcurrent.itemMeta
            if (itemmeta.displayName.contains("投票神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.getRankingList_p_vote(page_display - 1))
            }
          }
        }
      }
    }
  }

  //ランキングメニュー
  @EventHandler
  fun onOpenDonationRanking(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが54でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he as Player

    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "寄付神ランキング") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type === InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.type === Material.SKULL_ITEM
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull) {
        val skullMeta = itemstackcurrent.itemMeta as SkullMeta
        val name = skullMeta.displayName
        when(skullMeta.owner) {
          "MHF_ArrowLeft" -> {
            GlobalScope.launch(Schedulers.async) {
              sequentialEffect(
                  CommonSoundEffects.menuTransitionFenceSound,
                  StickMenu.firstPage.open
              ).runFor(player)
            }
          }

          "MHF_ArrowDown" -> {
            if (name.contains("寄付神ランキング") && name.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(name.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.getRankingList_premiumeffectpoint(page_display - 1))
            }
          }

          "MHF_ArrowUp" -> {
            if (name.contains("寄付神ランキング") && name.contains("ページ目")) {//移動するページの種類を判定
              val page_display = Integer.parseInt(name.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

              //開く音を再生
              player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
              player.openInventory(MenuInventoryData.getRankingList_premiumeffectpoint(page_display - 1))
            }
          }
        }
      }
    }
  }

  //購入履歴メニュー
  @EventHandler
  fun onPlayerClickPremiumLogMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 4) {
      return
    }
    val player = he as Player

    //インベントリ名が以下の時処理
    if (topinventory.title == BLUE.toString() + "" + BOLD + "プレミアムエフェクト購入履歴") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type === InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.type === Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        //開く音を再生
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player))
      }
    }
  }

  //ガチャ交換システム
  @EventHandler
  fun onGachaTradeEvent(event: InventoryCloseEvent) {
    val player = event.player as Player
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid] ?: return
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
      val dropitem = ArrayList<ItemStack>()
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
        } else if (m.type === Material.SKULL_ITEM) {
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
      val skull = Util.getExchangeskull(Util.getName(player))
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
  fun onPlayerClickTitleMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    //インベントリを開けたのがプレイヤーではない時終了
    val view = event.view

    val he = view.player
    if (he.type !== EntityType.PLAYER) {
      return
    }

    //インベントリが存在しない時終了
    val topinventory = view.topInventory ?: return
    //インベントリサイズが36でない時終了
    if (topinventory.row != 4) {
      return
    }
    val itemstackcurrent = event.currentItem

    val player = he as Player
    val playerdata = playermap[player.uniqueId]!!

    fun setTitle(first: Int = 0, second: Int = 0, third: Int = 0, message: String =
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
    val isSkull = itemstackcurrent.type == Material.SKULL_ITEM
    val prefix = "$DARK_PURPLE$BOLD"
    when (title) {
      "${prefix}実績・二つ名システム" -> {
        event.isCancelled = true

        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */
        //val isSkull = current === Material.SKULL_ITEM

        //表示内容をLVに変更
        if (itemstackcurrent.type == Material.REDSTONE_TORCH_ON) {
          // Zero clear
          playerdata.updateNickname(style = PlayerNickName.Style.Level)
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getTitleMenuData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_Present2") {
          SeichiAchievement.tryAchieve(player, playerdata.giveachvNo)
          playerdata.giveachvNo = 0
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getTitleMenuData(player))
        } else if (itemstackcurrent.type == Material.ANVIL) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
        } else if (itemstackcurrent.type == Material.GOLD_PICKAXE) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.getTitleSeichi(player))
        } else if (itemstackcurrent.type == Material.COMPASS) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.getTitleLogin(player))
        } else if (itemstackcurrent.type == Material.BLAZE_POWDER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.getTitleSuperTry(player))
        } else if (itemstackcurrent.type == Material.EYE_OF_ENDER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.getTitleSpecial(player))
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
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
          player.openInventory(MenuInventoryData.getTitleBuild(player));
        }
        *///カテゴリ「ログイン」を開く
        //カテゴリ「整地」を開く
        //「二つ名組合せシステム」を開く
        //予約付与システム受け取り処理
      }

      "${prefix}カテゴリ「整地」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        //クリックしたボタンに応じた各処理内容の記述ここから

        //実績「整地量」
        if (itemstackcurrent.type === Material.IRON_PICKAXE) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleAmountData(player))
        }

        //実績「整地神ランキング」
        if (itemstackcurrent.type === Material.DIAMOND_PICKAXE) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleRankData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleMenuData(player))
          return
        }//実績メニューに戻る
      }

      "${prefix}カテゴリ「建築」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type == InventoryType.PLAYER) {
          return
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleMenuData(player))
          return
        }//クリックしたボタンに応じた各処理内容の記述ここから
        //実績未実装のカテゴリです。
        //実績メニューに戻る

      }

      "${prefix}カテゴリ「ログイン」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        // NOTE: WHEN
      } else if (itemstackcurrent.type === Material.COMPASS) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.getTitleTimeData(player))
        } else if (itemstackcurrent.type === Material.BOOK) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.getTitleJoinAmountData(player))
        } else if (itemstackcurrent.type === Material.BOOK_AND_QUILL) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.getTitleJoinChainData(player))
        } else if (itemstackcurrent.type === Material.NETHER_STAR) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.getTitleExtraData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleMenuData(player))
          return
        }//実績メニューに戻る
        //実績「記念日」を開く
        //実績「連続ログイン」を開く
        //実績「通算ログイン」を開く
        //クリックしたボタンに応じた各処理内容の記述ここから
        //実績「参加時間」を開く

      }

      "${prefix}カテゴリ「やりこみ」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleMenuData(player))
          return
        }//クリックしたボタンに応じた各処理内容の記述ここから
        //実績未実装のカテゴリです。
        //実績メニューに戻る

      }

      "${prefix}カテゴリ「特殊」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        } else if (itemstackcurrent.type === Material.BLAZE_POWDER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.getTitleEventData(player))
        } else if (itemstackcurrent.type === Material.YELLOW_FLOWER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.getTitleSupportData(player))
        } else if (itemstackcurrent.type === Material.DIAMOND_BARDING) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = 1
          player.openInventory(MenuInventoryData.getTitleSecretData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleMenuData(player))
          return
        }//実績メニューに戻る
        //実績「極秘任務」を開く
        //実績「JMS投票数」を開く
        //クリックしたボタンに応じた各処理内容の記述ここから
        //実績「公式イベント」を開く

      }

      "${prefix}二つ名組合せシステム" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */
when (itemstackcurrent.type) {
        //実績ポイント最新化
         Material.EMERALD_ORE -> {

          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.recalculateAchievePoint()
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
        }

        //エフェクトポイント→実績ポイント変換
        Material.EMERALD -> {
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
         Material.ITEM_FRAME -> {

          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setTitleShopData(player))
        }

        //前パーツ
         Material.WATER_BUCKET -> {

          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitle1Data(player))
        }

        //中パーツ
         Material.MILK_BUCKET -> {

          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitle2Data(player))
        }

        //後パーツ
         Material.LAVA_BUCKET -> {

          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitle3Data(player))}
         else -> {
          // NOP
        }
      }
        if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleMenuData(player))
          return
        }//実績メニューに戻る

      }

      "${prefix}二つ名組合せ「前」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        // NOTE: WHEN
      } else if (itemstackcurrent.type === Material.WATER_BUCKET) {
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
        } else if (itemstackcurrent.type === Material.GRASS) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.updateNickname(id1 = 0)
          player.sendMessage("前パーツの選択を解除しました。")
          return
        } else if (itemstackcurrent.type === Material.BARRIER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
          return
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowRight") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitle1Data(player))
          return
        }//次ページ
        //組み合わせメイン
        //パーツ未選択に


      }

      "${prefix}二つ名組合せ「中」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        // NOTE: WHEN
      } else if (itemstackcurrent.type === Material.MILK_BUCKET) {
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
        } else if (itemstackcurrent.type === Material.GRASS) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.updateNickname(id2 = 0)
          player.sendMessage("中パーツの選択を解除しました。")
          return
        } else if (itemstackcurrent.type === Material.BARRIER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
          return
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowRight") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitle2Data(player))
          return
        }//次ページ
        //組み合わせメインへ移動
        //パーツ未選択に


      }

      "${prefix}二つ名組合せ「後」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        // NOTE: when
      if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        } else if (itemstackcurrent.type === Material.LAVA_BUCKET) {
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
        } else if (itemstackcurrent.type === Material.GRASS) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.updateNickname(id3 = 0)
          player.sendMessage("後パーツの選択を解除しました。")
          return
        } else if (itemstackcurrent.type === Material.BARRIER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
          return
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowRight") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitle3Data(player))
          return
        }//次ページ
        //組み合わせメイン
        //パーツ未選択に


      }

      "${prefix}実績ポイントショップ" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        //実績ポイント最新化
        if (itemstackcurrent.type === Material.EMERALD_ORE) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.recalculateAchievePoint()
          playerdata.samepageflag = true
          player.openInventory(MenuInventoryData.setTitleShopData(player))
        }

        //購入処理
        if (itemstackcurrent.type === Material.BEDROCK) {
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


        } else if (itemstackcurrent.type === Material.BARRIER) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
          return
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowRight") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.setTitleShopData(player))
          return
        }//次ページ
        //組み合わせメイン

      }

      "${prefix}実績「整地神ランキング」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.type === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.getTitleRankData(player))
        } else if (itemstackcurrent.type === Material.DIAMOND_BLOCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemstackcurrent.itemMeta.displayName
          when {
            "No1001「" + SeichiAssist.seichiAssistConfig.getTitle1(1001) + "」" in name -> setTitle(first = 1001)
            "No1002「" + SeichiAssist.seichiAssistConfig.getTitle1(1002) + "」" in name -> setTitle(first = 1002)
            "No1003「" + SeichiAssist.seichiAssistConfig.getTitle1(1003) + "」" in name -> setTitle(first = 1003)
            "No1004「" + SeichiAssist.seichiAssistConfig.getTitle1(1004) + "」" in name -> setTitle(first = 1004)
            "No1010「" + SeichiAssist.seichiAssistConfig.getTitle1(1010) + "」" in name -> setTitle(first = 1010)
            ("No1011「" + SeichiAssist.seichiAssistConfig.getTitle1(1011)
                + SeichiAssist.seichiAssistConfig.getTitle2(9904) + SeichiAssist.seichiAssistConfig.getTitle3(1011) + "」") in name -> {
              setTitle(1011, 9904, 1011)
            }
            ("No1012「" + SeichiAssist.seichiAssistConfig.getTitle1(1012)
                + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(1012) + "」") in name -> {
              setTitle(1012, 9901, 1012)
            }
            ("No1005「" + SeichiAssist.seichiAssistConfig.getTitle1(1005)
                + SeichiAssist.seichiAssistConfig.getTitle3(1005) + "」") in name -> {
              setTitle(first = 1005, third = 1012)
            }
            ("No1006「" + SeichiAssist.seichiAssistConfig.getTitle1(1006) + "」") in name -> setTitle(first = 1006)
            ("No1007「" + SeichiAssist.seichiAssistConfig.getTitle1(1007)
                + SeichiAssist.seichiAssistConfig.getTitle2(9904) + SeichiAssist.seichiAssistConfig.getTitle3(1007) + "」") in name -> {
              setTitle(1007, 9904, 1007)
            }
            ("No1008「" + SeichiAssist.seichiAssistConfig.getTitle1(1008)
                + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(1008) + "」") in name -> {
              setTitle(1008, 9901, 1008)
            }
            ("No1009「" + SeichiAssist.seichiAssistConfig.getTitle1(1009)
                + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(1009) + "」") in name -> {
              setTitle(1009, 9909, 1009)
            }
          }
          player.openInventory(MenuInventoryData.getTitleRankData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleSeichi(player))
          return
        }//実績メニューに戻る
      }

      "${prefix}実績「整地量」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.type === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.getTitleAmountData(player))
        } else if (itemstackcurrent.type === Material.DIAMOND_BLOCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemstackcurrent.itemMeta.displayName
          when {
            "No3001「" + SeichiAssist.seichiAssistConfig.getTitle1(3001) + "」" in name -> setTitle(first = 3001)
            ("No3002「" + SeichiAssist.seichiAssistConfig.getTitle1(3002)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(3002) + "」") in name -> setTitle(3002, 9905, 3002)
            "No3003「" + SeichiAssist.seichiAssistConfig.getTitle1(3003) + "」" in name -> setTitle(first = 3003)
            ("No3004「" + SeichiAssist.seichiAssistConfig.getTitle1(3004)
                + SeichiAssist.seichiAssistConfig.getTitle2(9902) + "」") in name -> setTitle(first = 3004, second = 9902)
            ("No3005「" + SeichiAssist.seichiAssistConfig.getTitle1(3005)
                + SeichiAssist.seichiAssistConfig.getTitle3(3005) + "」") in name -> setTitle(first = 3005, third = 3005)
            "No3006「" + SeichiAssist.seichiAssistConfig.getTitle1(3006) + "」" in name -> setTitle(first = 3006)
            ("No3007「" + SeichiAssist.seichiAssistConfig.getTitle1(3007)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」") in name -> setTitle(first = 3007, second = 9905)
            "No3008「" + SeichiAssist.seichiAssistConfig.getTitle1(3008) + "」" in name -> setTitle(first = 3008)
            ("No3009「" + SeichiAssist.seichiAssistConfig.getTitle1(3009)
                + SeichiAssist.seichiAssistConfig.getTitle3(3009) + "」") in name -> setTitle(first = 3009, third = 3009)
            ("No3010「" + SeichiAssist.seichiAssistConfig.getTitle1(3010)
                + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(3010) + "」") in name -> setTitle(3010, 9909, 3010)
            "No3011「" + SeichiAssist.seichiAssistConfig.getTitle1(3011) + "」" in name -> setTitle(first = 3011)
            ("No3012「" + SeichiAssist.seichiAssistConfig.getTitle1(3012)
                + SeichiAssist.seichiAssistConfig.getTitle3(3012) + "」") in name -> setTitle(first = 3012, third = 3012)
            ("No3013「" + SeichiAssist.seichiAssistConfig.getTitle1(3013)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(3013) + "」") in name -> setTitle(3013, 9905, 3013)
            ("No3014「" + SeichiAssist.seichiAssistConfig.getTitle1(3014)
                + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(3014) + "」") in name -> setTitle(3014, 9909, 3014)
            "No3015「" + SeichiAssist.seichiAssistConfig.getTitle1(3015) + "」" in name -> setTitle(first = 3015)
            "No3016「" + SeichiAssist.seichiAssistConfig.getTitle1(3016) + "」" in name -> setTitle(first = 3016)
            "No3017「" + SeichiAssist.seichiAssistConfig.getTitle1(3017) + "」" in name -> setTitle(first = 3017)
            "No3018「" + SeichiAssist.seichiAssistConfig.getTitle1(3018) + "」" in name -> setTitle(first = 3018)
            "No3019「" + SeichiAssist.seichiAssistConfig.getTitle1(3019) + "」" in name -> setTitle(first = 3019)
          }
          player.openInventory(MenuInventoryData.getTitleAmountData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleSeichi(player))
          return
        }//実績メニューに戻る
      }

      "${prefix}実績「参加時間」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.type === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.getTitleTimeData(player))
        } else if (itemstackcurrent.type === Material.DIAMOND_BLOCK) {
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
          player.openInventory(MenuInventoryData.getTitleTimeData(player))
        } else if (itemstackcurrent.type === Material.EMERALD_BLOCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.TitleFlags.set(8003)
          player.sendMessage("お疲れ様でした！今日のお給料の代わりに二つ名をどうぞ！")
          player.openInventory(MenuInventoryData.getTitleTimeData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleLogin(player))
          return
        }//実績メニューに戻る
      }

      "${prefix}実績「通算ログイン」" -> {
        event.isCancelled = true

        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }
        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.type === Material.BEDROCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.getTitleJoinAmountData(player))
        } else if (itemstackcurrent.type === Material.DIAMOND_BLOCK) {
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

          player.openInventory(MenuInventoryData.getTitleJoinAmountData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleLogin(player))
          return
        }//実績メニューに戻る
      }

      "${prefix}実績「連続ログイン」" -> {
        event.isCancelled = true

        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.type === Material.BEDROCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.getTitleJoinChainData(player))
        } else if (itemstackcurrent.type === Material.DIAMOND_BLOCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemstackcurrent.itemMeta.displayName

          when {
            ("No5001「" + SeichiAssist.seichiAssistConfig.getTitle1(5001)
                + SeichiAssist.seichiAssistConfig.getTitle2(5001) + "」") in name -> setTitle(first = 5001, second = 5001)
            ("No5002「" + SeichiAssist.seichiAssistConfig.getTitle1(5002)
                + SeichiAssist.seichiAssistConfig.getTitle3(5002) + "」") in name -> setTitle(first = 5002, third = 5002)
            "No5003「" + SeichiAssist.seichiAssistConfig.getTitle1(5003) + "」" in name -> setTitle(first = 5003)
            ("No5004「" + SeichiAssist.seichiAssistConfig.getTitle1(5004)
                + SeichiAssist.seichiAssistConfig.getTitle3(5004) + "」") in name -> setTitle(first = 5004, third = 5004)
            ("No5005「" + SeichiAssist.seichiAssistConfig.getTitle1(5005)
                + SeichiAssist.seichiAssistConfig.getTitle3(5005) + "」") in name -> setTitle(first = 5005, third = 5005)
            ("No5006「" + SeichiAssist.seichiAssistConfig.getTitle1(5006)
                + SeichiAssist.seichiAssistConfig.getTitle3(5006) + "」") in name -> setTitle(first = 5006, third = 5006)
            "No5007「" + SeichiAssist.seichiAssistConfig.getTitle1(5007) + "」" in name -> setTitle(first = 5007)
            ("No5008「" + SeichiAssist.seichiAssistConfig.getTitle1(5008)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」") in name -> setTitle(first = 5008, second = 9905)
          }

          player.openInventory(MenuInventoryData.getTitleJoinChainData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f)
          player.openInventory(MenuInventoryData.getTitleLogin(player))
          return
        }//実績メニューに戻る
      }

      "${prefix}実績「JMS投票数」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.type === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
          player.openInventory(MenuInventoryData.getTitleSupportData(player))
        } else if (itemstackcurrent.type === Material.DIAMOND_BLOCK) {
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
          player.openInventory(MenuInventoryData.getTitleSupportData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleSpecial(player))
          return
        }//実績メニューに戻る
      }

      "${prefix}実績「公式イベント」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.type === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は配布解禁式です。運営チームからの配布タイミングを逃さないようご注意ください。")
          player.openInventory(MenuInventoryData.getTitleEventData(player))
        } else if (itemstackcurrent.type === Material.DIAMOND_BLOCK) {
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
          player.openInventory(MenuInventoryData.getTitleEventData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleSpecial(player))
          return
        }//実績メニューに戻る
      }

      "${prefix}実績「記念日」" -> {
        event.isCancelled = true

        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */
        if (itemstackcurrent.type === Material.BEDROCK) {
          val itemmeta = itemstackcurrent.itemMeta
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemmeta.displayName
          for (i in 9001..9036) {
            if ("No$i「???」" in name) {
              SeichiAchievement.tryAchieve(player, i)
            }
          }

          player.openInventory(MenuInventoryData.getTitleExtraData(player))
        } else if (itemstackcurrent.type === Material.DIAMOND_BLOCK) {
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
          player.openInventory(MenuInventoryData.getTitleExtraData(player))
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleLogin(player))
          return
        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowRight") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.titlepage = playerdata.titlepage + 1
          player.openInventory(MenuInventoryData.getTitleExtraData(player))
          return
        }//次ページ
        //実績メニューに戻る

      }

      "${prefix}実績「極秘任務」" -> {
        event.isCancelled = true

        //実績解除処理部分の読みこみ
        //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
        //プレイヤーインベントリのクリックの場合終了
        if (event.clickedInventory.type === InventoryType.PLAYER) {
          return
        }

        /*
         * クリックしたボタンに応じた各処理内容の記述ここから
         */

        if (itemstackcurrent.type === Material.BEDROCK) {
          //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.sendMessage("この実績は「極秘実績」です。いろいろやってみましょう！")
          player.openInventory(MenuInventoryData.getTitleSecretData(player))
        } else if (itemstackcurrent.type == Material.DIAMOND_BLOCK) {
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          val name = itemstackcurrent.itemMeta.displayName

          when {
            ("No8001「" + SeichiAssist.seichiAssistConfig.getTitle1(8001)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(8001) + "」") in name -> setTitle(8001, 9905, 8001)
            ("No8002「" + SeichiAssist.seichiAssistConfig.getTitle1(8002)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(8002) + "」") in name -> setTitle(8002, 9905, 8002)
            ("No8003「" + SeichiAssist.seichiAssistConfig.getTitle1(8003)
                + SeichiAssist.seichiAssistConfig.getTitle3(8003) + "」") in name -> setTitle(first = 8003, third = 8003)
          }
          player.openInventory(MenuInventoryData.getTitleSecretData(player))

        } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getTitleSpecial(player))
        }//実績メニューに戻る
      }
      else -> {
        // NOP
      }
    }
  }

  //鉱石・交換券変換システム
  @EventHandler
  fun onOreTradeEvent(event: InventoryCloseEvent) {
    val player = event.player as Player

    //エラー分岐
    val inventory = event.inventory

    //インベントリサイズが36でない時終了
    if (inventory.row != 4) return

    if (inventory.title != "$LIGHT_PURPLE${BOLD}交換したい鉱石を入れてください") return

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
            .partition { it.type in requiredAmountPerTicket }

    val exchangingAmount = itemsToExchange
        .groupBy { it.type }
        .mapValues { (_, stacks) -> stacks.map { it.amount }.sum() }

    val ticketAmount = exchangingAmount
        .map { (material, amount) -> amount / requiredAmountPerTicket[material]!! }
        .sum()

    //プレイヤー通知
    if (ticketAmount == 0) {
      player.sendMessage("${YELLOW}鉱石を認識しなかったか数が不足しています。全てのアイテムを返却します")
    } else {
      player.sendMessage("${DARK_RED}交換券$RESET${GREEN}を${ticketAmount}枚付与しました")
    }

    /*
     * step2 交換券をインベントリへ
     */
    val exchangeTicket = run {
      ItemStack(Material.PAPER).apply {
        itemMeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER).apply {
          displayName = "$DARK_RED${BOLD}交換券"
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
      player.sendMessage("${GREEN}交換券の付与が終わりました")
    }

    /*
     * step3 非対象・余剰鉱石の返却
     */
    val itemStacksToReturn =
        exchangingAmount
            .mapNotNull { (exchangedMaterial, exchangedAmount) ->
              val returningAmount = exchangedAmount % requiredAmountPerTicket[exchangedMaterial]!!

              if (returningAmount != 0)
                ItemStack(exchangedMaterial).apply { amount = returningAmount }
              else
                null
            } + rejectedItems

    //返却処理
    itemStacksToReturn.forEach { itemStack ->
      Util.addItemToPlayerSafely(player, itemStack)
    }
  }

  //ギガンティック→椎名林檎交換システム
  @EventHandler
  fun onGachaRingoEvent(event: InventoryCloseEvent) {
    val player = event.player as Player
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid] ?: return
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
      val dropitem = ArrayList<ItemStack>()
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
        } else if (m.type === Material.SKULL_ITEM) {
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
      val ringo = StaticGachaPrizeFactory.getMaxRingo(Util.getName(player))
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
  fun onTitanRepairEvent(event: InventoryCloseEvent) {
    val player = event.player as Player
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid] ?: return
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
  fun onVotingMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが4列でない時終了
    if (topinventory.row != 4) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]!!

    //インベントリ名が以下の時処理
    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "投票ptメニュー") {
      event.isCancelled = true

      if (event.clickedInventory.type === InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.type === Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      //投票pt受取
      if (itemstackcurrent.type === Material.DIAMOND) {
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
          val skull = Util.getVoteskull(Util.getName(player))
          for (i in 0..9) {
            if (player.inventory.contains(skull) || !Util.isPlayerInventoryFull(player)) {
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

          n--
          count++
        }

        player.sendMessage(GOLD.toString() + "投票特典" + WHITE + "(" + count + "票分)を受け取りました")
        player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)

        val itemmeta = itemstackcurrent.itemMeta
        itemstackcurrent.itemMeta = itemmeta
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.type === Material.BOOK_AND_QUILL) {
        // 投票リンク表示
        player.sendMessage(RED.toString() + "" + UNDERLINE + "https://minecraft.jp/servers/54d3529e4ddda180780041a7/vote")
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.closeInventory()
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              CommonSoundEffects.menuTransitionFenceSound,
              StickMenu.firstPage.open
          ).runFor(player)
        }
        // NOTE: WHEN
      } else if (itemstackcurrent.type === Material.WATCH) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleVotingFairy = playerdata.toggleVotingFairy % 4 + 1
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.type === Material.PAPER) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleGiveApple = playerdata.toggleGiveApple % 4 + 1
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.type === Material.JUKEBOX) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleVFSound = !playerdata.toggleVFSound
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.type === Material.GHAST_TEAR) {
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
      } else if (itemstackcurrent.type === Material.COMPASS) {
        VotingFairyTask.speak(player, "僕は" + Util.showHour(playerdata.votingFairyEndTime!!) + "には帰るよー。", playerdata.toggleVFSound)
        player.closeInventory()
      }//妖精召喚
      //妖精音トグル
      //妖精リンゴトグル
      //妖精時間トグル
      //棒メニューに戻る

    }
  }

  @EventHandler
  fun onHomeMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type != EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが3列でない時終了
    if (topinventory.row != 3) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]!!
    val itemmeta = itemstackcurrent.itemMeta

    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "ホームメニュー") {
      event.isCancelled = true

      if (event.clickedInventory.type === InventoryType.PLAYER) {
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
        player.openInventory(MenuInventoryData.getCheckSetHomeMenuData(player))
      }
      for (x in 1..SeichiAssist.seichiAssistConfig.subHomeMax) {
        if (itemmeta.displayName.contains("サブホームポイント" + x + "にワープ")) {
          player.chat("/subhome $x")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        } else if (itemmeta.displayName.contains("サブホームポイント" + x + "の情報")) {
          player.chat("/subhome name $x")
          player.closeInventory()
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        } else if (itemmeta.displayName.contains("サブホームポイント" + x + "を設定")) {
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          playerdata.selectHomeNum = x
          player.openInventory(MenuInventoryData.getCheckSetHomeMenuData(player))
        }
      }

    } else if (topinventory.title.contains("ホームポイントを変更しますか?")) {
      event.isCancelled = true

      if (event.clickedInventory.type === InventoryType.PLAYER) {
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
  fun onGiganticBerserkMenuEvent(event: InventoryClickEvent) {
    //外枠のクリック処理なら終了
    if (event.clickedInventory == null) {
      return
    }

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type !== EntityType.PLAYER) {
      return
    }

    //インベントリが存在しない時終了
    val topinventory = view.topInventory ?: return

    //インベントリが6列でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playerMap[uuid]!!

    if (topinventory.title == DARK_PURPLE.toString() + "" + BOLD + "スキルを進化させますか?") {
      event.isCancelled = true
      if (itemstackcurrent.type === Material.NETHER_STAR) {
        playerdata.giganticBerserk = GiganticBerserk(0, 0, playerdata.giganticBerserk.stage + 1, false)
        player.playSound(player.location, Sound.BLOCK_END_GATEWAY_SPAWN, 1f, 0.5.toFloat())
        player.playSound(player.location, Sound.ENTITY_ENDERDRAGON_AMBIENT, 1f, 0.8.toFloat())
        player.openInventory(MenuInventoryData.getGiganticBerserkEvolution2Menu(player))
      }
    } else if (topinventory.title == LIGHT_PURPLE.toString() + "" + BOLD + "スキルを進化させました") {
      event.isCancelled = true
    }
  }

  private fun getTitle(where: Int, id: Int): String = when (where) {
    1 -> SeichiAssist.seichiAssistConfig.getTitle1(id)
    2 -> SeichiAssist.seichiAssistConfig.getTitle2(id)
    3 -> SeichiAssist.seichiAssistConfig.getTitle3(id)
    else -> throw RuntimeException("メソッドの呼び出し規約違反: whereは、1..3のいずれかでなければいけません。")
  }
}

package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.*
import com.github.unchama.seichiassist.achievement.SeichiAchievement
import com.github.unchama.seichiassist.data.ActiveSkillInventoryData
import com.github.unchama.seichiassist.data.ItemData
import com.github.unchama.seichiassist.data.MenuInventoryData
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import com.github.unchama.seichiassist.menus.stickmenu.firstPage
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.StaticGachaPrizeFactory
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.util.exp.ExperienceManager
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.sequentialEffect
import com.github.unchama.util.ActionStatus.Fail
import com.github.unchama.util.row
import com.google.common.io.ByteStreams
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatColor
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
  internal var playermap = SeichiAssist.playermap
  internal var gachadatalist = SeichiAssist.gachadatalist
  internal var plugin = SeichiAssist.instance
  private val config = SeichiAssist.seichiAssistConfig
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
    if (he.type != EntityType.PLAYER) {
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
    if (topinventory.title == ChatColor.DARK_RED.toString() + "" + ChatColor.BOLD + "サーバーを選択してください") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
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
    //インベントリサイズが36でない時終了
    if (topinventory.row != 4) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playermap[uuid]!!

    //経験値変更用のクラスを設定
    //ExperienceManager expman = new ExperienceManager(player);


    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "整地スキル切り替え") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }
      val isSkull = itemstackcurrent.type == Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      //ページ変更処理
      // ->
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f),
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else if (itemstackcurrent.type == Material.DIAMOND_PICKAXE) {
        // 複数破壊トグル

        if (playerdata.level >= SeichiAssist.seichiAssistConfig.multipleIDBlockBreaklevel) {
          playerdata.multipleidbreakflag = !playerdata.multipleidbreakflag
          if (playerdata.multipleidbreakflag) {
            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
            player.sendMessage(ChatColor.GREEN.toString() + "複数種類同時破壊:ON")
          } else {
            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5.toFloat())
            player.sendMessage(ChatColor.RED.toString() + "複数種類同時破壊:OFF")
          }
          val itemmeta = itemstackcurrent.itemMeta
          itemstackcurrent.itemMeta = MenuInventoryData.MultipleIDBlockBreakToggleMeta(playerdata, itemmeta)
        } else {
          player.sendMessage("整地レベルが足りません")
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
        }
      } else if (itemstackcurrent.type == Material.DIAMOND_AXE) {
        playerdata.chestflag = false
        player.sendMessage(ChatColor.GREEN.toString() + "スキルでのチェスト破壊を無効化しました。")
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5.toFloat())
        player.openInventory(MenuInventoryData.getPassiveSkillMenuData(player))
      } else if (itemstackcurrent.type == Material.CHEST) {
        playerdata.chestflag = true
        player.sendMessage(ChatColor.RED.toString() + "スキルでのチェスト破壊を有効化しました。")
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(MenuInventoryData.getPassiveSkillMenuData(player))
      } else if (itemstackcurrent.type == Material.STICK) {
        player.sendMessage(ChatColor.WHITE.toString() + "パッシブスキル:" + ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "Gigantic" + ChatColor.RED + ChatColor.UNDERLINE + "" + ChatColor.BOLD + "Berserk" + ChatColor.WHITE + "はレベル10以上から使用可能です")
        player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
      } else if (itemstackcurrent.type == Material.WOOD_SWORD || itemstackcurrent.type == Material.STONE_SWORD || itemstackcurrent.type == Material.GOLD_SWORD || itemstackcurrent.type == Material.IRON_SWORD || itemstackcurrent.type == Material.DIAMOND_SWORD) {
        if (!playerdata.isGBStageUp) {
          player.sendMessage(ChatColor.RED.toString() + "進化条件を満たしていません")
        } else {
          player.openInventory(MenuInventoryData.getGiganticBerserkEvolutionMenu(player))
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.5.toFloat())
        }
      }
    }
  }

  //スキルメニューの処理
  @EventHandler
  fun onPlayerClickActiveSkillSellectEvent(event: InventoryClickEvent) {
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
    //インベントリサイズが45でない時終了
    if (topinventory.row != 5) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playermap[uuid]!!

    //経験値変更用のクラスを設定
    val expman = ExperienceManager(player)


    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "整地スキル選択") {
      val isSkull = itemstackcurrent.type == Material.SKULL_ITEM

      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      var type: Int
      var name: String
      var skilllevel: Int
      //ARROWSKILL
      type = ActiveSkill.ARROW.gettypenum()
      skilllevel = 4
      while (skilllevel <= 9) {
        name = ActiveSkill.ARROW.getName(skilllevel)
        if (itemstackcurrent.type == ActiveSkill.ARROW.getMaterial(skilllevel)) {
          val potionmeta = itemstackcurrent.itemMeta as PotionMeta
          if (potionmeta.basePotionData.type == ActiveSkill.ARROW.getPotionType(skilllevel)) {
            if (playerdata.activeskilldata.skilltype == type && playerdata.activeskilldata.skillnum == skilllevel) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(ChatColor.YELLOW.toString() + "選択を解除しました")
              playerdata.activeskilldata.skilltype = 0
              playerdata.activeskilldata.skillnum = 0
            } else {
              playerdata.activeskilldata.updateSkill(player, type, skilllevel, 1)
              player.sendMessage(ChatColor.GREEN.toString() + "アクティブスキル:" + name + "  が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
        skilllevel++
      }
      //MULTISKILL
      type = ActiveSkill.MULTI.gettypenum()
      skilllevel = 4
      while (skilllevel <= 9) {
        name = ActiveSkill.MULTI.getName(skilllevel)
        if (itemstackcurrent.type == ActiveSkill.MULTI.getMaterial(skilllevel)) {
          if (playerdata.activeskilldata.skilltype == type && playerdata.activeskilldata.skillnum == skilllevel) {
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
            player.sendMessage(ChatColor.YELLOW.toString() + "選択を解除しました")
            playerdata.activeskilldata.skilltype = 0
            playerdata.activeskilldata.skillnum = 0
          } else {
            playerdata.activeskilldata.updateSkill(player, type, skilllevel, 1)
            player.sendMessage(ChatColor.GREEN.toString() + "アクティブスキル:" + name + "  が選択されました")
            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
          }
        }
        skilllevel++
      }
      //BREAKSKILL
      type = ActiveSkill.BREAK.gettypenum()
      skilllevel = 1
      while (skilllevel <= 9) {
        name = ActiveSkill.BREAK.getName(skilllevel)
        if (itemstackcurrent.type == ActiveSkill.BREAK.getMaterial(skilllevel)) {
          if (playerdata.activeskilldata.skilltype == ActiveSkill.BREAK.gettypenum() && playerdata.activeskilldata.skillnum == skilllevel) {
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
            player.sendMessage(ChatColor.YELLOW.toString() + "選択を解除しました")
            playerdata.activeskilldata.skilltype = 0
            playerdata.activeskilldata.skillnum = 0
          } else {
            playerdata.activeskilldata.updateSkill(player, type, skilllevel, 1)
            player.sendMessage(ChatColor.GREEN.toString() + "アクティブスキル:" + name + "  が選択されました")
            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
          }
        }
        skilllevel++
      }
      //CONDENSKILL
      //WATER
      type = ActiveSkill.WATERCONDENSE.gettypenum()
      skilllevel = 7
      while (skilllevel <= 9) {
        name = ActiveSkill.WATERCONDENSE.getName(skilllevel)
        if (itemstackcurrent.type == ActiveSkill.WATERCONDENSE.getMaterial(skilllevel)) {
          if (playerdata.activeskilldata.assaulttype == type && playerdata.activeskilldata.assaultnum == skilllevel) {
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
            player.sendMessage(ChatColor.YELLOW.toString() + "選択を解除しました")
            playerdata.activeskilldata.assaulttype = 0
            playerdata.activeskilldata.assaultnum = 0
          } else {
            playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
            player.sendMessage(ChatColor.DARK_GREEN.toString() + "アサルトスキル:" + name + "  が選択されました")
            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
          }
        }
        skilllevel++
      }

      //LAVA
      type = ActiveSkill.LAVACONDENSE.gettypenum()
      skilllevel = 7
      while (skilllevel <= 9) {
        name = ActiveSkill.LAVACONDENSE.getName(skilllevel)
        if (itemstackcurrent.type == ActiveSkill.LAVACONDENSE.getMaterial(skilllevel)) {
          if (playerdata.activeskilldata.assaulttype == type && playerdata.activeskilldata.assaultnum == skilllevel) {
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
            player.sendMessage(ChatColor.YELLOW.toString() + "選択を解除しました")
            playerdata.activeskilldata.assaulttype = 0
            playerdata.activeskilldata.assaultnum = 0
          } else {
            playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
            player.sendMessage(ChatColor.DARK_GREEN.toString() + "アサルトスキル:" + name + "  が選択されました")
            player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
          }
        }
        skilllevel++
      }

      type = ActiveSkill.FLUIDCONDENSE.gettypenum()
      skilllevel = 10
      if (itemstackcurrent.type == ActiveSkill.FLUIDCONDENSE.getMaterial(skilllevel)) {
        if (playerdata.activeskilldata.assaultnum == skilllevel && playerdata.activeskilldata.assaulttype == type) {
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          player.sendMessage(ChatColor.YELLOW.toString() + "選択を解除しました")
          playerdata.activeskilldata.assaulttype = 0
          playerdata.activeskilldata.assaultnum = 0
        } else {
          playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
          player.sendMessage(ChatColor.DARK_GREEN.toString() + "アサルトスキル:" + "ヴェンダー・ブリザード" + " が選択されました")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
        }
      }

      //アサルトアーマー
      type = ActiveSkill.ARMOR.gettypenum()
      skilllevel = 10
      if (itemstackcurrent.type == ActiveSkill.ARMOR.getMaterial(skilllevel)) {
        if (playerdata.activeskilldata.assaultnum == skilllevel && playerdata.activeskilldata.assaulttype == type) {
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          player.sendMessage(ChatColor.YELLOW.toString() + "選択を解除しました")
          playerdata.activeskilldata.assaulttype = 0
          playerdata.activeskilldata.assaultnum = 0
        } else {
          playerdata.activeskilldata.updateAssaultSkill(player, type, skilllevel, 1)
          player.sendMessage(ChatColor.DARK_GREEN.toString() + "アサルトスキル:" + "アサルト・アーマー" + " が選択されました")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
        }
      }

      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f),
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else if (itemstackcurrent.type == Material.STONE_BUTTON) {
        if (itemstackcurrent.itemMeta.displayName.contains("リセット")) {
          //経験値変更用のクラスを設定
          //経験値が足りなかったら処理を終了
          if (!expman.hasExp(10000)) {
            player.sendMessage(ChatColor.RED.toString() + "必要な経験値が足りません")
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
          player.sendMessage(ChatColor.LIGHT_PURPLE.toString() + "アクティブスキルポイントをリセットしました")
          //メニューを開く
          player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
        }
      } else if (itemstackcurrent.type == Material.GLASS) {
        if (playerdata.activeskilldata.skilltype == 0 && playerdata.activeskilldata.skillnum == 0
            && playerdata.activeskilldata.assaulttype == 0 && playerdata.activeskilldata.assaultnum == 0) {
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          player.sendMessage(ChatColor.YELLOW.toString() + "既に全ての選択は削除されています")
        } else {
          playerdata.activeskilldata.clearSellect(player)

        }
      } else if (itemstackcurrent.type == Material.BOOKSHELF) {
        //開く音を再生
        player.playSound(player.location, Sound.BLOCK_BREWING_STAND_BREW, 1f, 0.5.toFloat())
        player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player))
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
    if (he.type != EntityType.PLAYER) {
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
    val playerdata = playermap[uuid]!!

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "整地スキルエフェクト選択") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.type == Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        //開く音を再生
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1.toFloat())
        player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
        return
      } else if (itemstackcurrent.type == Material.GLASS) {
        if (playerdata.activeskilldata.effectnum == 0) {
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          player.sendMessage(ChatColor.YELLOW.toString() + "既に選択されています")
        } else {
          playerdata.activeskilldata.effectnum = 0
          player.sendMessage(ChatColor.GREEN.toString() + "エフェクト:未設定  が選択されました")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
        }
        return
      } else if (itemstackcurrent.type == Material.BOOK_AND_QUILL) {
        //開く音を再生
        player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getBuyRecordMenuData(player))
        return
      } else {
        val skilleffect = ActiveSkillEffect.values()
        for (activeSkillEffect in skilleffect) {
          if (itemstackcurrent.type == activeSkillEffect.material) {
            if (playerdata.activeskilldata.effectnum == activeSkillEffect.num) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(ChatColor.YELLOW.toString() + "既に選択されています")
            } else {
              playerdata.activeskilldata.effectnum = activeSkillEffect.num
              player.sendMessage(ChatColor.GREEN.toString() + "エフェクト:" + activeSkillEffect.getName() + ChatColor.RESET + "" + ChatColor.GREEN + " が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
        val premiumeffect = ActiveSkillPremiumEffect.values()
        for (activeSkillPremiumEffect in premiumeffect) {
          if (itemstackcurrent.type == activeSkillPremiumEffect.material) {
            if (playerdata.activeskilldata.effectnum == activeSkillPremiumEffect.num) {
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
              player.sendMessage(ChatColor.YELLOW.toString() + "既に選択されています")
            } else {
              playerdata.activeskilldata.effectnum = activeSkillPremiumEffect.num + 100
              player.sendMessage(ChatColor.GREEN.toString() + "" + ChatColor.BOLD + "プレミアムエフェクト:" + activeSkillPremiumEffect.getName() + ChatColor.RESET + "" + ChatColor.GREEN + "" + ChatColor.BOLD + " が選択されました")
              player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 0.1.toFloat())
            }
          }
        }
      }


      //ここからエフェクト開放の処理
      if (itemstackcurrent.type == Material.BEDROCK) {
        val itemmeta = itemstackcurrent.itemMeta
        val skilleffect = ActiveSkillEffect.values()
        for (activeSkillEffect in skilleffect) {
          if (itemmeta.displayName.contains(activeSkillEffect.getName())) {
            if (playerdata.activeskilldata.effectpoint < activeSkillEffect.usePoint) {
              player.sendMessage(ChatColor.DARK_RED.toString() + "エフェクトポイントが足りません")
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat())
            } else {
              playerdata.activeskilldata.obtainedSkillEffects.add(activeSkillEffect)
              player.sendMessage(ChatColor.LIGHT_PURPLE.toString() + "エフェクト：" + activeSkillEffect.getName() + ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "" + " を解除しました")
              player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
              playerdata.activeskilldata.effectpoint -= activeSkillEffect.usePoint
              player.openInventory(MenuInventoryData.getActiveSkillEffectMenuData(player))
            }
          }
        }
      }
      //ここからプレミアムエフェクト開放の処理
      if (itemstackcurrent.type == Material.BEDROCK) {
        val itemmeta = itemstackcurrent.itemMeta
        val premiumeffect = ActiveSkillPremiumEffect.values()
        for (activeSkillPremiumEffect in premiumeffect) {
          if (activeSkillPremiumEffect.getName() in itemmeta.displayName) {
            if (playerdata.activeskilldata.premiumeffectpoint < activeSkillPremiumEffect.usePoint) {
              player.sendMessage(ChatColor.DARK_RED.toString() + "プレミアムエフェクトポイントが足りません")
              player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat())
            } else {
              playerdata.activeskilldata.obtainedSkillPremiumEffects.add(activeSkillPremiumEffect)
              player.sendMessage(ChatColor.LIGHT_PURPLE.toString() + "" + ChatColor.BOLD + "プレミアムエフェクト：" + activeSkillPremiumEffect.getName() + ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "" + " を解除しました")
              if (databaseGateway.donateDataManipulator.addPremiumEffectBuy(playerdata, activeSkillPremiumEffect) == Fail) {
                player.sendMessage("購入履歴が正しく記録されませんでした。管理者に報告してください。")
              }
              player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
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
    if (he.type != EntityType.PLAYER) {
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
    val playerdata = playermap[uuid]!!

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "整地スキル選択") {
      event.isCancelled = true
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.type == Material.BEDROCK) {
        val itemmeta = itemstackcurrent.itemMeta
        val skilllevel: Int
        val skilltype: Int
        if (itemmeta.displayName.contains("エビフライ・ドライブ")) {
          skilllevel = 4
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.5.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ホーリー・ショット")) {
          skilllevel = 5
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ツァーリ・ボンバ")) {
          skilllevel = 6
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("アーク・ブラスト")) {
          skilllevel = 7
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ファンタズム・レイ")) {
          skilllevel = 8
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("スーパー・ノヴァ")) {
          skilllevel = 9
          skilltype = 1
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.arrowskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.arrowskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(ChatColor.YELLOW.toString() + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(ChatColor.GOLD.toString() + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("トム・ボウイ")) {
          skilllevel = 4
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("サンダー・ストーム")) {
          skilllevel = 5
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("スターライト・ブレイカー")) {
          skilllevel = 6
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("アース・ディバイド")) {
          skilllevel = 7
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ヘヴン・ゲイボルグ")) {
          skilllevel = 8
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ディシジョン")) {
          skilllevel = 9
          skilltype = 2
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.multiskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.multiskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.breakskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(ChatColor.YELLOW.toString() + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(ChatColor.GOLD.toString() + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("デュアル・ブレイク")) {
          skilllevel = 1
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("トリアル・ブレイク")) {
          skilllevel = 2
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("エクスプロージョン")) {
          skilllevel = 3
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ミラージュ・フレア")) {
          skilllevel = 4
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ドッ・カーン")) {
          skilllevel = 5
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ギガンティック・ボム")) {
          skilllevel = 6
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ブリリアント・デトネーション")) {
          skilllevel = 7
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("レムリア・インパクト")) {
          skilllevel = 8
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("エターナル・ヴァイス")) {
          skilllevel = 9
          skilltype = 3
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.breakskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(ChatColor.YELLOW.toString() + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(ChatColor.GOLD.toString() + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ホワイト・ブレス")) {
          skilllevel = 7
          skilltype = 4
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.watercondenskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("アブソリュート・ゼロ")) {
          skilllevel = 8
          skilltype = 4
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.watercondenskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.watercondenskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ダイアモンド・ダスト")) {
          skilllevel = 9
          skilltype = 4
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.watercondenskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.watercondenskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(ChatColor.YELLOW.toString() + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(ChatColor.GOLD.toString() + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("ラヴァ・コンデンセーション")) {
          skilllevel = 7
          skilltype = 5
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.breakskill < 3) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(3, 3) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.lavacondenskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }/*else if(playerdata.activeskilldata.condenskill < skilllevel - 1){
						player.sendMessage(ChatColor.DARK_RED + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype,skilllevel - 1) + "]を習得する必要があります");
						player.playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 1, (float) 0.1);
					}*/
        } else if (itemmeta.displayName.contains("モエラキ・ボールダーズ")) {
          skilllevel = 8
          skilltype = 5
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.lavacondenskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.lavacondenskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("エルト・フェットル")) {
          skilllevel = 9
          skilltype = 5
          if (playerdata.activeskilldata.skillpoint < skilllevel * 10) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else if (playerdata.activeskilldata.lavacondenskill < skilllevel - 1) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "前提スキル[" + ActiveSkill.getActiveSkillName(skilltype, skilllevel - 1) + "]を習得する必要があります")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.lavacondenskill = skilllevel
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + ActiveSkill.getActiveSkillName(skilltype, skilllevel) + "を解除しました")
            player.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.2.toFloat())
            playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
            if (playerdata.activeskilldata.arrowskill == 9 && playerdata.activeskilldata.multiskill == 9 && playerdata.activeskilldata.watercondenskill == 9 && playerdata.activeskilldata.lavacondenskill == 9) {
              player.sendMessage(ChatColor.YELLOW.toString() + "" + ChatColor.BOLD + "全てのスキルを習得し、アサルト・アーマーを解除しました")
              Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1f, 1.2.toFloat())
              Util.sendEveryMessage(ChatColor.GOLD.toString() + "" + ChatColor.BOLD + playerdata.name + "が全てのスキルを習得し、アサルトアーマーを解除しました！")
            }
            player.openInventory(ActiveSkillInventoryData.getActiveSkillMenuData(player))
          }
        } else if (itemmeta.displayName.contains("アサルト・アーマー")) {

        } else if (itemmeta.displayName.contains("ヴェンダー・ブリザード")) {
          if (playerdata.activeskilldata.skillpoint < 110) {
            player.sendMessage(ChatColor.DARK_RED.toString() + "アクティブスキルポイントが足りません")
            player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          } else {
            playerdata.activeskilldata.fluidcondenskill = 10
            player.sendMessage(ChatColor.AQUA.toString() + "" + ChatColor.BOLD + "" + "ヴェンダー・ブリザードを解除しました")
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
    if (he.type != EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he as Player

    val isSkull = itemstackcurrent.type == Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "整地神ランキング") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f),
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowDown") {
        val itemmeta = itemstackcurrent.itemMeta
        if (itemmeta.displayName.contains("整地神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
          val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

          //開く音を再生
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getRankingList(page_display - 1))
        }
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowUp") {
        val itemmeta = itemstackcurrent.itemMeta
        if (itemmeta.displayName.contains("整地神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
          val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

          //開く音を再生
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getRankingList(page_display - 1))
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
    if (he.type != EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he as Player

    val isSkull = itemstackcurrent.type == Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "ログイン神ランキング") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f),
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
    if (he.type != EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he as Player

    val isSkull = itemstackcurrent.type == Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "投票神ランキング") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f),
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowDown") {
        val itemmeta = itemstackcurrent.itemMeta
        if (itemmeta.displayName.contains("投票神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
          val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

          //開く音を再生
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getRankingList_p_vote(page_display - 1))
        }
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowUp") {
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

  //ランキングメニュー
  @EventHandler
  fun onPlayerClickSeichiRankingMenuEvent3(event: InventoryClickEvent) {
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
    //インベントリサイズが36でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he as Player

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "寄付神ランキング") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.type == Material.SKULL_ITEM
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      //ページ変更処理
      if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f),
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowDown") {
        val itemmeta = itemstackcurrent.itemMeta
        if (itemmeta.displayName.contains("寄付神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
          val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

          //開く音を再生
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getRankingList_premiumeffectpoint(page_display - 1))
        }
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowUp") {
        val itemmeta = itemstackcurrent.itemMeta
        if (itemmeta.displayName.contains("寄付神ランキング") && itemmeta.displayName.contains("ページ目")) {//移動するページの種類を判定
          val page_display = Integer.parseInt(itemmeta.displayName.replace("[^0-9]".toRegex(), "")) //数字以外を全て消す

          //開く音を再生
          player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
          player.openInventory(MenuInventoryData.getRankingList_premiumeffectpoint(page_display - 1))
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
    if (he.type != EntityType.PLAYER) {
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
    if (topinventory.title == ChatColor.BLUE.toString() + "" + ChatColor.BOLD + "プレミアムエフェクト購入履歴") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.type == Material.SKULL_ITEM

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
    val playerdata = playermap[uuid] ?: return
    //エラー分岐
    val name = playerdata.name
    val inventory = event.inventory

    //インベントリサイズが36でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.title == ChatColor.LIGHT_PURPLE.toString() + "" + ChatColor.BOLD + "交換したい景品を入れてください") {
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
        } else if (m.type == Material.SKULL_ITEM) {
          //丁重にお返しする
          dropitem.add(m)
          continue
        }
        //ガチャ景品リストにアイテムがあった時にtrueになるフラグ
        var flag = false
        //ガチャ景品リストを一個ずつ見ていくfor文
        for (gachadata in gachadatalist) {
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
        player.sendMessage(ChatColor.RED.toString() + "ガチャシステムメンテナンス中の為全てのアイテムを返却します")
      } else if (big <= 0 && reg <= 0) {
        player.sendMessage(ChatColor.YELLOW.toString() + "景品を認識しませんでした。全てのアイテムを返却します")
      } else {
        player.sendMessage(ChatColor.GREEN.toString() + "大当たり景品を" + big + "個、当たり景品を" + reg + "個認識しました")
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
        player.sendMessage(ChatColor.GREEN.toString() + "" + count + "枚の" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "を受け取りました")
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

    val itemstackcurrent = event.currentItem
    val view = event.view
    val he = view.player
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.type != EntityType.PLAYER) {
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
    val playerdata = playermap[uuid]!!

    //経験値変更用のクラスを設定
    //ExperienceManager expman = new ExperienceManager(player);


    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "実績・二つ名システム") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      val isSkull = itemstackcurrent.type == Material.SKULL_ITEM

      //表示内容をLVに変更
      if (itemstackcurrent.type == Material.REDSTONE_TORCH_ON) {
        playerdata.displayTitle1No = 0
        playerdata.displayTitle2No = 0
        playerdata.displayTitle3No = 0
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
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f),
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

    val isSkull = itemstackcurrent.type == Material.SKULL_ITEM
    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "カテゴリ「整地」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      //クリックしたボタンに応じた各処理内容の記述ここから

      //実績「整地量」
      if (itemstackcurrent.type == Material.IRON_PICKAXE) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getTitleAmountData(player))
      }

      //実績「整地神ランキング」
      if (itemstackcurrent.type == Material.DIAMOND_PICKAXE) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getTitleRankData(player))
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getTitleMenuData(player))
        return
      }//実績メニューに戻る

    }

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "カテゴリ「建築」") {
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

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "カテゴリ「ログイン」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      } else if (itemstackcurrent.type == Material.COMPASS) {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        playerdata.titlepage = 1
        player.openInventory(MenuInventoryData.getTitleTimeData(player))
      } else if (itemstackcurrent.type == Material.BOOK) {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        playerdata.titlepage = 1
        player.openInventory(MenuInventoryData.getTitleJoinAmountData(player))
      } else if (itemstackcurrent.type == Material.BOOK_AND_QUILL) {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        playerdata.titlepage = 1
        player.openInventory(MenuInventoryData.getTitleJoinChainData(player))
      } else if (itemstackcurrent.type == Material.NETHER_STAR) {
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

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "カテゴリ「やりこみ」") {
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

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "カテゴリ「特殊」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      } else if (itemstackcurrent.type == Material.BLAZE_POWDER) {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        playerdata.titlepage = 1
        player.openInventory(MenuInventoryData.getTitleEventData(player))
      } else if (itemstackcurrent.type == Material.YELLOW_FLOWER) {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        playerdata.titlepage = 1
        player.openInventory(MenuInventoryData.getTitleSupportData(player))
      } else if (itemstackcurrent.type == Material.DIAMOND_BARDING) {
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

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "二つ名組合せシステム") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      //実績ポイント最新化
      if (itemstackcurrent.type == Material.EMERALD_ORE) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.achvPointMAX = 0
        for (i in 1000..9799) {
          if (playerdata.TitleFlags.get(i)) {
            playerdata.achvPointMAX = playerdata.achvPointMAX + 10
          }
        }
        playerdata.achvPoint = playerdata.achvPointMAX + playerdata.achvChangenum * 3 - playerdata.achvPointUSE
        player.openInventory(MenuInventoryData.setFreeTitleMainData(player))
      }

      //エフェクトポイント→実績ポイント変換
      if (itemstackcurrent.type == Material.EMERALD) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        //不足してたらスルー
        if (playerdata.activeskilldata.effectpoint < 10) {
          player.sendMessage("エフェクトポイントが不足しています。")
        } else {
          playerdata.achvChangenum = playerdata.achvChangenum + 1
          playerdata.activeskilldata.effectpoint -= 10
        }
        //データ最新化
        playerdata.achvPointMAX = 0
        for (i in 1000..9799) {
          if (playerdata.TitleFlags.get(i)) {
            playerdata.achvPointMAX = playerdata.achvPointMAX + 10
          }
        }
        playerdata.achvPoint = playerdata.achvPointMAX + playerdata.achvChangenum * 3 - playerdata.achvPointUSE

        player.openInventory(MenuInventoryData.setFreeTitleMainData(player))


      }

      //パーツショップ
      if (itemstackcurrent.type == Material.ITEM_FRAME) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.setTitleShopData(player))
      }

      //前パーツ
      if (itemstackcurrent.type == Material.WATER_BUCKET) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.setFreeTitle1Data(player))
      }

      //中パーツ
      if (itemstackcurrent.type == Material.MILK_BUCKET) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.setFreeTitle2Data(player))
      }

      //後パーツ
      if (itemstackcurrent.type == Material.LAVA_BUCKET) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.setFreeTitle3Data(player))
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getTitleMenuData(player))
        return
      }//実績メニューに戻る

    }

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "二つ名組合せ「前」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      } else if (itemstackcurrent.type == Material.WATER_BUCKET) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

        val forcheck = (SeichiAssist.seichiAssistConfig.getTitle1(Integer.parseInt(itemmeta.displayName))
            + SeichiAssist.seichiAssistConfig.getTitle2(playerdata.displayTitle2No)
            + SeichiAssist.seichiAssistConfig.getTitle3(playerdata.displayTitle3No))
        if (forcheck.length < 9) {
          playerdata.displayTitle1No = Integer.parseInt(itemmeta.displayName)
          player.sendMessage("前パーツ「" + SeichiAssist.seichiAssistConfig.getTitle1(playerdata.displayTitle1No) + "」をセットしました。")
        } else {
          player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。")
        }
      } else if (itemstackcurrent.type == Material.GRASS) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.displayTitle1No = 0
        player.sendMessage("前パーツの選択を解除しました。")
        return
      } else if (itemstackcurrent.type == Material.BARRIER) {
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

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "二つ名組合せ「中」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      } else if (itemstackcurrent.type == Material.MILK_BUCKET) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

        val forcheck = (SeichiAssist.seichiAssistConfig.getTitle1(playerdata.displayTitle1No)
            + SeichiAssist.seichiAssistConfig.getTitle2(Integer.parseInt(itemmeta.displayName))
            + SeichiAssist.seichiAssistConfig.getTitle3(playerdata.displayTitle3No))
        if (forcheck.length < 9) {
          playerdata.displayTitle2No = Integer.parseInt(itemmeta.displayName)
          player.sendMessage("中パーツ「" + SeichiAssist.seichiAssistConfig.getTitle2(playerdata.displayTitle2No) + "」をセットしました。")
        } else {
          player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。")
        }
      } else if (itemstackcurrent.type == Material.GRASS) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.displayTitle2No = 0
        player.sendMessage("中パーツの選択を解除しました。")
        return
      } else if (itemstackcurrent.type == Material.BARRIER) {
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

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "二つ名組合せ「後」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      } else if (itemstackcurrent.type == Material.LAVA_BUCKET) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

        val forcheck = (SeichiAssist.seichiAssistConfig.getTitle1(playerdata.displayTitle1No)
            + SeichiAssist.seichiAssistConfig.getTitle2(playerdata.displayTitle2No)
            + SeichiAssist.seichiAssistConfig.getTitle3(Integer.parseInt(itemmeta.displayName)))
        if (forcheck.length < 9) {
          playerdata.displayTitle3No = Integer.parseInt(itemmeta.displayName)
          player.sendMessage("後パーツ「" + SeichiAssist.seichiAssistConfig.getTitle3(playerdata.displayTitle3No) + "」をセットしました。")
        } else {
          player.sendMessage("全パーツ合計で8文字以内になるよう設定してください。")
        }
      } else if (itemstackcurrent.type == Material.GRASS) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.displayTitle3No = 0
        player.sendMessage("後パーツの選択を解除しました。")
        return
      } else if (itemstackcurrent.type == Material.BARRIER) {
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

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "実績ポイントショップ") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      //実績ポイント最新化
      if (itemstackcurrent.type == Material.EMERALD_ORE) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.achvPointMAX = 0
        for (i in 1000..9799) {
          if (playerdata.TitleFlags.get(i)) {
            playerdata.achvPointMAX = playerdata.achvPointMAX + 10
          }
        }
        playerdata.achvPoint = playerdata.achvPointMAX + playerdata.achvChangenum * 3 - playerdata.achvPointUSE
        playerdata.samepageflag = true
        player.openInventory(MenuInventoryData.setTitleShopData(player))
      }

      //購入処理
      if (itemstackcurrent.type == Material.BEDROCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)

        if (Integer.parseInt(itemmeta.displayName) < 9900) {
          if (playerdata.achvPoint < 20) {
            player.sendMessage("実績ポイントが不足しています。")
          } else {
            playerdata.TitleFlags.set(Integer.parseInt(itemmeta.displayName))
            playerdata.achvPoint = playerdata.achvPoint - 20
            playerdata.achvPointUSE = playerdata.achvPointUSE + 20
            player.sendMessage("パーツ「" + SeichiAssist.seichiAssistConfig.getTitle1(Integer.parseInt(itemmeta.displayName)) + "」を購入しました。")
            playerdata.samepageflag = true
            player.openInventory(MenuInventoryData.setTitleShopData(player))
          }
        } else {
          if (playerdata.achvPoint < 35) {
            player.sendMessage("実績ポイントが不足しています。")
          } else {
            playerdata.TitleFlags.set(Integer.parseInt(itemmeta.displayName))
            playerdata.achvPoint = playerdata.achvPoint - 35
            playerdata.achvPointUSE = playerdata.achvPointUSE + 35
            player.sendMessage("パーツ「" + SeichiAssist.seichiAssistConfig.getTitle2(Integer.parseInt(itemmeta.displayName)) + "」を購入しました。")
            playerdata.samepageflag = true
            player.openInventory(MenuInventoryData.setTitleShopData(player))
          }
        }


      } else if (itemstackcurrent.type == Material.BARRIER) {
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


    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "実績「整地神ランキング」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      if (itemstackcurrent.type == Material.BEDROCK) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
        player.openInventory(MenuInventoryData.getTitleRankData(player))
      } else if (itemstackcurrent.type == Material.DIAMOND_BLOCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (itemmeta.displayName.contains("No1001「" + SeichiAssist.seichiAssistConfig.getTitle1(1001) + "」")) {
          playerdata.displayTitle1No = 1001
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1001) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No1002「" + SeichiAssist.seichiAssistConfig.getTitle1(1002) + "」")) {
          playerdata.displayTitle1No = 1002
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1002) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No1003「" + SeichiAssist.seichiAssistConfig.getTitle1(1003) + "」")) {
          playerdata.displayTitle1No = 1003
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1003) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No1004「" + SeichiAssist.seichiAssistConfig.getTitle1(1004) + "」")) {
          playerdata.displayTitle1No = 1004
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1004) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No1010「" + SeichiAssist.seichiAssistConfig.getTitle1(1010) + "」")) {
          playerdata.displayTitle1No = 1010
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1010) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No1011「" + SeichiAssist.seichiAssistConfig.getTitle1(1011)
                + SeichiAssist.seichiAssistConfig.getTitle2(9904) + SeichiAssist.seichiAssistConfig.getTitle3(1011) + "」")) {
          playerdata.displayTitle1No = 1011
          playerdata.displayTitle2No = 9904
          playerdata.displayTitle3No = 1011
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1011)
              + SeichiAssist.seichiAssistConfig.getTitle2(9904) + SeichiAssist.seichiAssistConfig.getTitle3(1011) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No1012「" + SeichiAssist.seichiAssistConfig.getTitle1(1012)
                + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(1012) + "」")) {
          playerdata.displayTitle1No = 1012
          playerdata.displayTitle2No = 9901
          playerdata.displayTitle3No = 1012
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1012)
              + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(1012) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No1005「" + SeichiAssist.seichiAssistConfig.getTitle1(1005)
                + SeichiAssist.seichiAssistConfig.getTitle3(1005) + "」")) {
          playerdata.displayTitle1No = 1005
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 1005
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1005)
              + SeichiAssist.seichiAssistConfig.getTitle3(1005) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No1006「" + SeichiAssist.seichiAssistConfig.getTitle1(1006) + "」")) {
          playerdata.displayTitle1No = 1006
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1006) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No1007「" + SeichiAssist.seichiAssistConfig.getTitle1(1007)
                + SeichiAssist.seichiAssistConfig.getTitle2(9904) + SeichiAssist.seichiAssistConfig.getTitle3(1007) + "」")) {
          playerdata.displayTitle1No = 1007
          playerdata.displayTitle2No = 9904
          playerdata.displayTitle3No = 1007
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1007)
              + SeichiAssist.seichiAssistConfig.getTitle2(9904) + SeichiAssist.seichiAssistConfig.getTitle3(1007) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No1008「" + SeichiAssist.seichiAssistConfig.getTitle1(1008)
                + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(1008) + "」")) {
          playerdata.displayTitle1No = 1008
          playerdata.displayTitle2No = 9901
          playerdata.displayTitle3No = 1008
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1008)
              + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(1008) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No1009「" + SeichiAssist.seichiAssistConfig.getTitle1(1009)
                + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(1009) + "」")) {
          playerdata.displayTitle1No = 1009
          playerdata.displayTitle2No = 9909
          playerdata.displayTitle3No = 1009
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(1009)
              + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(1009) + "」が設定されました。")
        }
        player.openInventory(MenuInventoryData.getTitleRankData(player))

      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getTitleSeichi(player))
        return
      }//実績メニューに戻る
    }

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "実績「整地量」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      if (itemstackcurrent.type == Material.BEDROCK) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
        player.openInventory(MenuInventoryData.getTitleAmountData(player))
      } else if (itemstackcurrent.type == Material.DIAMOND_BLOCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (itemmeta.displayName.contains("No3001「" + SeichiAssist.seichiAssistConfig.getTitle1(3001) + "」")) {
          playerdata.displayTitle1No = 3001
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3001) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3002「" + SeichiAssist.seichiAssistConfig.getTitle1(3002)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(3002) + "」")) {
          playerdata.displayTitle1No = 3002
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 3002
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3002)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(3002) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3003「" + SeichiAssist.seichiAssistConfig.getTitle1(3003) + "」")) {
          playerdata.displayTitle1No = 3003
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3003) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3004「" + SeichiAssist.seichiAssistConfig.getTitle1(3004)
                + SeichiAssist.seichiAssistConfig.getTitle2(9902) + "」")) {
          playerdata.displayTitle1No = 3004
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3004) +
              SeichiAssist.seichiAssistConfig.getTitle2(9902) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3005「" + SeichiAssist.seichiAssistConfig.getTitle1(3005)
                + SeichiAssist.seichiAssistConfig.getTitle3(3005) + "」")) {
          playerdata.displayTitle1No = 3005
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 3005
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3005)
              + SeichiAssist.seichiAssistConfig.getTitle3(3005) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3006「" + SeichiAssist.seichiAssistConfig.getTitle1(3006) + "」")) {
          playerdata.displayTitle1No = 3006
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3006) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3007「" + SeichiAssist.seichiAssistConfig.getTitle1(3007)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」")) {
          playerdata.displayTitle1No = 3007
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3007) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3008「" + SeichiAssist.seichiAssistConfig.getTitle1(3008) + "」")) {
          playerdata.displayTitle1No = 3008
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3008) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3009「" + SeichiAssist.seichiAssistConfig.getTitle1(3009)
                + SeichiAssist.seichiAssistConfig.getTitle3(3009) + "」")) {
          playerdata.displayTitle1No = 3009
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 3009
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3009)
              + SeichiAssist.seichiAssistConfig.getTitle3(3009) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3010「" + SeichiAssist.seichiAssistConfig.getTitle1(3010)
                + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(3010) + "」")) {
          playerdata.displayTitle1No = 3010
          playerdata.displayTitle2No = 9909
          playerdata.displayTitle3No = 3010
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3010)
              + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(3010) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3011「" + SeichiAssist.seichiAssistConfig.getTitle1(3011) + "」")) {
          playerdata.displayTitle1No = 3011
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3011) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3012「" + SeichiAssist.seichiAssistConfig.getTitle1(3012)
                + SeichiAssist.seichiAssistConfig.getTitle3(3012) + "」")) {
          playerdata.displayTitle1No = 3012
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 3012
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3012)
              + SeichiAssist.seichiAssistConfig.getTitle3(3012) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3013「" + SeichiAssist.seichiAssistConfig.getTitle1(3013)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(3013) + "」")) {
          playerdata.displayTitle1No = 3013
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 3013
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3013)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(3013) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3014「" + SeichiAssist.seichiAssistConfig.getTitle1(3014)
                + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(3014) + "」")) {
          playerdata.displayTitle1No = 3014
          playerdata.displayTitle2No = 9909
          playerdata.displayTitle3No = 3014
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3014)
              + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(3014) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3015「" + SeichiAssist.seichiAssistConfig.getTitle1(3015) + "」")) {
          playerdata.displayTitle1No = 3015
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3015) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3016「" + SeichiAssist.seichiAssistConfig.getTitle1(3016) + "」")) {
          playerdata.displayTitle1No = 3016
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3016) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3017「" + SeichiAssist.seichiAssistConfig.getTitle1(3017) + "」")) {
          playerdata.displayTitle1No = 3017
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3017) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3018「" + SeichiAssist.seichiAssistConfig.getTitle1(3018) + "」")) {
          playerdata.displayTitle1No = 3018
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3018) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No3019「" + SeichiAssist.seichiAssistConfig.getTitle1(3019) + "」")) {
          playerdata.displayTitle1No = 3019
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(3019) + "」が設定されました。")
        }
        player.openInventory(MenuInventoryData.getTitleAmountData(player))

      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getTitleSeichi(player))
        return
      }//実績メニューに戻る
    }

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "実績「参加時間」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      if (itemstackcurrent.type == Material.BEDROCK) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
        player.openInventory(MenuInventoryData.getTitleTimeData(player))
      } else if (itemstackcurrent.type == Material.DIAMOND_BLOCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (itemmeta.displayName.contains("No4001「" + SeichiAssist.seichiAssistConfig.getTitle1(4001)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4001) + "」")) {
          playerdata.displayTitle1No = 4001
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 4001
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4001)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4001) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4002「" + SeichiAssist.seichiAssistConfig.getTitle1(4002)
                + SeichiAssist.seichiAssistConfig.getTitle3(4002) + "」")) {
          playerdata.displayTitle1No = 4002
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4002
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4002)
              + SeichiAssist.seichiAssistConfig.getTitle3(4002) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4003「" + SeichiAssist.seichiAssistConfig.getTitle1(4003)
                + SeichiAssist.seichiAssistConfig.getTitle3(4003) + "」")) {
          playerdata.displayTitle1No = 4003
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4003
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4003)
              + SeichiAssist.seichiAssistConfig.getTitle3(4003) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4004「" + SeichiAssist.seichiAssistConfig.getTitle1(4004)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4004) + "」")) {
          playerdata.displayTitle1No = 4004
          playerdata.displayTitle2No = 9005
          playerdata.displayTitle3No = 4004
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4004)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4004) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4005「" + SeichiAssist.seichiAssistConfig.getTitle1(4005)
                + SeichiAssist.seichiAssistConfig.getTitle3(4005) + "」")) {
          playerdata.displayTitle1No = 4005
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4005
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4005)
              + SeichiAssist.seichiAssistConfig.getTitle3(4005) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4006「" + SeichiAssist.seichiAssistConfig.getTitle1(4006)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4006) + "」")) {
          playerdata.displayTitle1No = 4006
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 4006
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4006)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4006) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4007「" + SeichiAssist.seichiAssistConfig.getTitle1(4007)
                + SeichiAssist.seichiAssistConfig.getTitle3(4007) + "」")) {
          playerdata.displayTitle1No = 4007
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4007
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4007)
              + SeichiAssist.seichiAssistConfig.getTitle3(4007) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4008「" + SeichiAssist.seichiAssistConfig.getTitle1(4008)
                + SeichiAssist.seichiAssistConfig.getTitle3(4008) + "」")) {
          playerdata.displayTitle1No = 4008
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4008
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4008)
              + SeichiAssist.seichiAssistConfig.getTitle3(4008) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4009「" + SeichiAssist.seichiAssistConfig.getTitle1(4009)
                + SeichiAssist.seichiAssistConfig.getTitle3(4009) + "」")) {
          playerdata.displayTitle1No = 4009
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4009
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4009)
              + SeichiAssist.seichiAssistConfig.getTitle3(4009) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4010「" + SeichiAssist.seichiAssistConfig.getTitle1(4010)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4010) + "」")) {
          playerdata.displayTitle1No = 4010
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 4010
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4010)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4010) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4011「" + SeichiAssist.seichiAssistConfig.getTitle1(4011)
                + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(4011) + "」")) {
          playerdata.displayTitle1No = 4011
          playerdata.displayTitle2No = 9901
          playerdata.displayTitle3No = 4011
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4011)
              + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(4011) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4012「" + SeichiAssist.seichiAssistConfig.getTitle1(4012)
                + SeichiAssist.seichiAssistConfig.getTitle3(4012) + "」")) {
          playerdata.displayTitle1No = 4012
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4012
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4012)
              + SeichiAssist.seichiAssistConfig.getTitle3(4012) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4013「" + SeichiAssist.seichiAssistConfig.getTitle1(4013)
                + SeichiAssist.seichiAssistConfig.getTitle3(4013) + "」")) {
          playerdata.displayTitle1No = 4013
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4013
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4013)
              + SeichiAssist.seichiAssistConfig.getTitle3(4013) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4014「" + SeichiAssist.seichiAssistConfig.getTitle1(4014)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4014) + "」")) {
          playerdata.displayTitle1No = 4014
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 4014
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4014)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(4014) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4015「" + SeichiAssist.seichiAssistConfig.getTitle1(4015) + "」")) {
          playerdata.displayTitle1No = 4015
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4015) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4016「" + SeichiAssist.seichiAssistConfig.getTitle1(4016)
                + SeichiAssist.seichiAssistConfig.getTitle3(4016) + "」")) {
          playerdata.displayTitle1No = 4016
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4016
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4016)
              + SeichiAssist.seichiAssistConfig.getTitle3(4016) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4017「" + SeichiAssist.seichiAssistConfig.getTitle1(4017) + "」")) {
          playerdata.displayTitle1No = 4017
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4017) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4018「" + SeichiAssist.seichiAssistConfig.getTitle1(4018)
                + SeichiAssist.seichiAssistConfig.getTitle3(4018) + "」")) {
          playerdata.displayTitle1No = 4018
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4018
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4018)
              + SeichiAssist.seichiAssistConfig.getTitle3(4018) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4019「" + SeichiAssist.seichiAssistConfig.getTitle1(4019)
                + SeichiAssist.seichiAssistConfig.getTitle3(4019) + "」")) {
          playerdata.displayTitle1No = 4019
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4019
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4019)
              + SeichiAssist.seichiAssistConfig.getTitle3(4019) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4020「" + SeichiAssist.seichiAssistConfig.getTitle1(4020)
                + SeichiAssist.seichiAssistConfig.getTitle3(4020) + "」")) {
          playerdata.displayTitle1No = 4020
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4020
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4020)
              + SeichiAssist.seichiAssistConfig.getTitle3(4020) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4021「" + SeichiAssist.seichiAssistConfig.getTitle1(4021)
                + SeichiAssist.seichiAssistConfig.getTitle3(4021) + "」")) {
          playerdata.displayTitle1No = 4021
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4021
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4021)
              + SeichiAssist.seichiAssistConfig.getTitle3(4021) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4022「" + SeichiAssist.seichiAssistConfig.getTitle1(4022)
                + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(4022) + "」")) {
          playerdata.displayTitle1No = 4022
          playerdata.displayTitle2No = 9903
          playerdata.displayTitle3No = 4022
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4022)
              + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(4022) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No4023「" + SeichiAssist.seichiAssistConfig.getTitle1(4023)
                + SeichiAssist.seichiAssistConfig.getTitle3(4023) + "」")) {
          playerdata.displayTitle1No = 4023
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 4023
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(4023)
              + SeichiAssist.seichiAssistConfig.getTitle3(4023) + "」が設定されました。")
        }
        player.openInventory(MenuInventoryData.getTitleTimeData(player))
      } else if (itemstackcurrent.type == Material.EMERALD_BLOCK) {
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


    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "実績「通算ログイン」") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      if (itemstackcurrent.type == Material.BEDROCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
        player.openInventory(MenuInventoryData.getTitleJoinAmountData(player))
      } else if (itemstackcurrent.type == Material.DIAMOND_BLOCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (itemmeta.displayName.contains("No5101「" + SeichiAssist.seichiAssistConfig.getTitle1(5101)
                + SeichiAssist.seichiAssistConfig.getTitle3(5101) + "」")) {
          playerdata.displayTitle1No = 5101
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 5101
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5101)
              + SeichiAssist.seichiAssistConfig.getTitle3(5101) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5102「" + SeichiAssist.seichiAssistConfig.getTitle1(5102)
                + SeichiAssist.seichiAssistConfig.getTitle2(9907) + SeichiAssist.seichiAssistConfig.getTitle3(5102) + "」")) {
          playerdata.displayTitle1No = 5102
          playerdata.displayTitle2No = 9907
          playerdata.displayTitle3No = 5102
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5102)
              + SeichiAssist.seichiAssistConfig.getTitle2(9907) + SeichiAssist.seichiAssistConfig.getTitle3(5102) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5103「" + SeichiAssist.seichiAssistConfig.getTitle1(5103)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」")) {
          playerdata.displayTitle1No = 5103
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5103)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5104「" + SeichiAssist.seichiAssistConfig.getTitle1(5104)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5104) + "」")) {
          playerdata.displayTitle1No = 5104
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 5104
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5104)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5104) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5105「" + SeichiAssist.seichiAssistConfig.getTitle1(5105)
                + SeichiAssist.seichiAssistConfig.getTitle2(9907) + SeichiAssist.seichiAssistConfig.getTitle3(5105) + "」")) {
          playerdata.displayTitle1No = 5105
          playerdata.displayTitle2No = 9907
          playerdata.displayTitle3No = 5105
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5105)
              + SeichiAssist.seichiAssistConfig.getTitle2(9907) + SeichiAssist.seichiAssistConfig.getTitle3(5105) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5106「" + SeichiAssist.seichiAssistConfig.getTitle1(5106) + "」")) {
          playerdata.displayTitle1No = 5106
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5106) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5107「" + SeichiAssist.seichiAssistConfig.getTitle1(5107)
                + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(5107) + "」")) {
          playerdata.displayTitle1No = 5107
          playerdata.displayTitle2No = 9909
          playerdata.displayTitle3No = 5107
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5107)
              + SeichiAssist.seichiAssistConfig.getTitle2(9909) + SeichiAssist.seichiAssistConfig.getTitle3(5107) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5108「" + SeichiAssist.seichiAssistConfig.getTitle1(5108)
                + SeichiAssist.seichiAssistConfig.getTitle3(5108) + "」")) {
          playerdata.displayTitle1No = 5108
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 5108
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5108)
              + SeichiAssist.seichiAssistConfig.getTitle3(5108) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5109「" + SeichiAssist.seichiAssistConfig.getTitle1(5109) + "」")) {
          playerdata.displayTitle1No = 5109
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5109) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5110「" + SeichiAssist.seichiAssistConfig.getTitle1(5110) + "」")) {
          playerdata.displayTitle1No = 5110
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5110) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5111「" + SeichiAssist.seichiAssistConfig.getTitle1(5111) + "」")) {
          playerdata.displayTitle1No = 5111
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5111) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5112「" + SeichiAssist.seichiAssistConfig.getTitle1(5112)
                + SeichiAssist.seichiAssistConfig.getTitle3(5112) + "」")) {
          playerdata.displayTitle1No = 5112
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 5112
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5112)
              + SeichiAssist.seichiAssistConfig.getTitle3(5112) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5113「" + SeichiAssist.seichiAssistConfig.getTitle1(5113)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5113) + "」")) {
          playerdata.displayTitle1No = 5113
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 5113
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5113)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5113) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5114「" + SeichiAssist.seichiAssistConfig.getTitle1(5114)
                + SeichiAssist.seichiAssistConfig.getTitle3(5114) + "」")) {
          playerdata.displayTitle1No = 5114
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 5114
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5114)
              + SeichiAssist.seichiAssistConfig.getTitle3(5114) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5115「" + SeichiAssist.seichiAssistConfig.getTitle1(5115) + "」")) {
          playerdata.displayTitle1No = 5115
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5115) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5116「" + SeichiAssist.seichiAssistConfig.getTitle1(5116)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5116) + "」")) {
          playerdata.displayTitle1No = 5116
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 5116
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5116)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5116) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5117「" + SeichiAssist.seichiAssistConfig.getTitle1(5117)
                + SeichiAssist.seichiAssistConfig.getTitle3(5117) + "」")) {
          playerdata.displayTitle1No = 5117
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 5117
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5117)
              + SeichiAssist.seichiAssistConfig.getTitle3(5117) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5118「" + SeichiAssist.seichiAssistConfig.getTitle1(5118)
                + SeichiAssist.seichiAssistConfig.getTitle3(5118) + "」")) {
          playerdata.displayTitle1No = 5118
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 5118
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5118)
              + SeichiAssist.seichiAssistConfig.getTitle3(5118) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5119「" + SeichiAssist.seichiAssistConfig.getTitle1(5119)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5119) + "」")) {
          playerdata.displayTitle1No = 5119
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 5119
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5119)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(5119) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5120「" + SeichiAssist.seichiAssistConfig.getTitle1(5120)
                + SeichiAssist.seichiAssistConfig.getTitle2(5120) + SeichiAssist.seichiAssistConfig.getTitle3(5120) + "」")) {
          playerdata.displayTitle1No = 5120
          playerdata.displayTitle2No = 5120
          playerdata.displayTitle3No = 5120
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5120)
              + SeichiAssist.seichiAssistConfig.getTitle2(5120) + SeichiAssist.seichiAssistConfig.getTitle3(5120) + "」が設定されました。")
        }

        player.openInventory(MenuInventoryData.getTitleJoinAmountData(player))

      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getTitleLogin(player))
        return
      }//実績メニューに戻る
    }


    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "実績「連続ログイン」") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      if (itemstackcurrent.type == Material.BEDROCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
        player.openInventory(MenuInventoryData.getTitleJoinChainData(player))
      } else if (itemstackcurrent.type == Material.DIAMOND_BLOCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (itemmeta.displayName.contains("No5001「" + SeichiAssist.seichiAssistConfig.getTitle1(5001)
                + SeichiAssist.seichiAssistConfig.getTitle2(5001) + "」")) {
          playerdata.displayTitle1No = 5001
          playerdata.displayTitle2No = 5001
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5001)
              + SeichiAssist.seichiAssistConfig.getTitle2(5001) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5002「" + SeichiAssist.seichiAssistConfig.getTitle1(5002)
                + SeichiAssist.seichiAssistConfig.getTitle3(5002) + "」")) {
          playerdata.displayTitle1No = 5002
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 5002
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5002)
              + SeichiAssist.seichiAssistConfig.getTitle3(5002) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5003「" + SeichiAssist.seichiAssistConfig.getTitle1(5003) + "」")) {
          playerdata.displayTitle1No = 5003
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5003) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5004「" + SeichiAssist.seichiAssistConfig.getTitle1(5004)
                + SeichiAssist.seichiAssistConfig.getTitle3(5004) + "」")) {
          playerdata.displayTitle1No = 5004
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 5004
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5004)
              + SeichiAssist.seichiAssistConfig.getTitle3(5004) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5005「" + SeichiAssist.seichiAssistConfig.getTitle1(5005)
                + SeichiAssist.seichiAssistConfig.getTitle3(5005) + "」")) {
          playerdata.displayTitle1No = 5005
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 5005
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5005)
              + SeichiAssist.seichiAssistConfig.getTitle3(5005) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5006「" + SeichiAssist.seichiAssistConfig.getTitle1(5006)
                + SeichiAssist.seichiAssistConfig.getTitle3(5006) + "」")) {
          playerdata.displayTitle1No = 5006
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 5006
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5006)
              + SeichiAssist.seichiAssistConfig.getTitle3(5006) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5007「" + SeichiAssist.seichiAssistConfig.getTitle1(5007) + "」")) {
          playerdata.displayTitle1No = 5007
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5007) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No5008「" + SeichiAssist.seichiAssistConfig.getTitle1(5008)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」")) {
          playerdata.displayTitle1No = 5008
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(5008)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」が設定されました。")
        }

        player.openInventory(MenuInventoryData.getTitleJoinChainData(player))

      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getTitleLogin(player))
        return
      }//実績メニューに戻る
    }

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "実績「JMS投票数」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      if (itemstackcurrent.type == Material.BEDROCK) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.sendMessage("この実績は自動解禁式です。毎分の処理をお待ちください。")
        player.openInventory(MenuInventoryData.getTitleSupportData(player))
      } else if (itemstackcurrent.type == Material.DIAMOND_BLOCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (itemmeta.displayName.contains("No6001「" + SeichiAssist.seichiAssistConfig.getTitle1(6001) + "」")) {
          playerdata.displayTitle1No = 6001
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(6001) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No6002「" + SeichiAssist.seichiAssistConfig.getTitle1(6002)
                + SeichiAssist.seichiAssistConfig.getTitle3(6002) + "」")) {
          playerdata.displayTitle1No = 6002
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 6002
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(6002)
              + SeichiAssist.seichiAssistConfig.getTitle3(6002) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No6003「" + SeichiAssist.seichiAssistConfig.getTitle1(6003) + "」")) {
          playerdata.displayTitle1No = 6003
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(6003) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No6004「" + SeichiAssist.seichiAssistConfig.getTitle1(6004)
                + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(6004) + "」")) {
          playerdata.displayTitle1No = 6004
          playerdata.displayTitle2No = 9903
          playerdata.displayTitle3No = 6004
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(6004)
              + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(6004) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No6005「" + SeichiAssist.seichiAssistConfig.getTitle1(6005)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」")) {
          playerdata.displayTitle1No = 6005
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(6005)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No6006「" + SeichiAssist.seichiAssistConfig.getTitle1(6006)
                + SeichiAssist.seichiAssistConfig.getTitle3(6006) + "」")) {
          playerdata.displayTitle1No = 6006
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 6006
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(6006)
              + SeichiAssist.seichiAssistConfig.getTitle3(6006) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No6007「" + SeichiAssist.seichiAssistConfig.getTitle1(6007)
                + SeichiAssist.seichiAssistConfig.getTitle2(9902) + "」")) {
          playerdata.displayTitle1No = 6007
          playerdata.displayTitle2No = 9902
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(6007)
              + SeichiAssist.seichiAssistConfig.getTitle2(9902) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No6008「" + SeichiAssist.seichiAssistConfig.getTitle1(6008) + "」")) {
          playerdata.displayTitle1No = 6008
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(6008) + "」が設定されました。")
        }
        player.openInventory(MenuInventoryData.getTitleSupportData(player))
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getTitleSpecial(player))
        return
      }//実績メニューに戻る
    }
    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "実績「公式イベント」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      if (itemstackcurrent.type == Material.BEDROCK) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.sendMessage("この実績は配布解禁式です。運営チームからの配布タイミングを逃さないようご注意ください。")
        player.openInventory(MenuInventoryData.getTitleEventData(player))
      } else if (itemstackcurrent.type == Material.DIAMOND_BLOCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (itemmeta.displayName.contains("No7001「" + SeichiAssist.seichiAssistConfig.getTitle1(7001)
                + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(7001) + "」")) {
          playerdata.displayTitle1No = 7001
          playerdata.displayTitle2No = 9901
          playerdata.displayTitle3No = 7001
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7001)
              + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(7001) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7002「" + SeichiAssist.seichiAssistConfig.getTitle1(7002)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7002) + "」")) {
          playerdata.displayTitle1No = 7002
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7002
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7002)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7002) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7003「" + SeichiAssist.seichiAssistConfig.getTitle1(7003)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7003) + "」")) {
          playerdata.displayTitle1No = 7003
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7003
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7003)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7003) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7004「" + SeichiAssist.seichiAssistConfig.getTitle2(7004) + "」")) {
          playerdata.displayTitle1No = 0
          playerdata.displayTitle2No = 7004
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle2(7004) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7005「" + SeichiAssist.seichiAssistConfig.getTitle1(7005)
                + SeichiAssist.seichiAssistConfig.getTitle2(9902) + SeichiAssist.seichiAssistConfig.getTitle3(7005) + "」")) {
          playerdata.displayTitle1No = 7005
          playerdata.displayTitle2No = 9902
          playerdata.displayTitle3No = 7005
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7005)
              + SeichiAssist.seichiAssistConfig.getTitle2(9902) + SeichiAssist.seichiAssistConfig.getTitle3(7005) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7006「" + SeichiAssist.seichiAssistConfig.getTitle1(7006)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7006) + "」")) {
          playerdata.displayTitle1No = 7006
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7006
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7006)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7006) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7007「" + SeichiAssist.seichiAssistConfig.getTitle1(7007)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7007) + "」")) {
          playerdata.displayTitle1No = 7007
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7007
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7007)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7007) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7008「" + SeichiAssist.seichiAssistConfig.getTitle1(7008)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7008) + "」")) {
          playerdata.displayTitle1No = 7008
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7008
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7008)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7008) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7009「" + SeichiAssist.seichiAssistConfig.getTitle1(7009)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7009) + "」")) {
          playerdata.displayTitle1No = 7009
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7009
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7009)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7009) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7010「" + SeichiAssist.seichiAssistConfig.getTitle1(7010)
                + SeichiAssist.seichiAssistConfig.getTitle3(7010) + "」")) {
          playerdata.displayTitle1No = 7010
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 7010
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7010)
              + SeichiAssist.seichiAssistConfig.getTitle3(7010) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7011「" + SeichiAssist.seichiAssistConfig.getTitle1(7011)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7011) + "」")) {
          playerdata.displayTitle1No = 7011
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7011
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7011)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7011) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7012「" + SeichiAssist.seichiAssistConfig.getTitle1(7012)
                + SeichiAssist.seichiAssistConfig.getTitle3(7012) + "」")) {
          playerdata.displayTitle1No = 7012
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 7012
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7012)
              + SeichiAssist.seichiAssistConfig.getTitle3(7012) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7013「" + SeichiAssist.seichiAssistConfig.getTitle1(7013) + "」")) {
          playerdata.displayTitle1No = 7013
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7013) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7014「" + SeichiAssist.seichiAssistConfig.getTitle1(7014) + "」")) {
          playerdata.displayTitle1No = 7014
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7014) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7015「" + SeichiAssist.seichiAssistConfig.getTitle1(7015)
                + SeichiAssist.seichiAssistConfig.getTitle3(9904) + SeichiAssist.seichiAssistConfig.getTitle3(7015) + "」")) {
          playerdata.displayTitle1No = 7015
          playerdata.displayTitle2No = 9904
          playerdata.displayTitle3No = 7015
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7015)
              + SeichiAssist.seichiAssistConfig.getTitle3(9904) + SeichiAssist.seichiAssistConfig.getTitle3(7015) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7016「" + SeichiAssist.seichiAssistConfig.getTitle1(7016)
                + SeichiAssist.seichiAssistConfig.getTitle3(7016) + "」")) {
          playerdata.displayTitle1No = 7016
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 7016
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7016)
              + SeichiAssist.seichiAssistConfig.getTitle3(7016) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7017「" + SeichiAssist.seichiAssistConfig.getTitle1(7017)
                + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7017) + "」")) {
          playerdata.displayTitle1No = 7017
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7017
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7017)
              + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7017) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7018「" + SeichiAssist.seichiAssistConfig.getTitle1(7018)
                + SeichiAssist.seichiAssistConfig.getTitle3(9904) + SeichiAssist.seichiAssistConfig.getTitle3(7018) + "」")) {
          playerdata.displayTitle1No = 7018
          playerdata.displayTitle2No = 9904
          playerdata.displayTitle3No = 7018
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7018)
              + SeichiAssist.seichiAssistConfig.getTitle3(9904) + SeichiAssist.seichiAssistConfig.getTitle3(7018) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7019「" + SeichiAssist.seichiAssistConfig.getTitle1(7019)
                + SeichiAssist.seichiAssistConfig.getTitle3(7019) + "」")) {
          playerdata.displayTitle1No = 7019
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 7019
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7019)
              + SeichiAssist.seichiAssistConfig.getTitle3(7019) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7020「" + SeichiAssist.seichiAssistConfig.getTitle1(7020)
                + SeichiAssist.seichiAssistConfig.getTitle3(7020) + "」")) {
          playerdata.displayTitle1No = 7020
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 7020
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7020)
              + SeichiAssist.seichiAssistConfig.getTitle3(7020) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7021「" + SeichiAssist.seichiAssistConfig.getTitle1(7021)
                + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7021) + "」")) {
          playerdata.displayTitle1No = 7021
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7021
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7021)
              + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7021) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7022「" + SeichiAssist.seichiAssistConfig.getTitle1(7022)
                + SeichiAssist.seichiAssistConfig.getTitle3(7022) + "」")) {
          playerdata.displayTitle1No = 7022
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 7022
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7022)
              + SeichiAssist.seichiAssistConfig.getTitle3(7022) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7023「" + SeichiAssist.seichiAssistConfig.getTitle1(7023)
                + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7023) + "」")) {
          playerdata.displayTitle1No = 7023
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7023
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7023)
              + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7023) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7024「" + SeichiAssist.seichiAssistConfig.getTitle1(7024)
                + SeichiAssist.seichiAssistConfig.getTitle3(7024) + "」")) {
          playerdata.displayTitle1No = 7024
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 7024
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7024)
              + SeichiAssist.seichiAssistConfig.getTitle3(7024) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7025「" + SeichiAssist.seichiAssistConfig.getTitle1(7025)
                + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7025) + "」")) {
          playerdata.displayTitle1No = 7025
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7025
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7025)
              + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7025) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7026「" + SeichiAssist.seichiAssistConfig.getTitle1(7026)
                + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7026) + "」")) {
          playerdata.displayTitle1No = 7026
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7026
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7026)
              + SeichiAssist.seichiAssistConfig.getTitle3(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7026) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7027「" + SeichiAssist.seichiAssistConfig.getTitle1(7027)
                + SeichiAssist.seichiAssistConfig.getTitle3(7027) + "」")) {
          playerdata.displayTitle1No = 7027
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 7027
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7027)
              + SeichiAssist.seichiAssistConfig.getTitle3(7027) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7901「" + SeichiAssist.seichiAssistConfig.getTitle1(7901)
                + SeichiAssist.seichiAssistConfig.getTitle2(7901) + SeichiAssist.seichiAssistConfig.getTitle3(7901) + "」")) {
          playerdata.displayTitle1No = 7901
          playerdata.displayTitle2No = 7901
          playerdata.displayTitle3No = 7901
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7901)
              + SeichiAssist.seichiAssistConfig.getTitle2(7901) + SeichiAssist.seichiAssistConfig.getTitle3(7901) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7902「" + SeichiAssist.seichiAssistConfig.getTitle1(7902)
                + SeichiAssist.seichiAssistConfig.getTitle3(7902) + "」")) {
          playerdata.displayTitle1No = 7902
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 7902
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7902)
              + SeichiAssist.seichiAssistConfig.getTitle3(7902) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7903「" + SeichiAssist.seichiAssistConfig.getTitle1(7903)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7903) + "」")) {
          playerdata.displayTitle1No = 7903
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 7903
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7903)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(7903) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7904「" + SeichiAssist.seichiAssistConfig.getTitle1(7904)
                + SeichiAssist.seichiAssistConfig.getTitle2(9907) + SeichiAssist.seichiAssistConfig.getTitle3(7904) + "」")) {
          playerdata.displayTitle1No = 7904
          playerdata.displayTitle2No = 9907
          playerdata.displayTitle3No = 7904
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7904)
              + SeichiAssist.seichiAssistConfig.getTitle2(9907) + SeichiAssist.seichiAssistConfig.getTitle3(7904) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7905「" + SeichiAssist.seichiAssistConfig.getTitle1(7905)
                + SeichiAssist.seichiAssistConfig.getTitle3(7905) + "」")) {
          playerdata.displayTitle1No = 7905
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 7905
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7905)
              + SeichiAssist.seichiAssistConfig.getTitle3(7905) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No7906「" + SeichiAssist.seichiAssistConfig.getTitle1(7906)
                + SeichiAssist.seichiAssistConfig.getTitle3(7906) + "」")) {
          playerdata.displayTitle1No = 7906
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 7906
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(7906)
              + SeichiAssist.seichiAssistConfig.getTitle3(7906) + "」が設定されました。")
        }
        player.openInventory(MenuInventoryData.getTitleEventData(player))

      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getTitleSpecial(player))
        return
      }//実績メニューに戻る
    }


    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "実績「記念日」") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.type == Material.BEDROCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (itemmeta.displayName.contains("No9001「???」")) {
          SeichiAchievement.tryAchieve(player, 9001)
        } else if (itemmeta.displayName.contains("No9002「???」")) {
          SeichiAchievement.tryAchieve(player, 9002)
        } else if (itemmeta.displayName.contains("No9003「???」")) {
          SeichiAchievement.tryAchieve(player, 9003)
        } else if (itemmeta.displayName.contains("No9004「???」")) {
          SeichiAchievement.tryAchieve(player, 9004)
        } else if (itemmeta.displayName.contains("No9005「???」")) {
          SeichiAchievement.tryAchieve(player, 9005)
        } else if (itemmeta.displayName.contains("No9006「???」")) {
          SeichiAchievement.tryAchieve(player, 9006)
        } else if (itemmeta.displayName.contains("No9007「???」")) {
          SeichiAchievement.tryAchieve(player, 9007)
        } else if (itemmeta.displayName.contains("No9008「???」")) {
          SeichiAchievement.tryAchieve(player, 9008)
        } else if (itemmeta.displayName.contains("No9009「???」")) {
          SeichiAchievement.tryAchieve(player, 9009)
        } else if (itemmeta.displayName.contains("No9010「???」")) {
          SeichiAchievement.tryAchieve(player, 9010)
        } else if (itemmeta.displayName.contains("No9011「???」")) {
          SeichiAchievement.tryAchieve(player, 9011)
        } else if (itemmeta.displayName.contains("No9012「???」")) {
          SeichiAchievement.tryAchieve(player, 9012)
        } else if (itemmeta.displayName.contains("No9013「???」")) {
          SeichiAchievement.tryAchieve(player, 9013)
        } else if (itemmeta.displayName.contains("No9014「???」")) {
          SeichiAchievement.tryAchieve(player, 9014)
        } else if (itemmeta.displayName.contains("No9015「???」")) {
          SeichiAchievement.tryAchieve(player, 9015)
        } else if (itemmeta.displayName.contains("No9016「???」")) {
          SeichiAchievement.tryAchieve(player, 9016)
        } else if (itemmeta.displayName.contains("No9017「???」")) {
          SeichiAchievement.tryAchieve(player, 9017)
        } else if (itemmeta.displayName.contains("No9018「???」")) {
          SeichiAchievement.tryAchieve(player, 9018)
        } else if (itemmeta.displayName.contains("No9019「???」")) {
          SeichiAchievement.tryAchieve(player, 9019)
        } else if (itemmeta.displayName.contains("No9020「???」")) {
          SeichiAchievement.tryAchieve(player, 9020)
        } else if (itemmeta.displayName.contains("No9021「???」")) {
          SeichiAchievement.tryAchieve(player, 9021)
        } else if (itemmeta.displayName.contains("No9022「???」")) {
          SeichiAchievement.tryAchieve(player, 9022)
        } else if (itemmeta.displayName.contains("No9023「???」")) {
          SeichiAchievement.tryAchieve(player, 9023)
        } else if (itemmeta.displayName.contains("No9024「???」")) {
          SeichiAchievement.tryAchieve(player, 9024)
        } else if (itemmeta.displayName.contains("No9025「???」")) {
          SeichiAchievement.tryAchieve(player, 9025)
        } else if (itemmeta.displayName.contains("No9026「???」")) {
          SeichiAchievement.tryAchieve(player, 9026)
        } else if (itemmeta.displayName.contains("No9027「???」")) {
          SeichiAchievement.tryAchieve(player, 9027)
        } else if (itemmeta.displayName.contains("No9028「???」")) {
          SeichiAchievement.tryAchieve(player, 9028)
        } else if (itemmeta.displayName.contains("No9029「???」")) {
          SeichiAchievement.tryAchieve(player, 9029)
        } else if (itemmeta.displayName.contains("No9030「???」")) {
          SeichiAchievement.tryAchieve(player, 9030)
        } else if (itemmeta.displayName.contains("No9031「???」")) {
          SeichiAchievement.tryAchieve(player, 9031)
        } else if (itemmeta.displayName.contains("No9032「???」")) {
          SeichiAchievement.tryAchieve(player, 9032)
        } else if (itemmeta.displayName.contains("No9033「???」")) {
          SeichiAchievement.tryAchieve(player, 9033)
        } else if (itemmeta.displayName.contains("No9034「???」")) {
          SeichiAchievement.tryAchieve(player, 9034)
        } else if (itemmeta.displayName.contains("No9035「???」")) {
          SeichiAchievement.tryAchieve(player, 9035)
        } else if (itemmeta.displayName.contains("No9036「???」")) {
          SeichiAchievement.tryAchieve(player, 9036)
        }

        player.openInventory(MenuInventoryData.getTitleExtraData(player))
      } else if (itemstackcurrent.type == Material.DIAMOND_BLOCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (itemmeta.displayName.contains("No9001「" + SeichiAssist.seichiAssistConfig.getTitle1(9001) + "」")) {
          playerdata.displayTitle1No = 9001
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9001) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9002「" + SeichiAssist.seichiAssistConfig.getTitle1(9002)
                + SeichiAssist.seichiAssistConfig.getTitle3(9002) + "」")) {
          playerdata.displayTitle1No = 9002
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9002
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9002)
              + SeichiAssist.seichiAssistConfig.getTitle3(9002) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9003「" + SeichiAssist.seichiAssistConfig.getTitle1(9003) + "」")) {
          playerdata.displayTitle1No = 9003
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9003) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9004「" + SeichiAssist.seichiAssistConfig.getTitle1(9004)
                + SeichiAssist.seichiAssistConfig.getTitle2(9004) + SeichiAssist.seichiAssistConfig.getTitle3(9004) + "」")) {
          playerdata.displayTitle1No = 9004
          playerdata.displayTitle2No = 9004
          playerdata.displayTitle3No = 9004
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9004)
              + SeichiAssist.seichiAssistConfig.getTitle2(9004) + SeichiAssist.seichiAssistConfig.getTitle3(9004) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9005「" + SeichiAssist.seichiAssistConfig.getTitle1(9005)
                + SeichiAssist.seichiAssistConfig.getTitle3(9005) + "」")) {
          playerdata.displayTitle1No = 9005
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9005
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9005)
              + SeichiAssist.seichiAssistConfig.getTitle3(9005) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9006「" + SeichiAssist.seichiAssistConfig.getTitle1(9006) + "」")) {
          playerdata.displayTitle1No = 9006
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9006) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9007「" + SeichiAssist.seichiAssistConfig.getTitle1(9007) + "」")) {
          playerdata.displayTitle1No = 9007
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9007) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9008「" + SeichiAssist.seichiAssistConfig.getTitle1(9008)
                + SeichiAssist.seichiAssistConfig.getTitle3(9008) + "」")) {
          playerdata.displayTitle1No = 9008
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9008
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9008)
              + SeichiAssist.seichiAssistConfig.getTitle3(9008) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9009「" + SeichiAssist.seichiAssistConfig.getTitle1(9009) + "」")) {
          playerdata.displayTitle1No = 9009
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9009) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9010「" + SeichiAssist.seichiAssistConfig.getTitle1(9010)
                + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(9010) + "」")) {
          playerdata.displayTitle1No = 9010
          playerdata.displayTitle2No = 9903
          playerdata.displayTitle3No = 9010
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9010)
              + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(9010) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9011「" + SeichiAssist.seichiAssistConfig.getTitle1(9011)
                + SeichiAssist.seichiAssistConfig.getTitle3(9011) + "」")) {
          playerdata.displayTitle1No = 9011
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9011
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9011)
              + SeichiAssist.seichiAssistConfig.getTitle3(9011) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9012「" + SeichiAssist.seichiAssistConfig.getTitle1(9012)
                + SeichiAssist.seichiAssistConfig.getTitle3(9012) + "」")) {
          playerdata.displayTitle1No = 9012
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9012
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9012)
              + SeichiAssist.seichiAssistConfig.getTitle3(9012) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9013「" + SeichiAssist.seichiAssistConfig.getTitle1(9013) + "」")) {
          playerdata.displayTitle1No = 9013
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9013) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9014「" + SeichiAssist.seichiAssistConfig.getTitle2(9014) + "」")) {
          playerdata.displayTitle1No = 0
          playerdata.displayTitle2No = 9014
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle2(9014) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9015「" + SeichiAssist.seichiAssistConfig.getTitle1(9015)
                + SeichiAssist.seichiAssistConfig.getTitle3(9015) + "」")) {
          playerdata.displayTitle1No = 9015
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9015
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9015)
              + SeichiAssist.seichiAssistConfig.getTitle3(9015) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9016「" + SeichiAssist.seichiAssistConfig.getTitle1(9016)
                + SeichiAssist.seichiAssistConfig.getTitle2(9016) + SeichiAssist.seichiAssistConfig.getTitle3(9016) + "」")) {
          playerdata.displayTitle1No = 9016
          playerdata.displayTitle2No = 9016
          playerdata.displayTitle3No = 9016
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9016)
              + SeichiAssist.seichiAssistConfig.getTitle2(9016) + SeichiAssist.seichiAssistConfig.getTitle3(9016) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9017「" + SeichiAssist.seichiAssistConfig.getTitle1(9017)
                + SeichiAssist.seichiAssistConfig.getTitle3(9017) + "」")) {
          playerdata.displayTitle1No = 9017
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9017
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9017)
              + SeichiAssist.seichiAssistConfig.getTitle3(9017) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9018「" + SeichiAssist.seichiAssistConfig.getTitle1(9018) + "」")) {
          playerdata.displayTitle1No = 9018
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 0
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9018) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9019「" + SeichiAssist.seichiAssistConfig.getTitle1(9019)
                + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(9019) + "」")) {
          playerdata.displayTitle1No = 9019
          playerdata.displayTitle2No = 9901
          playerdata.displayTitle3No = 9019
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9019)
              + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(9019) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9020「" + SeichiAssist.seichiAssistConfig.getTitle1(9020)
                + SeichiAssist.seichiAssistConfig.getTitle3(9020) + "」")) {
          playerdata.displayTitle1No = 9020
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9020
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9020)
              + SeichiAssist.seichiAssistConfig.getTitle3(9020) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9021「" + SeichiAssist.seichiAssistConfig.getTitle1(9021)
                + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(9021) + "」")) {
          playerdata.displayTitle1No = 9021
          playerdata.displayTitle2No = 9901
          playerdata.displayTitle3No = 9021
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9021)
              + SeichiAssist.seichiAssistConfig.getTitle2(9901) + SeichiAssist.seichiAssistConfig.getTitle3(9021) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9022「" + SeichiAssist.seichiAssistConfig.getTitle1(9022)
                + SeichiAssist.seichiAssistConfig.getTitle3(9022) + "」")) {
          playerdata.displayTitle1No = 9022
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9022
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9022)
              + SeichiAssist.seichiAssistConfig.getTitle3(9022) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9023「" + SeichiAssist.seichiAssistConfig.getTitle1(9023)
                + SeichiAssist.seichiAssistConfig.getTitle3(9023) + "」")) {
          playerdata.displayTitle1No = 9023
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9023
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9023)
              + SeichiAssist.seichiAssistConfig.getTitle3(9023) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9024「" + SeichiAssist.seichiAssistConfig.getTitle1(9024)
                + SeichiAssist.seichiAssistConfig.getTitle3(9024) + "」")) {
          playerdata.displayTitle1No = 9024
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9024
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9024)
              + SeichiAssist.seichiAssistConfig.getTitle3(9024) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9025「" + SeichiAssist.seichiAssistConfig.getTitle1(9025)
                + SeichiAssist.seichiAssistConfig.getTitle2(9025) + SeichiAssist.seichiAssistConfig.getTitle3(9025) + "」")) {
          playerdata.displayTitle1No = 9025
          playerdata.displayTitle2No = 9025
          playerdata.displayTitle3No = 9025
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9025)
              + SeichiAssist.seichiAssistConfig.getTitle2(9025) + SeichiAssist.seichiAssistConfig.getTitle3(9025) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9026「" + SeichiAssist.seichiAssistConfig.getTitle1(9026)
                + SeichiAssist.seichiAssistConfig.getTitle3(9026) + "」")) {
          playerdata.displayTitle1No = 9026
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9026
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9026)
              + SeichiAssist.seichiAssistConfig.getTitle3(9026) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9027「" + SeichiAssist.seichiAssistConfig.getTitle1(9027)
                + SeichiAssist.seichiAssistConfig.getTitle3(9027) + "」")) {
          playerdata.displayTitle1No = 9027
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9027
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9027)
              + SeichiAssist.seichiAssistConfig.getTitle3(9027) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9028「" + SeichiAssist.seichiAssistConfig.getTitle1(9028)
                + SeichiAssist.seichiAssistConfig.getTitle2(9028) + SeichiAssist.seichiAssistConfig.getTitle3(9028) + "」")) {
          playerdata.displayTitle1No = 9028
          playerdata.displayTitle2No = 9028
          playerdata.displayTitle3No = 9028
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9028)
              + SeichiAssist.seichiAssistConfig.getTitle2(9028) + SeichiAssist.seichiAssistConfig.getTitle3(9028) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9029「" + SeichiAssist.seichiAssistConfig.getTitle1(9029)
                + SeichiAssist.seichiAssistConfig.getTitle2(9029) + SeichiAssist.seichiAssistConfig.getTitle3(9029) + "」")) {
          playerdata.displayTitle1No = 9029
          playerdata.displayTitle2No = 9029
          playerdata.displayTitle3No = 9029
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9029)
              + SeichiAssist.seichiAssistConfig.getTitle2(9029) + SeichiAssist.seichiAssistConfig.getTitle3(9029) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9030「" + SeichiAssist.seichiAssistConfig.getTitle1(9030)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(9030) + "」")) {
          playerdata.displayTitle1No = 9030
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 9030
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9030)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(9030) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9031「" + SeichiAssist.seichiAssistConfig.getTitle1(9031)
                + SeichiAssist.seichiAssistConfig.getTitle2(9908) + SeichiAssist.seichiAssistConfig.getTitle3(9031) + "」")) {
          playerdata.displayTitle1No = 9031
          playerdata.displayTitle2No = 9908
          playerdata.displayTitle3No = 9031
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9031)
              + SeichiAssist.seichiAssistConfig.getTitle2(9908) + SeichiAssist.seichiAssistConfig.getTitle3(9031) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9032「" + SeichiAssist.seichiAssistConfig.getTitle1(9032)
                + SeichiAssist.seichiAssistConfig.getTitle3(9032) + "」")) {
          playerdata.displayTitle1No = 9032
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9032
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9032)
              + SeichiAssist.seichiAssistConfig.getTitle3(9032) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9033「" + SeichiAssist.seichiAssistConfig.getTitle1(9033)
                + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(9033) + "」")) {
          playerdata.displayTitle1No = 9033
          playerdata.displayTitle2No = 9903
          playerdata.displayTitle3No = 9033
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9033)
              + SeichiAssist.seichiAssistConfig.getTitle2(9903) + SeichiAssist.seichiAssistConfig.getTitle3(9033) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9034「" + SeichiAssist.seichiAssistConfig.getTitle1(9034)
                + SeichiAssist.seichiAssistConfig.getTitle3(9034) + "」")) {
          playerdata.displayTitle1No = 9034
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9034
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9034)
              + SeichiAssist.seichiAssistConfig.getTitle3(9034) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9035「" + SeichiAssist.seichiAssistConfig.getTitle1(9035)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(9035) + "」")) {
          playerdata.displayTitle1No = 9035
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 9035
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9035)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(9035) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No9036「" + SeichiAssist.seichiAssistConfig.getTitle1(9036)
                + SeichiAssist.seichiAssistConfig.getTitle3(9036) + "」")) {
          playerdata.displayTitle1No = 9036
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 9036
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(9036)
              + SeichiAssist.seichiAssistConfig.getTitle3(9036) + "」が設定されました。")
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
    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "実績「極秘任務」") {
      event.isCancelled = true

      //実績解除処理部分の読みこみ
      //TitleUnlockTaskRunnable TUTR = new TitleUnlockTaskRunnable() ;
      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      if (itemstackcurrent.type == Material.BEDROCK) {
        //ItemMeta itemmeta = itemstackcurrent.getItemMeta();
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.sendMessage("この実績は「極秘実績」です。いろいろやってみましょう！")
        player.openInventory(MenuInventoryData.getTitleSecretData(player))
      } else if (itemstackcurrent.type == Material.DIAMOND_BLOCK) {
        val itemmeta = itemstackcurrent.itemMeta
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (itemmeta.displayName.contains("No8001「" + SeichiAssist.seichiAssistConfig.getTitle1(8001)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(8001) + "」")) {
          playerdata.displayTitle1No = 8001
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 8001
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(8001)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(8001) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No8002「" + SeichiAssist.seichiAssistConfig.getTitle1(8002)
                + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(8002) + "」")) {
          playerdata.displayTitle1No = 8002
          playerdata.displayTitle2No = 9905
          playerdata.displayTitle3No = 8002
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(8002)
              + SeichiAssist.seichiAssistConfig.getTitle2(9905) + SeichiAssist.seichiAssistConfig.getTitle3(8002) + "」が設定されました。")
        } else if (itemmeta.displayName.contains("No8003「" + SeichiAssist.seichiAssistConfig.getTitle1(8003)
                + SeichiAssist.seichiAssistConfig.getTitle3(8003) + "」")) {
          playerdata.displayTitle1No = 8003
          playerdata.displayTitle2No = 0
          playerdata.displayTitle3No = 8003
          player.sendMessage("二つ名「" + SeichiAssist.seichiAssistConfig.getTitle1(8003)
              + SeichiAssist.seichiAssistConfig.getTitle3(8003) + "」が設定されました。")
        }
        player.openInventory(MenuInventoryData.getTitleSecretData(player))

      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getTitleSpecial(player))
      }//実績メニューに戻る
    }
  }

  //鉱石・交換券変換システム
  @EventHandler
  fun onOreTradeEvent(event: InventoryCloseEvent) {
    val player = event.player as Player
    val uuid = player.uniqueId
    val playerdata = playermap[uuid] ?: return
    //エラー分岐
    val inventory = event.inventory

    //インベントリサイズが36でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.title == ChatColor.LIGHT_PURPLE.toString() + "" + ChatColor.BOLD + "交換したい鉱石を入れてください") {
      var giveticket = 0
      /*
			 * step1 for文でinventory内の対象商品の個数を計算
			 * 非対象商品は返却boxへ
			 */
      //ガチャ景品交換インベントリの中身を取得
      val item = inventory.contents
      //ドロップ用アイテムリスト(返却box)作成
      val dropitem = ArrayList<ItemStack>()
      //余剰鉱石返却用アイテムリスト
      val retore = ArrayList<ItemStack>()
      //個数計算用変数(このやり方以外に効率的なやり方があるかもしれません)
      var coalore = 0 //石炭
      var ironore = 0 //鉄
      var goldore = 0 //金
      var lapisore = 0 //ラピスラズリ
      var diamondore = 0 //ダイアモンド
      var redstoneore = 0 //レッドストーン
      var emeraldore = 0 //エメラルド
      var quartzore = 0 //ネザー水晶
      //for文でインベントリ内のアイテムを1つずつ見る
      //鉱石・交換券変換インベントリスロットを1つずつ見る
      for (m in item) {
        //ないなら次へ
        if (m == null) {
          continue
        } else if (m.type == Material.COAL_ORE) {
          //石炭なら個数分だけcoaloreを増やす(以下同様)
          coalore += m.amount
          continue
        } else if (m.type == Material.IRON_ORE) {
          ironore += m.amount
          continue
        } else if (m.type == Material.GOLD_ORE) {
          goldore += m.amount
          continue
        } else if (m.type == Material.LAPIS_ORE) {
          lapisore += m.amount
          continue
        } else if (m.type == Material.DIAMOND_ORE) {
          diamondore += m.amount
          continue
        } else if (m.type == Material.REDSTONE_ORE) {
          redstoneore += m.amount
          continue
        } else if (m.type == Material.EMERALD_ORE) {
          emeraldore += m.amount
          continue
        } else if (m.type == Material.QUARTZ_ORE) {
          quartzore += m.amount
          continue
        } else {
          dropitem.add(m)
        }
      }
      //チケット計算
      giveticket = giveticket + coalore / 128 + ironore / 64 + goldore / 8 + lapisore / 8 + diamondore / 4 + redstoneore / 32 + emeraldore / 4 + quartzore / 16

      //プレイヤー通知
      if (giveticket == 0) {
        player.sendMessage(ChatColor.YELLOW.toString() + "鉱石を認識しなかったか数が不足しています。全てのアイテムを返却します")
      } else {
        player.sendMessage(ChatColor.DARK_RED.toString() + "交換券" + ChatColor.RESET + "" + ChatColor.GREEN + "を" + giveticket + "枚付与しました")
      }
      /*
			 * step2 交換券をインベントリへ
			 */
      val exchangeticket = ItemStack(Material.PAPER)
      val itemmeta = Bukkit.getItemFactory().getItemMeta(Material.PAPER)
      itemmeta.displayName = ChatColor.DARK_RED.toString() + "" + ChatColor.BOLD + "交換券"
      itemmeta.addEnchant(Enchantment.PROTECTION_FIRE, 1, false)
      itemmeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
      exchangeticket.itemMeta = itemmeta

      var count = 0
      while (giveticket > 0) {
        if (player.inventory.contains(exchangeticket) || !Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, exchangeticket)
        } else {
          Util.dropItem(player, exchangeticket)
        }
        giveticket--
        count++
      }
      if (count > 0) {
        player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
        player.sendMessage(ChatColor.GREEN.toString() + "交換券の付与が終わりました")
      }
      /*
			 * step3 非対象商品・余剰鉱石の返却
			 */
      if (coalore - coalore / 128 * 128 != 0) {
        val c = ItemStack(Material.COAL_ORE)
        val citemmeta = Bukkit.getItemFactory().getItemMeta(Material.COAL_ORE)
        c.itemMeta = citemmeta
        c.amount = coalore - coalore / 128 * 128
        retore.add(c)
      }

      if (ironore - ironore / 64 * 64 != 0) {
        val i = ItemStack(Material.IRON_ORE)
        val iitemmeta = Bukkit.getItemFactory().getItemMeta(Material.IRON_ORE)
        i.itemMeta = iitemmeta
        i.amount = ironore - ironore / 64 * 64
        retore.add(i)
      }

      if (goldore - goldore / 8 * 8 != 0) {
        val g = ItemStack(Material.GOLD_ORE)
        val gitemmeta = Bukkit.getItemFactory().getItemMeta(Material.GOLD_ORE)
        g.itemMeta = gitemmeta
        g.amount = goldore - goldore / 8 * 8
        retore.add(g)
      }

      if (lapisore - lapisore / 8 * 8 != 0) {
        val l = ItemStack(Material.LAPIS_ORE)
        val litemmeta = Bukkit.getItemFactory().getItemMeta(Material.LAPIS_ORE)
        l.itemMeta = litemmeta
        l.amount = lapisore - lapisore / 8 * 8
        retore.add(l)
      }

      if (diamondore - diamondore / 4 * 4 != 0) {
        val d = ItemStack(Material.DIAMOND_ORE)
        val ditemmeta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_ORE)
        d.itemMeta = ditemmeta
        d.amount = diamondore - diamondore / 4 * 4
        retore.add(d)
      }

      if (redstoneore - redstoneore / 32 * 32 != 0) {
        val r = ItemStack(Material.REDSTONE_ORE)
        val ritemmeta = Bukkit.getItemFactory().getItemMeta(Material.REDSTONE_ORE)
        r.itemMeta = ritemmeta
        r.amount = redstoneore - redstoneore / 32 * 32
        retore.add(r)
      }

      if (emeraldore - emeraldore / 4 * 4 != 0) {
        val e = ItemStack(Material.EMERALD_ORE)
        val eitemmeta = Bukkit.getItemFactory().getItemMeta(Material.EMERALD_ORE)
        e.itemMeta = eitemmeta
        e.amount = emeraldore - emeraldore / 4 * 4
        retore.add(e)
      }

      if (quartzore - quartzore / 16 * 16 != 0) {
        val q = ItemStack(Material.QUARTZ_ORE)
        val qitemmeta = Bukkit.getItemFactory().getItemMeta(Material.QUARTZ_ORE)
        q.itemMeta = qitemmeta
        q.amount = quartzore - quartzore / 16 * 16
        retore.add(q)
      }

      //返却処理
      for (m in dropitem) {
        if (!Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, m)
        } else {
          Util.dropItem(player, m)
        }
      }
      for (m in retore) {
        if (!Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, m)
        } else {
          Util.dropItem(player, m)
        }
      }
    }
  }

  //ギガンティック→椎名林檎交換システム
  @EventHandler
  fun onGachaRingoEvent(event: InventoryCloseEvent) {
    val player = event.player as Player
    val uuid = player.uniqueId
    val playerdata = playermap[uuid] ?: return
    //エラー分岐
    val name = playerdata.name
    val inventory = event.inventory

    //インベントリサイズが4列でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.title == ChatColor.GOLD.toString() + "" + ChatColor.BOLD + "椎名林檎と交換したい景品を入れてネ") {
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
        } else if (m.type == Material.SKULL_ITEM) {
          //丁重にお返しする
          dropitem.add(m)
          continue
        }
        //ガチャ景品リストにアイテムがあった時にtrueになるフラグ
        var flag = false
        //ガチャ景品リストを一個ずつ見ていくfor文
        for (gachadata in gachadatalist) {
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
        player.sendMessage(ChatColor.RED.toString() + "ガチャシステムメンテナンス中の為全てのアイテムを返却します")
      } else if (giga <= 0) {
        player.sendMessage(ChatColor.YELLOW.toString() + "ギガンティック大当り景品を認識しませんでした。全てのアイテムを返却します")
      } else {
        player.sendMessage(ChatColor.GREEN.toString() + "ギガンティック大当り景品を" + giga + "個認識しました")
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
        player.sendMessage(ChatColor.GREEN.toString() + "" + count + "個の" + ChatColor.GOLD + "椎名林檎" + ChatColor.WHITE + "を受け取りました")
      }
    }

  }

  @EventHandler
  fun onTitanRepairEvent(event: InventoryCloseEvent) {
    val player = event.player as Player
    val uuid = player.uniqueId
    val playerdata = playermap[uuid] ?: return
    //エラー分岐
    val inventory = event.inventory

    //インベントリサイズが36でない時終了
    if (inventory.row != 4) {
      return
    }
    if (inventory.title == ChatColor.GOLD.toString() + "" + ChatColor.BOLD + "修繕したい限定タイタンを入れてネ") {
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
        player.sendMessage(ChatColor.GREEN.toString() + "限定タイタンを認識しませんでした。すべてのアイテムを返却します")
      } else {
        player.sendMessage(ChatColor.GREEN.toString() + "限定タイタンを" + count + "個認識し、修繕しました。")
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
    if (he.type != EntityType.PLAYER) {
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
    val playerdata = playermap[uuid]!!

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "投票ptメニュー") {
      event.isCancelled = true

      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }

      val isSkull = itemstackcurrent.type == Material.SKULL_ITEM

      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */

      //投票pt受取
      if (itemstackcurrent.type == Material.DIAMOND) {
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

        player.sendMessage(ChatColor.GOLD.toString() + "投票特典" + ChatColor.WHITE + "(" + count + "票分)を受け取りました")
        player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)

        val itemmeta = itemstackcurrent.itemMeta
        itemstackcurrent.itemMeta = itemmeta
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.type == Material.BOOK_AND_QUILL) {
        // 投票リンク表示
        player.sendMessage(ChatColor.RED.toString() + "" + ChatColor.UNDERLINE + "https://minecraft.jp/servers/54d3529e4ddda180780041a7/vote")
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.closeInventory()
      } else if (isSkull && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        GlobalScope.launch(Schedulers.async) {
          sequentialEffect(
              FocusedSoundEffect(Sound.BLOCK_FENCE_GATE_OPEN, 1.0f, 0.1f),
              StickMenu.firstPage.open
          ).runFor(player)
        }
      } else if (itemstackcurrent.type == Material.WATCH) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleVotingFairy = playerdata.toggleVotingFairy % 4 + 1
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.type == Material.PAPER) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleGiveApple = playerdata.toggleGiveApple % 4 + 1
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.type == Material.JUKEBOX) {
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        playerdata.toggleVFSound = !playerdata.toggleVFSound
        player.openInventory(MenuInventoryData.getVotingMenuData(player))
      } else if (itemstackcurrent.type == Material.GHAST_TEAR) {
        player.closeInventory()

        //プレイヤーレベルが10に達していないとき
        if (playerdata.level < 10) {
          player.sendMessage(ChatColor.GOLD.toString() + "プレイヤーレベルが足りません")
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          return
        }

        //既に妖精召喚している場合終了
        if (playerdata.usingVotingFairy) {
          player.sendMessage(ChatColor.GOLD.toString() + "既に妖精を召喚しています")
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          return
        }

        //投票ptが足りない場合終了
        if (playerdata.activeskilldata.effectpoint < playerdata.toggleVotingFairy * 2) {
          player.sendMessage(ChatColor.GOLD.toString() + "投票ptが足りません")
          player.playSound(player.location, Sound.BLOCK_GLASS_PLACE, 1f, 0.1.toFloat())
          return
        }

        VotingFairyListener.summon(player)
        player.closeInventory()
      } else if (itemstackcurrent.type == Material.COMPASS) {
        VotingFairyTask.speak(player, "僕は" + Util.showHour(playerdata.VotingFairyEndTime!!) + "には帰るよー。", playerdata.toggleVFSound)
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
    val playerdata = playermap[uuid]!!
    val itemmeta = itemstackcurrent.itemMeta

    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "ホームメニュー") {
      event.isCancelled = true

      if (event.clickedInventory.type == InventoryType.PLAYER) {
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

      if (event.clickedInventory.type == InventoryType.PLAYER) {
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
    if (he.type != EntityType.PLAYER) {
      return
    }

    val topinventory = view.topInventory ?: return
    //インベントリが存在しない時終了
    //インベントリが6列でない時終了
    if (topinventory.row != 6) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playermap[uuid]!!

    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "スキルを進化させますか?") {
      event.isCancelled = true
      if (itemstackcurrent.type == Material.NETHER_STAR) {
        playerdata.GBstage = playerdata.GBstage + 1
        playerdata.GBlevel = 0
        playerdata.GBexp = 0
        playerdata.isGBStageUp = false
        player.playSound(player.location, Sound.BLOCK_END_GATEWAY_SPAWN, 1f, 0.5.toFloat())
        player.playSound(player.location, Sound.ENTITY_ENDERDRAGON_AMBIENT, 1f, 0.8.toFloat())
        player.openInventory(MenuInventoryData.getGiganticBerserkEvolution2Menu(player))
      }
    } else if (topinventory.title == ChatColor.LIGHT_PURPLE.toString() + "" + ChatColor.BOLD + "スキルを進化させました") {
      event.isCancelled = true
    }
  }
}

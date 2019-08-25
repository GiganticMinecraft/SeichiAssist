package com.github.unchama.buildassist

import com.github.unchama.buildassist.menu.BuildMainMenu
import com.github.unchama.seichiassist.Schedulers
import com.github.unchama.seichiassist.SeichiAssist
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.meta.SkullMeta

class PlayerInventoryListener : Listener {
  internal var playermap = BuildAssist.playermap

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
    //インベントリサイズが36でない時終了
    if (topinventory.size != 36) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playermap[uuid] ?: return

    //プレイヤーデータが無い場合は処理終了

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "木の棒メニューB") {

    }
    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "「範囲設置スキル」設定画面") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      
      if (itemstackcurrent.type == Material.BARRIER) {
        //ホームメニューへ帰還
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        GlobalScope.launch(Schedulers.async) { BuildMainMenu.open.runFor(player) }

      } else if (itemstackcurrent.type == Material.SKULL_ITEM) {
        if (itemstackcurrent.amount == 11) {
          //範囲MAX
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.AREAint = 5
          player.sendMessage(ChatColor.RED.toString() + "現在の範囲設定は" + (playerdata.AREAint * 2 + 1) + "×" + (playerdata.AREAint * 2 + 1) + "です")
          player.openInventory(MenuInventoryData.getSetBlockSkillData(player))

        } else if (itemstackcurrent.amount == 7) {
          //範囲++
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata.AREAint == 5) {
            player.sendMessage(ChatColor.RED.toString() + "[範囲スキル設定]これ以上範囲を広くできません！")
          } else {
            playerdata.AREAint++
          }
          player.sendMessage(ChatColor.RED.toString() + "現在の範囲設定は" + (playerdata.AREAint * 2 + 1) + "×" + (playerdata.AREAint * 2 + 1) + "です")
          player.openInventory(MenuInventoryData.getSetBlockSkillData(player))

        } else if (itemstackcurrent.amount == 5) {
          //範囲初期化
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.AREAint = 2
          player.sendMessage(ChatColor.RED.toString() + "現在の範囲設定は" + (playerdata.AREAint * 2 + 1) + "×" + (playerdata.AREAint * 2 + 1) + "です")
          player.openInventory(MenuInventoryData.getSetBlockSkillData(player))

        } else if (itemstackcurrent.amount == 3) {
          //範囲--
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata.AREAint == 1) {
            player.sendMessage(ChatColor.RED.toString() + "[範囲スキル設定]これ以上範囲を狭くできません！")
          } else {
            playerdata.AREAint--
          }
          player.sendMessage(ChatColor.RED.toString() + "現在の範囲設定は" + (playerdata.AREAint * 2 + 1) + "×" + (playerdata.AREAint * 2 + 1) + "です")
          player.openInventory(MenuInventoryData.getSetBlockSkillData(player))

        } else if (itemstackcurrent.amount == 1) {
          //範囲MIN
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          playerdata.AREAint = 1
          player.sendMessage(ChatColor.RED.toString() + "現在の範囲設定は" + (playerdata.AREAint * 2 + 1) + "×" + (playerdata.AREAint * 2 + 1) + "です")
          player.openInventory(MenuInventoryData.getSetBlockSkillData(player))
        }
      } else if (itemstackcurrent.type == Material.STONE) {
        //範囲設置スキル ON/OFF
        //範囲設置スキル ON/OFF
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (playerdata.level < BuildAssist.config.zoneSetSkillLevel) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {
          if (playerdata.ZoneSetSkillFlag == false) {
            playerdata.ZoneSetSkillFlag = true
            player.sendMessage(ChatColor.RED.toString() + "範囲設置スキルON")
            player.openInventory(MenuInventoryData.getSetBlockSkillData(player))
          } else if (playerdata.ZoneSetSkillFlag == true) {
            playerdata.ZoneSetSkillFlag = false
            player.sendMessage(ChatColor.RED.toString() + "範囲設置スキルOFF")
            player.openInventory(MenuInventoryData.getSetBlockSkillData(player))
          }
        }


      } else if (itemstackcurrent.type == Material.DIRT) {
        //範囲設置スキル、土設置 ON/OFF
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (playerdata.zsSkillDirtFlag == false) {
          playerdata.zsSkillDirtFlag = true
          player.sendMessage(ChatColor.RED.toString() + "土設置機能ON")
          player.openInventory(MenuInventoryData.getSetBlockSkillData(player))
        } else if (playerdata.zsSkillDirtFlag == true) {
          playerdata.zsSkillDirtFlag = false
          player.sendMessage(ChatColor.RED.toString() + "土設置機能OFF")
          player.openInventory(MenuInventoryData.getSetBlockSkillData(player))
        }
      } else if (itemstackcurrent.type == Material.CHEST) {
        //MineStack優先設定
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        if (playerdata.level < BuildAssist.config.zoneskillMinestacklevel) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {
          if (playerdata.zs_minestack_flag == true) {
            playerdata.zs_minestack_flag = false
            player.sendMessage(ChatColor.RED.toString() + "MineStack優先設定OFF")
            player.openInventory(MenuInventoryData.getSetBlockSkillData(player))
          } else {
            playerdata.zs_minestack_flag = true
            player.sendMessage(ChatColor.RED.toString() + "MineStack優先設定ON")
            player.openInventory(MenuInventoryData.getSetBlockSkillData(player))
          }
        }
      }

    }

  }


  //ブロックを並べるスキル（仮）設定画面
  @EventHandler
  fun onPlayerClickBlockLineUpEvent(event: InventoryClickEvent) {
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
    if (topinventory.size != 36) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playermap[uuid] ?: return

    //プレイヤーデータが無い場合は処理終了

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "「ブロックを並べるスキル（仮）」設定") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.type == Material.SKULL_ITEM) {
        //ホームメニューへ帰還
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        GlobalScope.launch(Schedulers.async) { BuildMainMenu.open.runFor(player) }

      } else if (itemstackcurrent.type == Material.WOOD) {
        //ブロックを並べるスキル設定
        if (playerdata.level < BuildAssist.config.getblocklineuplevel()) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          if (playerdata.line_up_flg >= 2) {
            playerdata.line_up_flg = 0
          } else {
            playerdata.line_up_flg++
          }
          player.sendMessage(ChatColor.GREEN.toString() + "ブロックを並べるスキル（仮） ：" + BuildAssist.line_up_str[playerdata.line_up_flg])
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getBlockLineUpData(player))
        }

      } else if (itemstackcurrent.type == Material.STEP) {
        //ブロックを並べるスキルハーフブロック設定
        if (playerdata.line_up_step_flg >= 2) {
          playerdata.line_up_step_flg = 0
        } else {
          playerdata.line_up_step_flg++
        }
        player.sendMessage(ChatColor.GREEN.toString() + "ハーフブロック設定 ：" + BuildAssist.line_up_step_str[playerdata.line_up_step_flg])
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(MenuInventoryData.getBlockLineUpData(player))

      } else if (itemstackcurrent.type == Material.TNT) {
        //ブロックを並べるスキル一部ブロックを破壊して並べる設定
        playerdata.line_up_des_flg = playerdata.line_up_des_flg xor 1
        player.sendMessage(ChatColor.GREEN.toString() + "破壊設定 ：" + BuildAssist.line_up_off_on_str[playerdata.line_up_des_flg])
        player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(MenuInventoryData.getBlockLineUpData(player))

      } else if (itemstackcurrent.type == Material.CHEST) {
        //マインスタックの方を優先して消費する設定
        if (playerdata.level < BuildAssist.config.getblocklineupMinestacklevel()) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {
          playerdata.line_up_minestack_flg = playerdata.line_up_minestack_flg xor 1
          player.sendMessage(ChatColor.GREEN.toString() + "マインスタック優先設定 ：" + BuildAssist.line_up_off_on_str[playerdata.line_up_minestack_flg])
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getBlockLineUpData(player))
        }
      }
    }
  }


  //MineStackブロック一括クラフト画面1
  @EventHandler
  fun onPlayerClickBlockCraft(event: InventoryClickEvent) {
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
    //インベントリサイズが54でない時終了
    if (topinventory.size != 54) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playermap[uuid] ?: return

    //プレイヤーデータが無い場合は処理終了

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "MineStackブロック一括クラフト1") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.type == Material.SKULL_ITEM && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowLeft") {
        //ホームメニューへ帰還
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        GlobalScope.launch(Schedulers.async) { BuildMainMenu.open.runFor(player) }

      } else if (itemstackcurrent.type == Material.SKULL_ITEM && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowDown") {
        //2ページ目へ
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getBlockCraftData2(player))

        //石を石ハーフブロックに変換10～10万
      } else if (itemstackcurrent.type == Material.STEP) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(1)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("stone")
          val id_2 = Util.findMineStackObjectByName("step0")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, (Math.pow(10.0, x.toDouble()).toInt() * 2).toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "石" + Math.pow(10.0, x.toDouble()).toInt() + "個→石ハーフブロック" + Math.pow(10.0, x.toDouble()).toInt() * 2 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //石を石レンガに変換10～10万
      } else if (itemstackcurrent.type == Material.SMOOTH_BRICK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(1)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("stone")
          val id_2 = Util.findMineStackObjectByName("smooth_brick0")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "石" + Math.pow(10.0, x.toDouble()).toInt() + "個→石レンガ" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //花崗岩を磨かれた花崗岩に変換10～1万
      } else if (itemstackcurrent.type == Material.STONE && itemstackcurrent.durability.toInt() == 2) {
        //				player.sendMessage(ChatColor.RED + "data:"+itemstackcurrent.getDurability() );

        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("granite")
          val id_2 = Util.findMineStackObjectByName("polished_granite")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "花崗岩" + Math.pow(10.0, x.toDouble()).toInt() + "個→磨かれた花崗岩" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //閃緑岩を磨かれた閃緑岩に変換10～1万
      } else if (itemstackcurrent.type == Material.STONE && itemstackcurrent.durability.toInt() == 4) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("diorite")
          val id_2 = Util.findMineStackObjectByName("polished_diorite")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "閃緑岩" + Math.pow(10.0, x.toDouble()).toInt() + "個→磨かれた閃緑岩" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //安山岩を磨かれた安山岩に変換10～1万
      } else if (itemstackcurrent.type == Material.STONE && itemstackcurrent.durability.toInt() == 6) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("andesite")
          val id_2 = Util.findMineStackObjectByName("polished_andesite")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "安山岩" + Math.pow(10.0, x.toDouble()).toInt() + "個→磨かれた安山岩" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //ネザー水晶をネザー水晶ブロックに変換10～1万
      } else if (itemstackcurrent.type == Material.QUARTZ_BLOCK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("quartz")
          val id_2 = Util.findMineStackObjectByName("quartz_block")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 4) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "ネザー水晶" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個→ネザー水晶ブロック" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //レンガをレンガブロックに変換10～1万
      } else if (itemstackcurrent.type == Material.BRICK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("brick_item")
          val id_2 = Util.findMineStackObjectByName("brick")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 4) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "レンガ" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個→レンガブロック" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }
        //ネザーレンガをネザーレンガブロックに変換10～1万
      } else if (itemstackcurrent.type == Material.NETHER_BRICK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("nether_brick_item")
          val id_2 = Util.findMineStackObjectByName("nether_brick")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 4) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "ネザーレンガ" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個→ネザーレンガブロック" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

      }

    }

  }


  //MineStackブロック一括クラフト画面2
  @EventHandler
  fun onPlayerClickBlockCraft2(event: InventoryClickEvent) {
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
    //インベントリサイズが54でない時終了
    if (topinventory.size != 54) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playermap[uuid] ?: return

    //プレイヤーデータが無い場合は処理終了

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "MineStackブロック一括クラフト2") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.type == Material.SKULL_ITEM && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowUp") {
        //1ページ目へ
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getBlockCraftData(player))

      } else if (itemstackcurrent.type == Material.SKULL_ITEM && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowDown") {
        //3ページ目へ
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getBlockCraftData3(player))

        //雪玉を雪（ブロック）に変換10～1万
      } else if (itemstackcurrent.type == Material.SNOW_BLOCK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("snow_ball")
          val id_2 = Util.findMineStackObjectByName("snow_block")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 4) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "雪玉" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個→雪（ブロック）" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //ネザーウォートとネザーレンガを赤いネザーレンガに変換10～10万
      } else if (itemstackcurrent.type == Material.RED_NETHER_BRICK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("nether_stalk")
          val id_2 = Util.findMineStackObjectByName("red_nether_brick")
          val id_3 = Util.findMineStackObjectByName("nether_brick_item")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 2 || playerdata_s.minestack.getStackedAmountOf(id_3!!) < Math.pow(10.0, x.toDouble()).toInt() * 2) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 2).toLong())
            playerdata_s.minestack.subtractStackedAmountOf(id_3, (Math.pow(10.0, x.toDouble()).toInt() * 2).toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "ネザーウォート" + Math.pow(10.0, x.toDouble()).toInt() * 2 + "個+ネザーレンガ" + Math.pow(10.0, x.toDouble()).toInt() * 2 + "個→赤いネザーレンガ" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //石炭を消費して鉄鉱石を鉄インゴットに変換4～4000
      } else if (itemstackcurrent.type == Material.IRON_INGOT && itemstackcurrent.itemMeta.displayName.contains("石炭")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("iron_ore")
          val id_2 = Util.findMineStackObjectByName("iron_ingot")
          val id_3 = Util.findMineStackObjectByName("coal")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 4 || playerdata_s.minestack.getStackedAmountOf(id_3!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "鉄鉱石" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個+石炭" + Math.pow(10.0, x.toDouble()).toInt() + "個→鉄インゴット" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //溶岩バケツを消費して鉄鉱石を鉄インゴットに変換50～5万
      } else if (itemstackcurrent.type == Material.IRON_INGOT && itemstackcurrent.itemMeta.displayName.contains("溶岩")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("iron_ore")
          val id_2 = Util.findMineStackObjectByName("iron_ingot")
          val id_3 = Util.findMineStackObjectByName("lava_bucket")
          val id_4 = Util.findMineStackObjectByName("bucket")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 50 || playerdata_s.minestack.getStackedAmountOf(id_3!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 50).toLong())
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, (Math.pow(10.0, x.toDouble()).toInt() * 50).toLong())
            playerdata_s.minestack.addStackedAmountOf(id_4!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "鉄鉱石" + Math.pow(10.0, x.toDouble()).toInt() * 50 + "個+溶岩バケツ" + Math.pow(10.0, x.toDouble()).toInt() + "個→鉄インゴット" + Math.pow(10.0, x.toDouble()).toInt() * 50 + "個+バケツ" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }


        //石炭を消費して金鉱石を金インゴットに変換4～4000
      } else if (itemstackcurrent.type == Material.GOLD_INGOT && itemstackcurrent.itemMeta.displayName.contains("石炭")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("gold_ore")
          val id_2 = Util.findMineStackObjectByName("gold_ingot")
          val id_3 = Util.findMineStackObjectByName("coal")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 4 || playerdata_s.minestack.getStackedAmountOf(id_3!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "金鉱石" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個+石炭" + Math.pow(10.0, x.toDouble()).toInt() + "個→金インゴット" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //溶岩バケツを消費して金鉱石を金インゴットに変換50～5万
      } else if (itemstackcurrent.type == Material.GOLD_INGOT && itemstackcurrent.itemMeta.displayName.contains("溶岩")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("gold_ore")
          val id_2 = Util.findMineStackObjectByName("gold_ingot")
          val id_3 = Util.findMineStackObjectByName("lava_bucket")
          val id_4 = Util.findMineStackObjectByName("bucket")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 50 || playerdata_s.minestack.getStackedAmountOf(id_3!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 50).toLong())
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, (Math.pow(10.0, x.toDouble()).toInt() * 50).toLong())
            playerdata_s.minestack.addStackedAmountOf(id_4!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "金鉱石" + Math.pow(10.0, x.toDouble()).toInt() * 50 + "個+溶岩バケツ" + Math.pow(10.0, x.toDouble()).toInt() + "個→金インゴット" + Math.pow(10.0, x.toDouble()).toInt() * 50 + "個+バケツ" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }


        //石炭を消費して砂をガラスに変換4～4000
      } else if (itemstackcurrent.type == Material.GLASS && itemstackcurrent.itemMeta.displayName.contains("石炭")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("sand")
          val id_2 = Util.findMineStackObjectByName("glass")
          val id_3 = Util.findMineStackObjectByName("coal")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 4 || playerdata_s.minestack.getStackedAmountOf(id_3!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "砂" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個+石炭" + Math.pow(10.0, x.toDouble()).toInt() + "個→ガラス" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //溶岩バケツを消費して砂をガラスに変換50～5万
      } else if (itemstackcurrent.type == Material.GLASS && itemstackcurrent.itemMeta.displayName.contains("溶岩")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("sand")
          val id_2 = Util.findMineStackObjectByName("glass")
          val id_3 = Util.findMineStackObjectByName("lava_bucket")
          val id_4 = Util.findMineStackObjectByName("bucket")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 50 || playerdata_s.minestack.getStackedAmountOf(id_3!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 50).toLong())
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, (Math.pow(10.0, x.toDouble()).toInt() * 50).toLong())
            playerdata_s.minestack.addStackedAmountOf(id_4!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "砂" + Math.pow(10.0, x.toDouble()).toInt() * 50 + "個+溶岩バケツ" + Math.pow(10.0, x.toDouble()).toInt() + "個→ガラス" + Math.pow(10.0, x.toDouble()).toInt() * 50 + "個+バケツ" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }


        //石炭を消費してネザーラックをネザーレンガに変換4～4000
      } else if (itemstackcurrent.type == Material.NETHER_BRICK_ITEM && itemstackcurrent.itemMeta.displayName.contains("石炭")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("netherrack")
          val id_2 = Util.findMineStackObjectByName("nether_brick_item")
          val id_3 = Util.findMineStackObjectByName("coal")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 4 || playerdata_s.minestack.getStackedAmountOf(id_3!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "ネザーラック" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個+石炭" + Math.pow(10.0, x.toDouble()).toInt() + "個→ネザーレンガ" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //溶岩バケツを消費してネザーラックをネザーレンガに変換50～5万
      } else if (itemstackcurrent.type == Material.NETHER_BRICK_ITEM && itemstackcurrent.itemMeta.displayName.contains("溶岩")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("netherrack")
          val id_2 = Util.findMineStackObjectByName("nether_brick_item")
          val id_3 = Util.findMineStackObjectByName("lava_bucket")
          val id_4 = Util.findMineStackObjectByName("bucket")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 50 || playerdata_s.minestack.getStackedAmountOf(id_3!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 50).toLong())
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, (Math.pow(10.0, x.toDouble()).toInt() * 50).toLong())
            playerdata_s.minestack.addStackedAmountOf(id_4!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "ネザーラック" + Math.pow(10.0, x.toDouble()).toInt() * 50 + "個+溶岩バケツ" + Math.pow(10.0, x.toDouble()).toInt() + "個→ネザーレンガ" + Math.pow(10.0, x.toDouble()).toInt() * 50 + "個+バケツ" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }
      }
    }
  }

  //MineStackブロック一括クラフト画面3
  @EventHandler
  fun onPlayerClickBlockCraft3(event: InventoryClickEvent) {
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
    //インベントリサイズが54でない時終了
    if (topinventory.size != 54) {
      return
    }
    val player = he as Player
    val uuid = player.uniqueId
    val playerdata = playermap[uuid] ?: return

    //プレイヤーデータが無い場合は処理終了

    //インベントリ名が以下の時処理
    if (topinventory.title == ChatColor.DARK_PURPLE.toString() + "" + ChatColor.BOLD + "MineStackブロック一括クラフト3") {
      event.isCancelled = true

      //プレイヤーインベントリのクリックの場合終了
      if (event.clickedInventory.type == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.type == Material.SKULL_ITEM && (itemstackcurrent.itemMeta as SkullMeta).owner == "MHF_ArrowUp") {
        //2ページ目へ
        player.playSound(player.location, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1.toFloat())
        player.openInventory(MenuInventoryData.getBlockCraftData2(player))

        /*			} else if (itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.getItemMeta()).getOwner().equals("MHF_ArrowDown") ){
				//4ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getBlockCraftData4(player));
*/

        //石炭を消費して粘土をレンガに変換4～4000
      } else if (itemstackcurrent.type == Material.CLAY_BRICK && itemstackcurrent.itemMeta.displayName.contains("石炭")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("clay_ball")
          val id_2 = Util.findMineStackObjectByName("brick_item")
          val id_3 = Util.findMineStackObjectByName("coal")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 4 || playerdata_s.minestack.getStackedAmountOf(id_3!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, (Math.pow(10.0, x.toDouble()).toInt() * 4).toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "粘土" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個+石炭" + Math.pow(10.0, x.toDouble()).toInt() + "個→レンガ" + Math.pow(10.0, x.toDouble()).toInt() * 4 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData3(player))
        }

        //溶岩バケツを消費して粘土をレンガに変換50～5万
      } else if (itemstackcurrent.type == Material.CLAY_BRICK && itemstackcurrent.itemMeta.displayName.contains("溶岩")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(ChatColor.RED.toString() + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap[uuid] ?: return
          val x = itemstackcurrent.amount
          val id_1 = Util.findMineStackObjectByName("clay_ball")
          val id_2 = Util.findMineStackObjectByName("brick_item")
          val id_3 = Util.findMineStackObjectByName("lava_bucket")
          val id_4 = Util.findMineStackObjectByName("bucket")
          player.playSound(player.location, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1!!) < Math.pow(10.0, x.toDouble()).toInt() * 50 || playerdata_s.minestack.getStackedAmountOf(id_3!!) < Math.pow(10.0, x.toDouble()).toInt()) {
            player.sendMessage(ChatColor.RED.toString() + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble()).toInt() * 50).toLong())
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble()).toInt().toLong())
            playerdata_s.minestack.addStackedAmountOf(id_2!!, (Math.pow(10.0, x.toDouble()).toInt() * 50).toLong())
            playerdata_s.minestack.addStackedAmountOf(id_4!!, Math.pow(10.0, x.toDouble()).toInt().toLong())
            player.sendMessage(ChatColor.GREEN.toString() + "粘土" + Math.pow(10.0, x.toDouble()).toInt() * 50 + "個+溶岩バケツ" + Math.pow(10.0, x.toDouble()).toInt() + "個→レンガ" + Math.pow(10.0, x.toDouble()).toInt() * 50 + "個+バケツ" + Math.pow(10.0, x.toDouble()).toInt() + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData3(player))
        }
      }
    }
  }


}

package com.github.unchama.buildassist

import java.util.UUID

import com.github.unchama.buildassist.menu.BuildMainMenu
import com.github.unchama.seichiassist
import com.github.unchama.seichiassist.{CommonSoundEffects, SeichiAssist}
import net.md_5.bungee.api.ChatColor._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.{Material, Sound}

import scala.collection.mutable

class PlayerInventoryListener extends Listener {
  val playerMap: mutable.HashMap[UUID, PlayerData] = BuildAssist.playermap

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.sync
  import com.github.unchama.targetedeffect._
  import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver

  //ブロックを並べるスキル（仮）設定画面
  @EventHandler
  def onPlayerClickBlockLineUpEvent(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) return

    val topinventory = view.getTopInventory.ifNull {
      return
    }

    //インベントリが存在しない時終了
    //インベントリサイズが36でない時終了
    if (topinventory.getSize != 36) {
      return
    }

    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap.getOrElse(uuid, return)

    //プレイヤーデータが無い場合は処理終了

    //インベントリ名が以下の時処理
    if (topinventory.getTitle == s"${DARK_PURPLE.toString}$BOLD「ブロックを並べるスキル（仮）」設定") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.getType == Material.SKULL_ITEM) {
        //ホームメニューへ帰還
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, sync}

        seichiassist.unsafe.runAsyncTargetedEffect(player)(
          sequentialEffect(
            CommonSoundEffects.menuTransitionFenceSound,
            BuildMainMenu.open
          ),
          "BuildMainMenuを開く"
        )
      } else if (itemstackcurrent.getType == Material.WOOD) {
        //ブロックを並べるスキル設定
        if (playerdata.level < BuildAssist.config.getblocklineuplevel()) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {
          playerdata.line_up_flg = (playerdata.line_up_flg + 1) % 3

          player.sendMessage(s"${GREEN.toString}ブロックを並べるスキル（仮） ：${BuildAssist.line_up_str.apply(playerdata.line_up_flg)}")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getBlockLineUpData(player))
        }
      } else if (itemstackcurrent.getType == Material.STEP) {
        //ブロックを並べるスキルハーフブロック設定
        if (playerdata.line_up_step_flg >= 2) {
          playerdata.line_up_step_flg = 0
        } else {
          playerdata.line_up_step_flg += 1
        }
        player.sendMessage(s"${GREEN.toString}ハーフブロック設定 ：${BuildAssist.line_up_step_str(playerdata.line_up_step_flg)}")
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(MenuInventoryData.getBlockLineUpData(player))

      } else if (itemstackcurrent.getType == Material.TNT) {
        //ブロックを並べるスキル一部ブロックを破壊して並べる設定
        playerdata.line_up_des_flg = if (playerdata.line_up_des_flg == 0) 1 else 0
        player.sendMessage(s"${GREEN.toString}破壊設定 ：${BuildAssist.line_up_off_on_str(playerdata.line_up_des_flg)}")
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(MenuInventoryData.getBlockLineUpData(player))

      } else if (itemstackcurrent.getType == Material.CHEST) {
        //マインスタックの方を優先して消費する設定
        if (playerdata.level < BuildAssist.config.getblocklineupMinestacklevel()) {
          player.sendMessage(s"${RED.toString}建築LVが足りません")
        } else {
          playerdata.line_up_minestack_flg = if (playerdata.line_up_minestack_flg == 0) 1 else 0
          player.sendMessage(GREEN.toString + "マインスタック優先設定 ：" + BuildAssist.line_up_off_on_str(playerdata.line_up_minestack_flg))
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getBlockLineUpData(player))
        }
      }
    }
  }


  //MineStackブロック一括クラフト画面1
  @EventHandler
  def onPlayerClickBlockCraft(event: InventoryClickEvent): Unit = {
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
    if (topinventory.getSize != 54) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap.getOrElse(uuid, return)

    //プレイヤーデータが無い場合は処理終了

    //インベントリ名が以下の時処理
    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "MineStackブロック一括クラフト1") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.getType == Material.SKULL_ITEM && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowLeft") {
        //ホームメニューへ帰還
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.layoutPreparationContext

        seichiassist.unsafe.runAsyncTargetedEffect(player)(
          sequentialEffect(
            CommonSoundEffects.menuTransitionFenceSound,
            BuildMainMenu.open
          ),
          "BuildMainMenuを開く"
        )
      } else if (itemstackcurrent.getType == Material.SKULL_ITEM && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowDown") {
        //2ページ目へ
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f)
        player.openInventory(MenuInventoryData.getBlockCraftData2(player))

        //石を石ハーフブロックに変換10～10万
      } else if (itemstackcurrent.getType == Material.STEP) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(1)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("stone")
          val id_2 = Util.findMineStackObjectByName("step0")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * 2).toLong)
            player.sendMessage(GREEN.toString + "石" + Math.pow(10.0, x.toDouble).toInt + "個→石ハーフブロック" + Math.pow(10.0, x.toDouble).toInt * 2 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //石を石レンガに変換10～10万
      } else if (itemstackcurrent.getType == Material.SMOOTH_BRICK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(1)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("stone")
          val id_2 = Util.findMineStackObjectByName("smooth_brick0")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "石" + Math.pow(10.0, x.toDouble).toInt + "個→石レンガ" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //花崗岩を磨かれた花崗岩に変換10～1万
      } else if (itemstackcurrent.getType == Material.STONE && itemstackcurrent.getDurability.toInt == 2) {
        //				player.sendMessage(RED + "data:"+itemstackcurrent.getDurability() );

        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("granite")
          val id_2 = Util.findMineStackObjectByName("polished_granite")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "花崗岩" + Math.pow(10.0, x.toDouble).toInt + "個→磨かれた花崗岩" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //閃緑岩を磨かれた閃緑岩に変換10～1万
      } else if (itemstackcurrent.getType == Material.STONE && itemstackcurrent.getDurability.toInt == 4) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("diorite")
          val id_2 = Util.findMineStackObjectByName("polished_diorite")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "閃緑岩" + Math.pow(10.0, x.toDouble).toInt + "個→磨かれた閃緑岩" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //安山岩を磨かれた安山岩に変換10～1万
      } else if (itemstackcurrent.getType == Material.STONE && itemstackcurrent.getDurability.toInt == 6) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("andesite")
          val id_2 = Util.findMineStackObjectByName("polished_andesite")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "安山岩" + Math.pow(10.0, x.toDouble).toInt + "個→磨かれた安山岩" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //ネザー水晶をネザー水晶ブロックに変換10～1万
      } else if (itemstackcurrent.getType == Material.QUARTZ_BLOCK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("quartz")
          val id_2 = Util.findMineStackObjectByName("quartz_block")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 4) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "ネザー水晶" + Math.pow(10.0, x.toDouble).toInt * 4 + "個→ネザー水晶ブロック" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

        //レンガをレンガブロックに変換10～1万
      } else if (itemstackcurrent.getType == Material.BRICK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("brick_item")
          val id_2 = Util.findMineStackObjectByName("brick")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 4) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "レンガ" + Math.pow(10.0, x.toDouble).toInt * 4 + "個→レンガブロック" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }
        //ネザーレンガをネザーレンガブロックに変換10～1万
      } else if (itemstackcurrent.getType == Material.NETHER_BRICK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("nether_brick_item")
          val id_2 = Util.findMineStackObjectByName("nether_brick")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 4) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "ネザーレンガ" + Math.pow(10.0, x.toDouble).toInt * 4 + "個→ネザーレンガブロック" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData(player))
        }

      }

    }

  }


  //MineStackブロック一括クラフト画面2
  @EventHandler
  def onPlayerClickBlockCraft2(event: InventoryClickEvent): Unit = {
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
    if (topinventory.getSize != 54) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap.getOrElse(uuid, return)

    //プレイヤーデータが無い場合は処理終了

    //インベントリ名が以下の時処理
    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "MineStackブロック一括クラフト2") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.getType == Material.SKULL_ITEM && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowUp") {
        //1ページ目へ
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f)
        player.openInventory(MenuInventoryData.getBlockCraftData(player))

      } else if (itemstackcurrent.getType == Material.SKULL_ITEM && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowDown") {
        //3ページ目へ
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f)
        player.openInventory(MenuInventoryData.getBlockCraftData3(player))

        //雪玉を雪（ブロック）に変換10～1万
      } else if (itemstackcurrent.getType == Material.SNOW_BLOCK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("snow_ball")
          val id_2 = Util.findMineStackObjectByName("snow_block")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 4) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "雪玉" + Math.pow(10.0, x.toDouble).toInt * 4 + "個→雪（ブロック）" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //ネザーウォートとネザーレンガを赤いネザーレンガに変換10～10万
      } else if (itemstackcurrent.getType == Material.RED_NETHER_BRICK) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(2)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("nether_stalk")
          val id_2 = Util.findMineStackObjectByName("red_nether_brick")
          val id_3 = Util.findMineStackObjectByName("nether_brick_item")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 2 || playerdata_s.minestack.getStackedAmountOf(id_3) < Math.pow(10.0, x.toDouble).toInt * 2) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 2).toLong)
            playerdata_s.minestack.subtractStackedAmountOf(id_3, (Math.pow(10.0, x.toDouble).toInt * 2).toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "ネザーウォート" + Math.pow(10.0, x.toDouble).toInt * 2 + "個+ネザーレンガ" + Math.pow(10.0, x.toDouble).toInt * 2 + "個→赤いネザーレンガ" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //石炭を消費して鉄鉱石を鉄インゴットに変換4～4000
      } else if (itemstackcurrent.getType == Material.IRON_INGOT && itemstackcurrent.getItemMeta.getDisplayName.contains("石炭")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("iron_ore")
          val id_2 = Util.findMineStackObjectByName("iron_ingot")
          val id_3 = Util.findMineStackObjectByName("coal")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 4 || playerdata_s.minestack.getStackedAmountOf(id_3) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            player.sendMessage(GREEN.toString + "鉄鉱石" + Math.pow(10.0, x.toDouble).toInt * 4 + "個+石炭" + Math.pow(10.0, x.toDouble).toInt + "個→鉄インゴット" + Math.pow(10.0, x.toDouble).toInt * 4 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //溶岩バケツを消費して鉄鉱石を鉄インゴットに変換50～5万
      } else if (itemstackcurrent.getType == Material.IRON_INGOT && itemstackcurrent.getItemMeta.getDisplayName.contains("溶岩")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("iron_ore")
          val id_2 = Util.findMineStackObjectByName("iron_ingot")
          val id_3 = Util.findMineStackObjectByName("lava_bucket")
          val id_4 = Util.findMineStackObjectByName("bucket")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 50 || playerdata_s.minestack.getStackedAmountOf(id_3) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 50).toLong)
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * 50).toLong)
            playerdata_s.minestack.addStackedAmountOf(id_4, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "鉄鉱石" + Math.pow(10.0, x.toDouble).toInt * 50 + "個+溶岩バケツ" + Math.pow(10.0, x.toDouble).toInt + "個→鉄インゴット" + Math.pow(10.0, x.toDouble).toInt * 50 + "個+バケツ" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }


        //石炭を消費して金鉱石を金インゴットに変換4～4000
      } else if (itemstackcurrent.getType == Material.GOLD_INGOT && itemstackcurrent.getItemMeta.getDisplayName.contains("石炭")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("gold_ore")
          val id_2 = Util.findMineStackObjectByName("gold_ingot")
          val id_3 = Util.findMineStackObjectByName("coal")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 4 || playerdata_s.minestack.getStackedAmountOf(id_3) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            player.sendMessage(GREEN.toString + "金鉱石" + Math.pow(10.0, x.toDouble).toInt * 4 + "個+石炭" + Math.pow(10.0, x.toDouble).toInt + "個→金インゴット" + Math.pow(10.0, x.toDouble).toInt * 4 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //溶岩バケツを消費して金鉱石を金インゴットに変換50～5万
      } else if (itemstackcurrent.getType == Material.GOLD_INGOT && itemstackcurrent.getItemMeta.getDisplayName.contains("溶岩")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("gold_ore")
          val id_2 = Util.findMineStackObjectByName("gold_ingot")
          val id_3 = Util.findMineStackObjectByName("lava_bucket")
          val id_4 = Util.findMineStackObjectByName("bucket")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 50 || playerdata_s.minestack.getStackedAmountOf(id_3) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 50).toLong)
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * 50).toLong)
            playerdata_s.minestack.addStackedAmountOf(id_4, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "金鉱石" + Math.pow(10.0, x.toDouble).toInt * 50 + "個+溶岩バケツ" + Math.pow(10.0, x.toDouble).toInt + "個→金インゴット" + Math.pow(10.0, x.toDouble).toInt * 50 + "個+バケツ" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }


        //石炭を消費して砂をガラスに変換4～4000
      } else if (itemstackcurrent.getType == Material.GLASS && itemstackcurrent.getItemMeta.getDisplayName.contains("石炭")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("sand")
          val id_2 = Util.findMineStackObjectByName("glass")
          val id_3 = Util.findMineStackObjectByName("coal")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 4 || playerdata_s.minestack.getStackedAmountOf(id_3) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            player.sendMessage(GREEN.toString + "砂" + Math.pow(10.0, x.toDouble).toInt * 4 + "個+石炭" + Math.pow(10.0, x.toDouble).toInt + "個→ガラス" + Math.pow(10.0, x.toDouble).toInt * 4 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //溶岩バケツを消費して砂をガラスに変換50～5万
      } else if (itemstackcurrent.getType == Material.GLASS && itemstackcurrent.getItemMeta.getDisplayName.contains("溶岩")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("sand")
          val id_2 = Util.findMineStackObjectByName("glass")
          val id_3 = Util.findMineStackObjectByName("lava_bucket")
          val id_4 = Util.findMineStackObjectByName("bucket")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 50 || playerdata_s.minestack.getStackedAmountOf(id_3) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 50).toLong)
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * 50).toLong)
            playerdata_s.minestack.addStackedAmountOf(id_4, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "砂" + Math.pow(10.0, x.toDouble).toInt * 50 + "個+溶岩バケツ" + Math.pow(10.0, x.toDouble).toInt + "個→ガラス" + Math.pow(10.0, x.toDouble).toInt * 50 + "個+バケツ" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }


        //石炭を消費してネザーラックをネザーレンガに変換4～4000
      } else if (itemstackcurrent.getType == Material.NETHER_BRICK_ITEM && itemstackcurrent.getItemMeta.getDisplayName.contains("石炭")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("netherrack")
          val id_2 = Util.findMineStackObjectByName("nether_brick_item")
          val id_3 = Util.findMineStackObjectByName("coal")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 4 || playerdata_s.minestack.getStackedAmountOf(id_3) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            player.sendMessage(GREEN.toString + "ネザーラック" + Math.pow(10.0, x.toDouble).toInt * 4 + "個+石炭" + Math.pow(10.0, x.toDouble).toInt + "個→ネザーレンガ" + Math.pow(10.0, x.toDouble).toInt * 4 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }

        //溶岩バケツを消費してネザーラックをネザーレンガに変換50～5万
      } else if (itemstackcurrent.getType == Material.NETHER_BRICK_ITEM && itemstackcurrent.getItemMeta.getDisplayName.contains("溶岩")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("netherrack")
          val id_2 = Util.findMineStackObjectByName("nether_brick_item")
          val id_3 = Util.findMineStackObjectByName("lava_bucket")
          val id_4 = Util.findMineStackObjectByName("bucket")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 50 || playerdata_s.minestack.getStackedAmountOf(id_3) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 50).toLong)
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * 50).toLong)
            playerdata_s.minestack.addStackedAmountOf(id_4, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "ネザーラック" + Math.pow(10.0, x.toDouble).toInt * 50 + "個+溶岩バケツ" + Math.pow(10.0, x.toDouble).toInt + "個→ネザーレンガ" + Math.pow(10.0, x.toDouble).toInt * 50 + "個+バケツ" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData2(player))
        }
      }
    }
  }

  //MineStackブロック一括クラフト画面3
  @EventHandler
  def onPlayerClickBlockCraft3(event: InventoryClickEvent): Unit = {
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
    if (topinventory.getSize != 54) {
      return
    }
    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId
    val playerdata = playerMap.getOrElse(uuid, return)

    //プレイヤーデータが無い場合は処理終了

    //インベントリ名が以下の時処理
    if (topinventory.getTitle == DARK_PURPLE.toString + "" + BOLD + "MineStackブロック一括クラフト3") {
      event.setCancelled(true)

      //プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }
      /*
			 * クリックしたボタンに応じた各処理内容の記述ここから
			 */
      if (itemstackcurrent.getType == Material.SKULL_ITEM && itemstackcurrent.getItemMeta.asInstanceOf[SkullMeta].getOwner == "MHF_ArrowUp") {
        //2ページ目へ
        player.playSound(player.getLocation, Sound.BLOCK_FENCE_GATE_OPEN, 1f, 0.1f)
        player.openInventory(MenuInventoryData.getBlockCraftData2(player))

        /*			} else if (itemstackcurrent.getType().equals(Material.SKULL_ITEM) && ((SkullMeta)itemstackcurrent.ge.getItemMeta()).ge.getOwner().equals("MHF_ArrowDown") ){
				//4ページ目へ
				player.playSound(player.getLocation(), Sound.BLOCK_FENCE_GATE_OPEN, 1, (float) 0.1);
				player.openInventory(MenuInventoryData.getBlockCraftData4(player));
*/

        //石炭を消費して粘土をレンガに変換4～4000
      } else if (itemstackcurrent.getType == Material.CLAY_BRICK && itemstackcurrent.getItemMeta.getDisplayName.contains("石炭")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("clay_ball")
          val id_2 = Util.findMineStackObjectByName("brick_item")
          val id_3 = Util.findMineStackObjectByName("coal")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 4 || playerdata_s.minestack.getStackedAmountOf(id_3) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * 4).toLong)
            player.sendMessage(GREEN.toString + "粘土" + Math.pow(10.0, x.toDouble).toInt * 4 + "個+石炭" + Math.pow(10.0, x.toDouble).toInt + "個→レンガ" + Math.pow(10.0, x.toDouble).toInt * 4 + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData3(player))
        }

        //溶岩バケツを消費して粘土をレンガに変換50～5万
      } else if (itemstackcurrent.getType == Material.CLAY_BRICK && itemstackcurrent.getItemMeta.getDisplayName.contains("溶岩")) {
        if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(3)) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {

          val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
          val x = itemstackcurrent.getAmount
          val id_1 = Util.findMineStackObjectByName("clay_ball")
          val id_2 = Util.findMineStackObjectByName("brick_item")
          val id_3 = Util.findMineStackObjectByName("lava_bucket")
          val id_4 = Util.findMineStackObjectByName("bucket")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * 50 || playerdata_s.minestack.getStackedAmountOf(id_3) < Math.pow(10.0, x.toDouble).toInt) {
            player.sendMessage(RED.toString + "アイテムが足りません")
          } else {
            playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * 50).toLong)
            playerdata_s.minestack.subtractStackedAmountOf(id_3, Math.pow(10.0, x.toDouble).toInt.toLong)
            playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * 50).toLong)
            playerdata_s.minestack.addStackedAmountOf(id_4, Math.pow(10.0, x.toDouble).toInt.toLong)
            player.sendMessage(GREEN.toString + "粘土" + Math.pow(10.0, x.toDouble).toInt * 50 + "個+溶岩バケツ" + Math.pow(10.0, x.toDouble).toInt + "個→レンガ" + Math.pow(10.0, x.toDouble).toInt * 50 + "個+バケツ" + Math.pow(10.0, x.toDouble).toInt + "個変換")
          }
          player.openInventory(MenuInventoryData.getBlockCraftData3(player))
        }
      }
    }
  }


}

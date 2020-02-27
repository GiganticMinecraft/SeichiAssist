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

  import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.syncShift
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
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}

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
        convertItem(event, 1, ("stone", "石", 1), null, null, ("step0", "石ハーフブロック", 2), 1)

        //石を石レンガに変換10～10万
      } else if (itemstackcurrent.getType == Material.SMOOTH_BRICK) {
        convertItem(event, 1, ("stone", "石", 1), null, null, ("smooth_brick0", "石レンガ", 1), 1)

        //花崗岩を磨かれた花崗岩に変換10～1万
      } else if (itemstackcurrent.getType == Material.STONE && itemstackcurrent.getDurability.toInt == 2) {
        convertItem(event, 2, ("granite", "花崗岩", 1), null, null, ("polished_granite", "磨かれた花崗岩", 1), 1)

        //閃緑岩を磨かれた閃緑岩に変換10～1万
      } else if (itemstackcurrent.getType == Material.STONE && itemstackcurrent.getDurability.toInt == 4) {
        convertItem(event, 2, ("diorite", "閃緑岩", 1), null, null, ("polished_diorite", "磨かれた閃緑岩", 1), 1)

        //安山岩を磨かれた安山岩に変換10～1万
      } else if (itemstackcurrent.getType == Material.STONE && itemstackcurrent.getDurability.toInt == 6) {
        convertItem(event, 2, ("andesite", "安山岩", 1), null, null, ("polished_andesite", "磨かれた安山岩", 1), 1)

        //ネザー水晶をネザー水晶ブロックに変換10～1万
      } else if (itemstackcurrent.getType == Material.QUARTZ_BLOCK) {
        convertItem(event, 2, ("quartz", "ネザー水晶", 4), null, null, ("quartz_block", "ネザー水晶ブロック", 1), 1)

        //レンガをレンガブロックに変換10～1万
      } else if (itemstackcurrent.getType == Material.BRICK) {
        convertItem(event, 2, ("brick_item", "レンガ", 4), null, null, ("brick", "レンガブロック", 1), 1)

        //ネザーレンガをネザーレンガブロックに変換10～1万
      } else if (itemstackcurrent.getType == Material.NETHER_BRICK) {
        convertItem(event, 2, ("nether_brick_item", "ネザーレンガ", 4), null, null, ("nether_brick", "ネザーレンガブロック", 1), 1)

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
        convertItem(event, 2, ("snow_ball", "雪玉", 4), null, null, ("snow_block", "雪(ブロック)", 1), 2)


        //ネザーウォートとネザーレンガを赤いネザーレンガに変換10～10万
      } else if (itemstackcurrent.getType == Material.RED_NETHER_BRICK) {
        convertItem(event, 2, ("nether_stalk", "ネザーウォート", 2), ("nether_brick_item", "ネザーレンガ", 2), null, ("red_nether_brick", "赤いネザーレンガ", 1), 2)

        //石炭を消費して鉄鉱石を鉄インゴットに変換4～4000
      } else if (itemstackcurrent.getType == Material.IRON_INGOT && itemstackcurrent.getItemMeta.getDisplayName.contains("石炭")) {
        convertItem(event, 3, ("iron_ore", "鉄鉱石", 4), ("coal", "石炭", 1), null, ("iron_ingot", "鉄インゴット", 4), 2)

        //溶岩バケツを消費して鉄鉱石を鉄インゴットに変換50～5万
      } else if (itemstackcurrent.getType == Material.IRON_INGOT && itemstackcurrent.getItemMeta.getDisplayName.contains("溶岩")) {
        convertItem(event, 3, ("iron_ore", "鉄鉱石", 50), ("lava_bucket", "溶岩バケツ", 1.0), ("bucket", "バケツ", 1.0), ("iron_ingot", "鉄インゴット", 50), 2)

        //石炭を消費して金鉱石を金インゴットに変換4～4000
      } else if (itemstackcurrent.getType == Material.GOLD_INGOT && itemstackcurrent.getItemMeta.getDisplayName.contains("石炭")) {
        convertItem(event, 3, ("gold_ore", "金鉱石", 4), ("coal", "石炭", 1), null, ("gold_ingot", "金インゴット", 4), 2)

        //溶岩バケツを消費して金鉱石を金インゴットに変換50～5万
      } else if (itemstackcurrent.getType == Material.GOLD_INGOT && itemstackcurrent.getItemMeta.getDisplayName.contains("溶岩")) {
        convertItem(event, 3, ("gold_ore", "金鉱石", 50), ("lava_bucket", "溶岩バケツ", 1.0), ("bucket", "バケツ", 1.0), ("gold_ingot", "金インゴット", 50), 2)

        //石炭を消費して砂をガラスに変換4～4000
      } else if (itemstackcurrent.getType == Material.GLASS && itemstackcurrent.getItemMeta.getDisplayName.contains("石炭")) {
        convertItem(event, 3, ("sand", "砂", 4), ("coal", "石炭", 1), null, ("glass", "ガラス", 4), 2)

        //溶岩バケツを消費して砂をガラスに変換50～5万
      } else if (itemstackcurrent.getType == Material.GLASS && itemstackcurrent.getItemMeta.getDisplayName.contains("溶岩")) {
        convertItem(event, 3, ("sand", "砂", 50), ("lava_bucket", "溶岩バケツ", 1.0), ("bucket", "バケツ", 1.0), ("glass", "ガラス", 50), 2)

        //石炭を消費してネザーラックをネザーレンガに変換4～4000
      } else if (itemstackcurrent.getType == Material.NETHER_BRICK_ITEM && itemstackcurrent.getItemMeta.getDisplayName.contains("石炭")) {
        convertItem(event, 3, ("netherrack", "ネザーラック", 4), ("coal", "石炭", 1), null, ("nether_brick_item", "ネザーレンガ", 4), 2)

        //溶岩バケツを消費してネザーラックをネザーレンガに変換50～5万
      } else if (itemstackcurrent.getType == Material.NETHER_BRICK_ITEM && itemstackcurrent.getItemMeta.getDisplayName.contains("溶岩")) {
        convertItem(event, 3, ("netherrack", "ネザーラック", 50), ("lava_bucket", "溶岩バケツ", 1.0), ("bucket", "バケツ", 1.0), ("nether_brick_item", "ネザーレンガ", 50), 2)

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
        convertItem(event, 3, ("clay_ball", "粘土", 4), ("coal", "石炭", 1), null, ("brick_item", "レンガ", 4), 3)

        //溶岩バケツを消費して粘土をレンガに変換50～5万
      } else if (itemstackcurrent.getType == Material.CLAY_BRICK && itemstackcurrent.getItemMeta.getDisplayName.contains("溶岩")) {
        convertItem(event, 3, ("clay_ball", "粘土", 50), ("lava_bucket", "溶岩バケツ", 1.0), ("bucket", "バケツ", 1.0), ("brick_item", "レンガ", 50), 3)

      }
    }
  }

  @EventHandler
  def convertItem(event: InventoryClickEvent, minestackBlockCraftlevel: Int, block_before_id_1: Tuple3[String, String, Int], block_before_id_2: Tuple3[String, String, Double], block_return_id: Tuple3[String, String, Double], block_after_id: Tuple3[String, String, Int], page: Int): Unit = {
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

    if (playerdata.level < BuildAssist.config.getMinestackBlockCraftlevel(minestackBlockCraftlevel)) {
      player.sendMessage(RED.toString + "建築LVが足りません")
    } else {
      val playerdata_s = SeichiAssist.playermap.getOrElse(uuid, return)
      val x = itemstackcurrent.getAmount

      // id_1: 前提アイテム id_2: 変換後アイテム id_3: 前提アイテム(石炭、マグマ) id_4:バケツ
      val id_1 = Util.findMineStackObjectByName(block_before_id_1._1)
      val id_2 = Util.findMineStackObjectByName(block_after_id._1)
      var id_3 = Util.findMineStackObjectByName(block_before_id_1._1) //FIXME: varにMineStackObjであることを伝えるため適当に突っ込んでるけど怪しいので
      var id_4 = Util.findMineStackObjectByName(block_after_id._1)
      if (block_before_id_2 != null) {
        id_3 = Util.findMineStackObjectByName(block_before_id_2._1)
        if (block_return_id != null) {
          id_4 = Util.findMineStackObjectByName(block_return_id._1)
        } else {
          id_4 = Util.findMineStackObjectByName("")
        }
      } else {
        id_3 = Util.findMineStackObjectByName("")
      }
      player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
      if (block_before_id_2 != null && (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * block_before_id_1._3 || playerdata_s.minestack.getStackedAmountOf(id_3) < (Math.pow(10.0, x.toDouble).toInt) * block_before_id_2._3)) {
        player.sendMessage(RED.toString + "アイテムが足りません")
      } else if (playerdata_s.minestack.getStackedAmountOf(id_1) < Math.pow(10.0, x.toDouble).toInt * block_before_id_1._3) {
        player.sendMessage(RED.toString + "アイテムが足りません")
      } else {
        playerdata_s.minestack.subtractStackedAmountOf(id_1, (Math.pow(10.0, x.toDouble).toInt * block_before_id_1._3).toLong)
        if (block_before_id_2 != null) {
          playerdata_s.minestack.subtractStackedAmountOf(id_3, (Math.pow(10.0, x.toDouble).toInt * block_before_id_2._3).toLong)
        }
        if (block_return_id != null) {
          playerdata_s.minestack.addStackedAmountOf(id_4, (Math.pow(10.0, x.toDouble).toInt * block_return_id._3).toLong)
        }
        playerdata_s.minestack.addStackedAmountOf(id_2, (Math.pow(10.0, x.toDouble).toInt * block_after_id._3).toLong)
        if (block_before_id_2 != null && block_return_id != null) {
          player.sendMessage(GREEN.toString + block_before_id_1._2 + Math.pow(10.0, x.toDouble).toInt * block_before_id_1._3 + "個+" + block_before_id_2._2 + Math.pow(10.0, x.toDouble).toInt * block_before_id_2._3 + "個→" + block_after_id._2 + Math.pow(10.0, x.toDouble).toInt * block_after_id._3 + "個+" + block_return_id._2 + Math.pow(10.0, x.toDouble).toInt * block_return_id._3 + "個変換")
        } else if (block_before_id_2 != null) {
          player.sendMessage(GREEN.toString + block_before_id_1._2 + Math.pow(10.0, x.toDouble).toInt * block_before_id_1._3 + "個+" + block_before_id_2._2 + Math.pow(10.0, x.toDouble).toInt * block_before_id_2._3 + "個→" + block_after_id._2 + Math.pow(10.0, x.toDouble).toInt * block_after_id._3 + "個変換")
        } else {
          player.sendMessage(GREEN.toString + block_before_id_1._2 + Math.pow(10.0, x.toDouble).toInt * block_before_id_1._3 + "個→" + block_after_id._2 + Math.pow(10.0, x.toDouble).toInt * block_after_id._3 + "個変換")
        }
      }
      if (page == 1) {
        player.openInventory(MenuInventoryData.getBlockCraftData(player))
      } else if (page == 2) {
        player.openInventory(MenuInventoryData.getBlockCraftData2(player))
      } else if (page == 3) {
        player.openInventory(MenuInventoryData.getBlockCraftData3(player))
      } else {
        player.openInventory(MenuInventoryData.getBlockCraftData3(player))
      }
    }
  }
}
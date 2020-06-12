package com.github.unchama.buildassist.listener

import java.util.UUID

import com.github.unchama.buildassist.BuildAssist
import com.github.unchama.buildassist.data.{MenuInventoryData, PlayerData}
import com.github.unchama.buildassist.menu.BuildMainMenu
import com.github.unchama.seichiassist
import com.github.unchama.seichiassist.CommonSoundEffects
import net.md_5.bungee.api.ChatColor._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{Material, Sound}

import scala.collection.mutable

class PlayerInventoryListener extends Listener {
  val playerMap: mutable.HashMap[UUID, PlayerData] = BuildAssist.playermap

  import com.github.unchama.targetedeffect._
  import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver

  //ブロックを並べるスキル（仮）設定画面
  @EventHandler
  def onPlayerClickBlockLineUpEvent(event: InventoryClickEvent): Unit = {
    //外枠のクリック処理なら終了
    event.getClickedInventory.ifNull(return)

    val current = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    //インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) return

    //インベントリが存在しない時終了
    val inventory = view.getTopInventory.ifNull {
      return
    }

    if (inventory.getSize != 36) {
      return
    }

    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId
    //プレイヤーデータが無い場合は処理終了
    val playerdata = playerMap.getOrElse(uuid, return)

    //インベントリ名が以下の時処理
    if (inventory.getTitle != s"${DARK_PURPLE.toString}$BOLD「ブロックを並べるスキル（仮）」設定") {
      return
    }

    event.setCancelled(true)

    //プレイヤーインベントリのクリックの場合終了
    if (event.getClickedInventory.getType == InventoryType.PLAYER) {
      return
    }
    /*
     * クリックしたボタンに応じた各処理内容の記述ここから
     */
    current.getType match {
      case Material.SKULL_ITEM =>
        //ホームメニューへ帰還
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}

        seichiassist.unsafe.runAsyncTargetedEffect(player)(
          SequentialEffect(
            CommonSoundEffects.menuTransitionFenceSound,
            BuildMainMenu.open
          ),
          "BuildMainMenuを開く"
        )

      case Material.WOOD =>
        //ブロックを並べるスキル設定
        if (playerdata.level < BuildAssist.config.getblocklineuplevel()) {
          player.sendMessage(RED.toString + "建築LVが足りません")
        } else {
          playerdata.lineFillFlag = (playerdata.lineFillFlag + 1) % 3

          player.sendMessage(s"${GREEN.toString}ブロックを並べるスキル（仮） ：${BuildAssist.lineFillFlag.apply(playerdata.lineFillFlag)}")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getBlockLineUpData(player))
        }

      case Material.STEP =>
        //ブロックを並べるスキルハーフブロック設定
        playerdata.lineUpStepFlag = (playerdata.lineUpStepFlag + 1) % 3
        player.sendMessage(s"${GREEN.toString}ハーフブロック設定 ：${BuildAssist.lineUpStepStr(playerdata.lineUpStepFlag)}")
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(MenuInventoryData.getBlockLineUpData(player))

      case Material.TNT =>
        //ブロックを並べるスキル一部ブロックを破壊して並べる設定
        playerdata.breakLightBlockFlag = if (playerdata.breakLightBlockFlag == 0) 1 else 0
        player.sendMessage(s"${GREEN.toString}破壊設定 ：${BuildAssist.onOrOff(playerdata.breakLightBlockFlag)}")
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(MenuInventoryData.getBlockLineUpData(player))

      case Material.CHEST =>
        //マインスタックの方を優先して消費する設定
        if (playerdata.level < BuildAssist.config.getblocklineupMinestacklevel()) {
          player.sendMessage(s"${RED.toString}建築LVが足りません")
        } else {
          playerdata.preferMineStackI = if (playerdata.preferMineStackI == 0) 1 else 0
          player.sendMessage(GREEN.toString + "マインスタック優先設定 ：" + BuildAssist.onOrOff(playerdata.preferMineStackI))
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getBlockLineUpData(player))
        }
      case _ => // NOP
    }
  }
}

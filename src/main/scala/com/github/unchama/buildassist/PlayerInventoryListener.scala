package com.github.unchama.buildassist

import cats.effect.IO
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.menus.BuildMainMenu
import net.md_5.bungee.api.ChatColor._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{Material, Sound}

class PlayerInventoryListener(
  implicit effectEnvironment: EffectEnvironment,
  ioCanOpenBuildMainMenu: IO CanOpen BuildMainMenu.type
) extends Listener {

  import com.github.unchama.targetedeffect._
  import com.github.unchama.util.syntax.Nullability.NullabilityExtensionReceiver

  // 直列設置設定画面
  @EventHandler
  def onPlayerClickBlockLineUpEvent(event: InventoryClickEvent): Unit = {
    // 外枠のクリック処理なら終了
    if (event.getClickedInventory == null) {
      return
    }

    val itemstackcurrent = event.getCurrentItem
    val view = event.getView
    val he = view.getPlayer
    // インベントリを開けたのがプレイヤーではない時終了
    if (he.getType != EntityType.PLAYER) return

    val topinventory = view.getTopInventory.ifNull {
      return
    }

    // インベントリが存在しない時終了
    // インベントリサイズが36でない時終了
    if (topinventory.getSize != 36) {
      return
    }

    val player = he.asInstanceOf[Player]
    val uuid = player.getUniqueId

    val playerdata = BuildAssist.instance.temporaryData(uuid)
    val playerLevel = BuildAssist
      .instance
      .buildAmountDataRepository(player)
      .read
      .unsafeRunSync()
      .levelCorrespondingToExp
      .level

    // プレイヤーデータが無い場合は処理終了

    // インベントリ名が以下の時処理
    if (topinventory.getTitle == s"${DARK_PURPLE.toString}$BOLD「直列設置」設定") {
      event.setCancelled(true)

      // プレイヤーインベントリのクリックの場合終了
      if (event.getClickedInventory.getType == InventoryType.PLAYER) {
        return
      }
      /*
       * クリックしたボタンに応じた各処理内容の記述ここから
       */
      if (itemstackcurrent.getType == Material.SKULL_ITEM) {
        // ホームメニューへ帰還

        effectEnvironment.unsafeRunAsyncTargetedEffect(player)(
          SequentialEffect(
            CommonSoundEffects.menuTransitionFenceSound,
            ioCanOpenBuildMainMenu.open(BuildMainMenu)
          ),
          "BuildMainMenuを開く"
        )
      } else if (itemstackcurrent.getType == Material.WOOD) {
        // 直列設置設定
        if (playerLevel < BuildAssist.config.getblocklineuplevel) {
          player.sendMessage(RED.toString + "建築Lvが足りません")
        } else {
          playerdata.line_up_flg = (playerdata.line_up_flg + 1) % 3

          player.sendMessage(
            s"${GREEN.toString}直列設置 ：${BuildAssist.line_up_str.apply(playerdata.line_up_flg)}"
          )
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getBlockLineUpData(player))
        }
      } else if (itemstackcurrent.getType == Material.STEP) {
        // 直列設置ハーフブロック設定
        if (playerdata.line_up_step_flg >= 2) {
          playerdata.line_up_step_flg = 0
        } else {
          playerdata.line_up_step_flg += 1
        }
        player.sendMessage(
          s"${GREEN.toString}ハーフブロック設定 ：${BuildAssist.line_up_step_str(playerdata.line_up_step_flg)}"
        )
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(MenuInventoryData.getBlockLineUpData(player))

      } else if (itemstackcurrent.getType == Material.TNT) {
        // 直列設置一部ブロックを破壊して並べる設定
        playerdata.line_up_des_flg = if (playerdata.line_up_des_flg == 0) 1 else 0
        player.sendMessage(
          s"${GREEN.toString}破壊設定 ：${BuildAssist.line_up_off_on_str(playerdata.line_up_des_flg)}"
        )
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(MenuInventoryData.getBlockLineUpData(player))

      } else if (itemstackcurrent.getType == Material.CHEST) {
        // マインスタックの方を優先して消費する設定
        if (playerLevel < BuildAssist.config.getblocklineupMinestacklevel) {
          player.sendMessage(s"${RED.toString}建築Lvが足りません")
        } else {
          playerdata.line_up_minestack_flg = if (playerdata.line_up_minestack_flg == 0) 1 else 0
          player.sendMessage(
            GREEN.toString + "マインスタック優先設定 ：" + BuildAssist
              .line_up_off_on_str(playerdata.line_up_minestack_flg)
          )
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getBlockLineUpData(player))
        }
      }
    }
  }
}

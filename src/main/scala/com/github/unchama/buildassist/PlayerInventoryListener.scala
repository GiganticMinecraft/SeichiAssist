package com.github.unchama.buildassist

import cats.effect.{IO, SyncIO}
import com.github.unchama.buildassist.menu.BuildMainMenu
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.effects.player.CommonSoundEffects
import com.github.unchama.seichiassist.meta.subsystem.StatefulSubsystem
import com.github.unchama.seichiassist.subsystems
import net.md_5.bungee.api.ChatColor._
import org.bukkit.entity.{EntityType, Player}
import org.bukkit.event.inventory.{InventoryClickEvent, InventoryType}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.{Material, Sound}

import java.util.UUID
import scala.collection.mutable

class PlayerInventoryListener(implicit effectEnvironment: EffectEnvironment,
                              flySystem: StatefulSubsystem[IO, subsystems.managedfly.InternalState[SyncIO]]) extends Listener {
  val playerMap: mutable.HashMap[UUID, PlayerData] = BuildAssist.playermap

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
    if (topinventory.getTitle == s"$DARK_PURPLE$BOLD「ブロックを並べるスキル（仮）」設定") {
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

        effectEnvironment.runAsyncTargetedEffect(player)(
          SequentialEffect(
            CommonSoundEffects.menuTransitionFenceSound,
            new BuildMainMenu().open
          ),
          "BuildMainMenuを開く"
        )
      } else if (itemstackcurrent.getType == Material.WOOD) {
        //ブロックを並べるスキル設定
        if (playerdata.level < BuildAssist.config.getblocklineuplevel()) {
          player.sendMessage(s"${RED}建築Lvが足りません")
        } else {
          playerdata.lineFillAlign = playerdata.lineFillAlign.next

          player.sendMessage(s"${GREEN}ブロックを並べるスキル（仮） ：${playerdata.lineFillAlign.asHumanReadable}")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getBlockLineUpData(player))
        }
      } else if (itemstackcurrent.getType == Material.STEP) {
        //ブロックを並べるスキルハーフブロック設定
        playerdata.lineFillAlign = playerdata.lineFillAlign.next
        player.sendMessage(s"${GREEN}ハーフブロック設定 ：${playerdata.lineFillStepMode.asHumanReadable}")
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(MenuInventoryData.getBlockLineUpData(player))

      } else if (itemstackcurrent.getType == Material.TNT) {
        //ブロックを並べるスキル一部ブロックを破壊して並べる設定
        playerdata.lineFillBreakWeakBlocks = !playerdata.lineFillBreakWeakBlocks
        player.sendMessage(s"${GREEN}破壊設定 ：${BuildAssist.lineFillSwitchMessage(playerdata.lineFillBreakWeakBlocks)}")
        player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
        player.openInventory(MenuInventoryData.getBlockLineUpData(player))

      } else if (itemstackcurrent.getType == Material.CHEST) {
        //マインスタックの方を優先して消費する設定
        if (playerdata.level < BuildAssist.config.getblocklineupMinestacklevel()) {
          player.sendMessage(s"${RED}建築Lvが足りません")
        } else {
          playerdata.lineFillWithMinestack = !playerdata.lineFillWithMinestack
          player.sendMessage(s"${GREEN}マインスタック優先設定 ：${BuildAssist.lineFillSwitchMessage(playerdata.lineFillWithMinestack)}")
          player.playSound(player.getLocation, Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1f, 1f)
          player.openInventory(MenuInventoryData.getBlockLineUpData(player))
        }
      }
    }
  }
}

package com.github.unchama.seichiassist.listener

import java.util.UUID

import com.github.unchama.seichiassist.data.LimitedLoginEvent
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.{ActiveSkill, ManagedWorld, SeichiAssist}
import net.coreprotect.model.Config
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.player.{AsyncPlayerPreLoginEvent, PlayerChangedWorldEvent, PlayerJoinEvent}
import org.bukkit.event.{EventHandler, Listener}
import org.bukkit.inventory.ItemStack
import org.bukkit.{Material, Sound}

import scala.collection.mutable

class PlayerJoinListener extends Listener {
  private val playerMap: mutable.HashMap[UUID, PlayerData] = SeichiAssist.playermap
  private val databaseGateway = SeichiAssist.databaseGateway
  private val failedToLoadDataError =
    "プレーヤーデータの読み込みに失敗しました。再接続しても読み込まれない場合管理者に連絡してください。"

  @EventHandler
  def onPlayerPreLoginEvent(event: AsyncPlayerPreLoginEvent): Unit = {
    val maxTryCount = 10

    (1 until maxTryCount + 1).foreach { tryCount =>
      val isLastTry = tryCount == maxTryCount

      try {
        loadPlayerData(event.getUniqueId, event.getName)
        return
      } catch {
        case e: Exception =>
          if (isLastTry) {
            println("Caught exception while loading PlayerData.")
            e.printStackTrace()

            event.setKickMessage(failedToLoadDataError)
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER)
            return
          }
      }

      // intentional blocking
      Thread.sleep(600)
    }
  }

  private def loadPlayerData(playerUuid: UUID, playerName: String): Unit = {
    SeichiAssist.playermap(playerUuid) =
      databaseGateway.playerDataManipulator.loadPlayerData(playerUuid, playerName)
  }

  // プレイヤーがjoinした時に実行
  @EventHandler
  def onPlayerJoinEvent(event: PlayerJoinEvent): Unit = {
    val player: Player = event.getPlayer

    /*
      サーバー起動してからワールドが読み込まれる前に接続試行をするとAsyncPlayerPreLoginEventが発火されないことがあり、
      そういった場合ではPlayerDataが読み込まれないままここに到達するため、読み込み試行をしてだめだったらキックする。
     */
    if (!playerMap.isDefinedAt(player.getUniqueId)) {
      try {
        loadPlayerData(player.getUniqueId, player.getName)
      } catch {
        case e: Exception =>
          println("Caught exception while loading PlayerData.")
          e.printStackTrace()

          player.kickPlayer(failedToLoadDataError)
          return
      }
    }

    {
      val limitedLoginEvent = new LimitedLoginEvent()
      val playerData = playerMap(player.getUniqueId)

      //期間限定ログインイベント判別処理
      limitedLoginEvent.setLastCheckDate(playerData.lastcheckdate)
      limitedLoginEvent.TryGetItem(player)

      // 1周年記念
      if (playerData.anniversary) {
        player.sendMessage("整地サーバー1周年を記念してアイテムを入手出来ます。詳細はwikiをご確認ください。http://seichi.click/wiki/anniversary")
        player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
      }

      //join時とonenable時、プレイヤーデータを最新の状態に更新
      playerData.updateOnJoin()
    }

    // 初見さんへの処理
    if (!player.hasPlayedBefore) {
      //初見さんであることを全体告知
      Util.sendEveryMessage(LIGHT_PURPLE.toString + "" + BOLD + player.getName + "さんはこのサーバーに初めてログインしました！")
      Util.sendEveryMessage(WHITE.toString + "webサイトはもう読みましたか？→" + YELLOW + "" + UNDERLINE + "https://www.seichi.network/gigantic")
      Util.sendEverySound(Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
      //初見プレイヤーに木の棒、エリトラ、ピッケルを配布
      player.getInventory.addItem(new ItemStack(Material.STICK))
      player.getInventory.addItem(new ItemStack(Material.ELYTRA))
      player.getInventory.addItem(new ItemStack(Material.DIAMOND_PICKAXE))
      player.getInventory.addItem(new ItemStack(Material.DIAMOND_SPADE))

      player.getInventory.addItem(new ItemStack(Material.LOG, 64, 0.toShort),
        new ItemStack(Material.LOG, 64, 0.toShort),
        new ItemStack(Material.LOG, 64, 2.toShort),
        new ItemStack(Material.LOG_2, 64, 1.toShort))

      /* 期間限定ダイヤ配布.期間終了したので64→32に変更して恒久継続 */
      player.getInventory.addItem(new ItemStack(Material.DIAMOND, 32))

      player.sendMessage("初期装備を配布しました。Eキーで確認してネ")
      //メビウスおひとつどうぞ
      MebiusListener.give(player)
      //初見さんにLv1メッセージを送信
      player.sendMessage(SeichiAssist.seichiAssistConfig.getLvMessage(1))
    }

  }

  // プレイヤーがワールドを移動したとき
  @EventHandler
  def onPlayerChangedWorld(event: PlayerChangedWorldEvent): Unit = {
    // 整地ワールドから他のワールドに移動したとき
    if (ManagedWorld.fromBukkitWorld(event.getFrom).exists(_.isSeichi)) {
      val p = event.getPlayer
      val pd = playerMap(p.getUniqueId)

      // coreprotectを切る
      // inspectマップにtrueで登録されている場合
      if (Config.inspecting.getOrDefault(p.getName, false)) {
        // falseに変更する
        p.sendMessage("§3CoreProtect §f- Inspector now disabled.")
        Config.inspecting.put(p.getName, false)
      }

      // アサルトスキルを切る
      // 現在アサルトスキルorアサルトアーマーを選択中
      if (pd.activeskilldata.assaultnum >= 4 && pd.activeskilldata.assaulttype >= 4) {
        // アクティブスキルがONになっている
        if (pd.activeskilldata.mineflagnum != 0) {
          // メッセージを表示
          p.sendMessage(GOLD.toString + ActiveSkill.getActiveSkillName(pd.activeskilldata.assaulttype, pd.activeskilldata.assaultnum) + "：OFF")
          // 内部状態をアサルトOFFに変更
          pd.activeskilldata.updateAssaultSkill(p, pd.activeskilldata.assaulttype, pd.activeskilldata.assaultnum, 0)
          // トグル音を鳴らす
          p.playSound(p.getLocation, Sound.BLOCK_LEVER_CLICK, 1f, 1f)
        }
      }
    }
  }
}

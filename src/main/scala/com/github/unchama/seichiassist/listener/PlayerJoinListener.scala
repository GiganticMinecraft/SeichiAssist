package com.github.unchama.seichiassist.listener

import java.util.UUID

import cats.effect.IO
import com.github.unchama.seichiassist.data.SeichiLvUpMessages
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.seichiskill.SeichiSkillUsageMode.Disabled
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.subsystems.mebius.domain.property.MebiusProperty
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.{ManagedWorld, SeichiAssist}
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
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

    //join時とonenable時、プレイヤーデータを最新の状態に更新
    playerMap(player.getUniqueId).updateOnJoin()

    // 初見さんへの処理
    if (!player.hasPlayedBefore) {
      //初見さんであることを全体告知
      Util.sendEveryMessage(s"$LIGHT_PURPLE$BOLD${player.getName}さんはこのサーバーに初めてログインしました！")
      Util.sendEveryMessage(s"${WHITE}webサイトはもう読みましたか？→$YELLOW${UNDERLINE}https://www.seichi.network/gigantic")
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

      //メビウスおひとつどうぞ
      player.getInventory.setHelmet(BukkitMebiusItemStackCodec.materialize(
        // **getDisplayNameは二つ名も含むのでMCIDにはgetNameが適切**
        MebiusProperty.initialProperty(player.getName, player.getUniqueId.toString),
        damageValue = 0.toShort
      ))

      /* 期間限定ダイヤ配布.期間終了したので64→32に変更して恒久継続 */
      player.getInventory.addItem(new ItemStack(Material.DIAMOND, 32))

      player.sendMessage("初期装備を配布しました。Eキーで確認してネ")
      //初見さんにLv1メッセージを送信
      SeichiLvUpMessages.get(1).foreach(player.sendMessage(_))
    }

    // 整地専用サーバーの場合は上級者向けのサーバーである旨を通知
    if (SeichiAssist.seichiAssistConfig.getServerNum == 5)
      player.sendTitle(
        s"${WHITE}このサーバーは$BLUE${UNDERLINE}上級者向けのサーバー${WHITE}です",
        s"${WHITE}始めたての頃は他のサーバーがおすすめです。", 10, 70, 20)
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
      val skillState = pd.skillState.get.unsafeRunSync()
      if (skillState.usageMode != Disabled) {
        SeichiAssist.instance.assaultSkillRoutines.stopAnyFiber(p).flatMap(stopped =>
          if (stopped)
            FocusedSoundEffect(Sound.BLOCK_LEVER_CLICK, 1f, 1f).run(p)
          else
            IO.unit
        ).unsafeRunSync()
      }
    }
  }
}

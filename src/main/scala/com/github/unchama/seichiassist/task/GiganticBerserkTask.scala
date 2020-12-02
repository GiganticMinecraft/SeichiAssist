package com.github.unchama.seichiassist.task

import java.net.HttpURLConnection

import com.github.unchama.seichiassist.{LevelThresholds, SeichiAssist}
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.subsystems.webhook.service.WebhookService
import com.github.unchama.seichiassist.util.Util
import org.bukkit.{Bukkit, ChatColor, Sound}
import org.bukkit.entity.Player

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Random, Success}

class GiganticBerserkTask {
  private val playermap = SeichiAssist.playermap
  private var player: Player = _
  private var playerdata: PlayerData = _

  def PlayerKillEnemy(p: Player): Unit = {
    player = p
    val uuid = p.getUniqueId
    playerdata = playermap(uuid)
    val mana = playerdata.manaState

    playerdata.GBcd = playerdata.giganticBerserk.cd + 1
    if (playerdata.giganticBerserk.cd >= SeichiAssist.seichiAssistConfig.getGiganticBerserkLimit) {
      if (SeichiAssist.DEBUG) player.sendMessage("上限到達")
      return
    }
    if (playerdata.idleMinute >= 3) return

    //確率でマナを回復させる
    val d = math.random()
    if (d < playerdata.giganticBerserk.manaRegenerationProbability) {
      val i = getRecoveryValue(playerdata)
      mana.increase(i, p, playerdata.level)
      player.sendMessage(s"${ChatColor.YELLOW}${ChatColor.BOLD}${ChatColor.UNDERLINE}Gigantic${ChatColor.RED}${ChatColor.BOLD}${ChatColor.UNDERLINE}Berserk${ChatColor.WHITE}の効果でマナが${i}回復しました")
      player.playSound(player.getLocation, Sound.ENTITY_WITHER_SHOOT, 1, 0.5f)
    }

    //最大レベルの場合終了
    if (playerdata.giganticBerserk.reachedLimit()) return

    //進化待機状態の場合終了
    if (playerdata.giganticBerserk.canEvolve) return

    // stage * level
    val level = playerdata.giganticBerserk.level
    val n = (playerdata.giganticBerserk.stage * 10) + level

    playerdata.GBexp = playerdata.giganticBerserk.exp + 1
    //レベルアップするかどうか判定
    if (LevelThresholds.giganticBerserkLevelList(n) <= playerdata.giganticBerserk.exp)
      if (level <= 8) {
        playerdata.giganticBerserkLevelUp()
        //プレイヤーにメッセージ
        player.sendMessage(s"${ChatColor.YELLOW}${ChatColor.BOLD}${ChatColor.UNDERLINE}Gigantic${ChatColor.RED}${ChatColor.BOLD}${ChatColor.UNDERLINE}Berserk${ChatColor.WHITE}のレベルがアップし、確率が上昇しました")
        player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 0.8f)
        //最大レベルになった時の処理
        if (playerdata.giganticBerserk.reachedLimit()) {
          val webhookURL = SeichiAssist.seichiAssistConfig.getWebhookURL
          if (!webhookURL.equalsIgnoreCase("")) {
            new WebhookService().sendMessage(webhookURL, s"${playerdata.lowercaseName}がパッシブスキル:GiganticBerserkを完成させました！").onComplete {
              case Success(statusCode) =>
                if (statusCode != HttpURLConnection.HTTP_OK && statusCode != HttpURLConnection.HTTP_NO_CONTENT)
                  Bukkit.getLogger.warning(s"Discordへの通知に失敗しました。(ステータスコード: $statusCode)")
              case Failure(exception) =>
                exception.printStackTrace()
                Bukkit.getLogger.warning("Discordへの通知に失敗しました。")
            }
          }
          else Bukkit.getLogger.info("WebhookのURLが空のため、Discordへの通知を行いません。")
          Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, 1.2f)
          Util.sendEveryMessage(s"${ChatColor.GOLD}${ChatColor.BOLD}${playerdata.lowercaseName}がパッシブスキル:${ChatColor.YELLOW}${ChatColor.BOLD}${ChatColor.UNDERLINE}Gigantic${ChatColor.RED}${ChatColor.BOLD}${ChatColor.UNDERLINE}Berserk${ChatColor.GOLD}${ChatColor.BOLD}を完成させました！")
        }
      }
      //レベルが10かつ段階が第2段階の木の剣未満の場合は進化待機状態へ
    else {
      if (playerdata.giganticBerserk.stage <= 4) {
        player.sendMessage(s"${ChatColor.GREEN}パッシブスキルメニューより${ChatColor.YELLOW}${ChatColor.BOLD}${ChatColor.UNDERLINE}Gigantic${ChatColor.RED}${ChatColor.BOLD}${ChatColor.UNDERLINE}Berserk${ChatColor.GREEN}スキルが進化可能です。")
        player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 0.8f)
        playerdata.isGBStageUp = true
      }
    }
  }

  private def getRecoveryValue(playerdata: PlayerData): Double = {
    var i = .0
    var l = .0
    val rnd = new Random
    val level = playerdata.giganticBerserk.level
    playerdata.giganticBerserk.stage match {
      case 0 =>
        i = 300
        level match {
          case 0 =>
            l = 30
          case 1 =>
            l = 35
          case 2 =>
            l = 40
          case 3 =>
            l = 45
          case 4 =>
            l = 50
          case 5 =>
            l = 60
          case 6 =>
            l = 70
          case 7 =>
            l = 80
          case 8 =>
            l = 90
          case 9 =>
            l = 100
          case _ =>
            l = 0
        }
      case 1 =>
        i = 2000
        level match {
          case 0 =>
            l = 200
          case 1 =>
            l = 220
          case 2 =>
            l = 250
          case 3 =>
            l = 270
          case 4 =>
            l = 300
          case 5 =>
            l = 350
          case 6 =>
            l = 400
          case 7 =>
            l = 450
          case 8 =>
            l = 500
          case 9 =>
            l = 600
          case _ =>
            l = 0
        }
      case 2 =>
        i = 15000
        level match {
          case 0 =>
            l = 1500
          case 1 =>
            l = 1650
          case 2 =>
            l = 1800
          case 3 =>
            l = 2000
          case 4 =>
            l = 2200
          case 5 =>
            l = 2400
          case 6 =>
            l = 2600
          case 7 =>
            l = 2800
          case 8 =>
            l = 3000
          case 9 =>
            l = 3200
          case _ =>
            l = 0
        }
      case 3 =>
        i = 40000
        level match {
          case 0 =>
            l = 4000
          case 1 =>
            l = 4400
          case 2 =>
            l = 4800
          case 3 =>
            l = 5200
          case 4 =>
            l = 5600
          case 5 =>
            l = 6000
          case 6 =>
            l = 6500
          case 7 =>
            l = 7000
          case 8 =>
            l = 7500
          case 9 =>
            l = 8000
          case _ =>
            l = 0
        }
      case 4 =>
        i = 100000
        level match {
          case 0 =>
            l = 10000
          case 1 =>
            l = 11000
          case 2 =>
            l = 12000
          case 3 =>
            l = 13000
          case 4 =>
            l = 14000
          case 5 =>
            l = 15000
          case 6 =>
            l = 16000
          case 7 =>
            l = 17000
          case 8 =>
            l = 18500
          case 9 =>
            l = 20000
          case _ =>
            l = 0
        }
      case 5 =>
        i = 160000
        level match {
          case 0 =>
            l = 25000
          case 1 =>
            l = 27500
          case 2 =>
            l = 30000
          case 3 =>
            l = 32500
          case 4 =>
            l = 35000
          case 5 =>
            l = 37500
          case 6 =>
            l = 40000
          case 7 =>
            l = 43000
          case 8 =>
            l = 46000
          case 9 =>
            l = 50000
          case _ =>
            l = 0
        }
      case _ =>
    }
    i -= i / 10
    i += rnd.nextInt(l.toInt + 1)
    i
  }
}

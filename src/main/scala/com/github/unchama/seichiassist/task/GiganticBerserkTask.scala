package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.{LevelThresholds, SeichiAssist}
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.util.Util
import net.md_5.bungee.api.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player

import java.util.Random

object GiganticBerserkTask {
  private val playermap = SeichiAssist.playermap
  private val BERSERK = s"$YELLOW$BOLD${UNDERLINE}Gigantic$RED$BOLD${UNDERLINE}Berserk${RESET}"
  def PlayerKillEnemy(p: Player): Unit = {
    val uuid = p.getUniqueId
    val pd = playermap(uuid)
    val mana = pd.manaState
    pd.GBkillsPerMinute = pd.giganticBerserk.killsPerMinute + 1
    if (pd.giganticBerserk.killsPerMinute >= SeichiAssist.seichiAssistConfig.getGiganticBerserkLimitRatePerMinute) {
      if (SeichiAssist.DEBUG) p.sendMessage("上限到達")
      return
    }
    if (playerdata.idleMinute >= 3) return

    //確率でマナを回復させる
    val d = Math.random
    if (d < playerdata.giganticBerserk.manaRegenerationProbability) {
      val i = getRecoveryValue(playerdata)
      val level =
        SeichiAssist.instance
          .breakCountSystem.api
          .seichiAmountDataRepository(player)
          .read.unsafeRunSync()
          .levelCorrespondingToExp.level

      mana.increase(i, p, level)
      player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Gigantic" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Berserk" + ChatColor.WHITE + "の効果でマナが" + i + "回復しました")
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
    if (LevelThresholds.giganticBerserkLevelList.apply(n).asInstanceOf[Integer] <= playerdata.giganticBerserk.exp) if (level <= 8) {
      playerdata.giganticBerserkLevelUp()
      //プレイヤーにメッセージ
      player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Gigantic" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Berserk" + ChatColor.WHITE + "のレベルがアップし、確率が上昇しました")
      player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 0.8f)
      //最大レベルになった時の処理
      if (playerdata.giganticBerserk.reachedLimit()) {
        Util.sendEverySound(Sound.ENTITY_ENDERDRAGON_DEATH, 1, 1.2f)
        Util.sendEveryMessage(ChatColor.GOLD + "" + ChatColor.BOLD + playerdata.lowercaseName + "がパッシブスキル:" + ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Gigantic" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Berserk" + ChatColor.GOLD + "" + ChatColor.BOLD + "を完成させました！")
      }
    } else { //レベルが10かつ段階が第2段階の木の剣未満の場合は進化待機状態へ
      if (playerdata.giganticBerserk.stage <= 4) {
        player.sendMessage(ChatColor.GREEN + "パッシブスキルメニューより" + ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Gigantic" + ChatColor.RED + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "Berserk" + ChatColor.GREEN + "スキルが進化可能です。")
        player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 0.8f)
        playerdata.isGBStageUp_$eq(true)
      }
    }
  }

  private def getIncreasingMana(playerdata: PlayerData) = {
    val rnd = new Random
    val gb = playerdata.giganticBerserk
    val level = gb.level
    val (i, l) = gb.stage match {
      case 0 =>
        (300, level match {
          case 0 => 30
          case 1 => 35
          case 2 => 40
          case 3 => 45
          case 4 => 50
          case 5 => 60
          case 6 => 70
          case 7 => 80
          case 8 => 90
          case 9 => 100
          case _ => 0
        })

      case 1 =>
        (2000, level match {
          case 0 => 200
          case 1 => 220
          case 2 => 250
          case 3 => 270
          case 4 => 300
          case 5 => 350
          case 6 => 400
          case 7 => 450
          case 8 => 500
          case 9 => 600
          case _ => 0
        })

      case 2 =>
        (15000, level match {
          case 0 => 1500
          case 1 => 1650
          case 2 => 1800
          case 3 => 2000
          case 4 => 2200
          case 5 => 2400
          case 6 => 2600
          case 7 => 2800
          case 8 => 3000
          case 9 => 3200
          case _ => 0
        })

      case 3 =>
        (40000, level match {
          case 0 => 4000
          case 1 => 4400
          case 2 => 4800
          case 3 => 5200
          case 4 => 5600
          case 5 => 6000
          case 6 => 6500
          case 7 => 7000
          case 8 => 7500
          case 9 => 8000
          case _ => 0
        })

      case 4 =>
        (100000, level match {
          case 0 => 10000
          case 1 => 11000
          case 2 => 12000
          case 3 => 13000
          case 4 => 14000
          case 5 => 15000
          case 6 => 16000
          case 7 => 17000
          case 8 => 18500
          case 9 => 20000
          case _ => 0
        })

      case 5 =>
        (160000, level match {
          case 0 => 25000
          case 1 => 27500
          case 2 => 30000
          case 3 => 32500
          case 4 => 35000
          case 5 => 37500
          case 6 => 40000
          case 7 => 43000
          case 8 => 46000
          case 9 => 50000
          case _ => 0
        })

      case _ => (0, 0)
    }
    val i2 = (i * 9 / 10) + rnd.nextInt(l.toInt + 1)
    i2
  }
}
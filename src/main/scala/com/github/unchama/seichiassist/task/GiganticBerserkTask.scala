package com.github.unchama.seichiassist.task

import cats.effect.{ConcurrentEffect, IO, SyncIO}
import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.subsystems.discordnotification.DiscordNotificationAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaAmount
import com.github.unchama.seichiassist.util.{SendMessageEffect, SendSoundEffect}
import com.github.unchama.seichiassist.{LevelThresholds, SeichiAssist}
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player

import scala.util.Random

class GiganticBerserkTask {
  def PlayerKillEnemy[F[_]: ConcurrentEffect: DiscordNotificationAPI](
    p: Player
  )(implicit manaApi: ManaApi[IO, SyncIO, Player]): Unit = {
    val player = p
    val uuid = p.getUniqueId
    val playerdata = SeichiAssist.playermap(uuid)

    playerdata.GBcd = playerdata.giganticBerserk.cd + 1

    if (
      playerdata.giganticBerserk.cd >= SeichiAssist.seichiAssistConfig.getGiganticBerserkLimit
    ) {
      if (SeichiAssist.DEBUG) player.sendMessage("上限到達")
      return
    }

    // 確率でマナを回復させる
    val d = Math.random
    if (d < playerdata.giganticBerserk.manaRegenerationProbability()) {
      if (playerdata.giganticBerserk.reachedLimit()) {
        manaApi.manaAmount(p).restoreCompletely.unsafeRunSync()
        player.sendMessage(
          s"${YELLOW}${BOLD}${UNDERLINE}Gigantic${RED}${BOLD}${UNDERLINE}Berserk${WHITE}の効果でマナが完全回復しました"
        )
      } else {
        val i = getRecoveryValue(playerdata)
        manaApi.manaAmount(p).restoreAbsolute(ManaAmount(i)).unsafeRunSync()
        player.sendMessage(
          s"${YELLOW}${BOLD}${UNDERLINE}Gigantic${RED}${BOLD}${UNDERLINE}Berserk${WHITE}の効果でマナが${i}回復しました"
        )
      }
      player.playSound(player.getLocation, Sound.ENTITY_WITHER_SHOOT, 1, 0.5f)
    }

    // 最大レベルの場合終了
    if (playerdata.giganticBerserk.reachedLimit()) return

    // 進化待機状態の場合終了
    if (playerdata.giganticBerserk.canEvolve) return

    // stage * level
    val level = playerdata.giganticBerserk.level
    val n = (playerdata.giganticBerserk.stage * 10) + level

    playerdata.GBexp = playerdata.giganticBerserk.exp + 1

    // レベルアップするかどうか判定
    if (
      LevelThresholds.giganticBerserkLevelList(n).asInstanceOf[Integer] <= playerdata
        .giganticBerserk
        .exp
    ) if (level <= 8) {
      playerdata.giganticBerserkLevelUp()

      // プレイヤーにメッセージ
      player.sendMessage(
        s"${YELLOW}${BOLD}${UNDERLINE}Gigantic${RED}${BOLD}${UNDERLINE}Berserk${WHITE}のレベルがアップし、確率が上昇しました"
      )
      player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 0.8f)

      // 最大レベルになった時の処理
      if (playerdata.giganticBerserk.reachedLimit()) {
        import cats.effect.implicits._
        import cats.implicits._
        import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread

        val messageWithoutColor =
          s"${playerdata.lowercaseName}がパッシブスキル:GiganticBerserkを完成させました！"

        val messageWithColor =
          s"$GOLD$BOLD${playerdata.lowercaseName}がパッシブスキル:$YELLOW$BOLD${UNDERLINE}Gigantic$RED$BOLD${UNDERLINE}Berserk$GOLD${BOLD}を完成させました！"

        val program = List(
          DiscordNotificationAPI[F].sendPlainText(messageWithoutColor).toIO,
          IO {
            SendSoundEffect.sendEverySound(Sound.ENTITY_ENDER_DRAGON_DEATH, 1, 1.2f)
            SendMessageEffect.sendMessageToEveryoneIgnoringPreference(messageWithColor)
          }
        ).sequence

        program.unsafeRunAsyncAndForget()
      }
    } else { // レベルが10かつ段階が第2段階の木の剣未満の場合は進化待機状態へ
      if (playerdata.giganticBerserk.stage <= 4) {
        player.sendMessage(
          s"${GREEN}パッシブスキルメニューより${YELLOW}${BOLD}${UNDERLINE}Gigantic${RED}${BOLD}${UNDERLINE}Berserk${GREEN}スキルが進化可能です。"
        )
        player.playSound(player.getLocation, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 0.8f)
        playerdata.isGBStageUp = true
      }
    }
  }

  private def getRecoveryValue(playerdata: PlayerData) = {
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
          case _ =>
            l = 0
        }
      case _ =>
        i = 0
        l = 0
    }
    i -= i / 10
    i += rnd.nextInt(l.toInt + 1)
    i
  }
}

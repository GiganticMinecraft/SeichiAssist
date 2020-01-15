package com.github.unchama.seichiassist.task.repeating

import cats.effect.IO
import com.github.unchama.concurrent.{BukkitSyncExecutionContext, RepeatingTask, RepeatingTaskContext}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.achievement.SeichiAchievement
import com.github.unchama.seichiassist.data.potioneffect.FastDiggingEffect
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor._
import org.bukkit.potion.{PotionEffect, PotionEffectType}
import org.bukkit.{Bukkit, Sound}

import scala.concurrent.duration.FiniteDuration

class PlayerDataPeriodicRecalculation(implicit val syncContext: BukkitSyncExecutionContext,
                                      override val context: RepeatingTaskContext) extends RepeatingTask() {

  override val getRepeatInterval: IO[FiniteDuration] = IO {
    import scala.concurrent.duration._

    if (SeichiAssist.DEBUG) 10.seconds else 1.minute
  }

  val routineOnMainThread = IO {
    import scala.jdk.CollectionConverters._

    val config = SeichiAssist.seichiAssistConfig

    //オンラインプレイヤーの人数を取得
    val onlinePlayers = Bukkit.getServer.getOnlinePlayers.asScala
    val onlinenums = onlinePlayers.size

    //プレイヤーマップに記録されているすべてのplayerdataについての処理
    for (player <- onlinePlayers) {
      val playerData = SeichiAssist.playermap(player.getUniqueId)

      //エフェクトデータの持続時間を1200tick引いて、０以下のものを削除
      playerData.calcEffectData()

      //放置判定
      if (playerData.loc.contains(player.getLocation)) {
        // idletime加算
        playerData.idleMinute = playerData.idleMinute + 1
      } else {
        // 現在地点再取得
        playerData.loc = Some(player.getLocation)
        // idletimeリセット
        playerData.idleMinute = 0
      }

      //プレイヤー名を取得
      val name = player.getName
      //総整地量を更新(返り値で重み分け済みの1分間のブロック破壊量が返ってくる)
      val increase = playerData.updateAndCalcMinedBlockAmount()
      //Levelを設定(必ず総整地量更新後に実施！)
      playerData.updateLevel()
      //activeskillpointを設定
      playerData.activeskilldata.updateActiveSkillPoint(player, playerData.level)
      //総プレイ時間更新
      playerData.updatePlayTick()

      //スターレベル更新
      playerData.updateStarLevel()

      //１分間のブロック破壊量による上昇
      playerData.effectdatalist.addOne {
        val amplifier = increase.toDouble * config.getMinuteMineSpeed
        new FastDiggingEffect(amplifier, 2)
      }

      //プレイヤー数による上昇
      playerData.effectdatalist.addOne {
        val amplifier = onlinenums.toDouble * config.getLoginPlayerMineSpeed
        new FastDiggingEffect(amplifier, 1)
      }

      //effect追加の処理
      //実際に適用されるeffect量
      var minespeedlv = 0

      //effectflag ONの時のみ実行
      if (playerData.settings.fastDiggingEffectSuppression.isSuppressionActive.unsafeRunSync()) {
        //合計effect量
        var sum = 0.0
        //最大持続時間
        var maxduration = 0
        //effectdatalistにある全てのeffectについて計算
        for (ed <- playerData.effectdatalist) {
          //effect量を加算
          sum += ed.amplifier
          //持続時間の最大値を取得
          if (maxduration < ed.duration) {
            maxduration = ed.duration
          }
        }
        //実際のeffect値をsum-1の切り捨て整数値に設定
        minespeedlv = (sum - 1).toInt

        //effect上限値を判定
        val maxSpeed = playerData.settings.fastDiggingEffectSuppression.maximumAllowedEffectAmplifier().unsafeRunSync()

        //effect追加の処理
        //実際のeffect値が0より小さいときはeffectを適用しない
        if (minespeedlv < 0 || maxSpeed == 0) {
          player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false), true)
        } else {
          if (minespeedlv > maxSpeed) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, maxSpeed, false, false), true)
          } else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, minespeedlv, false, false), true)
          }
        }

        //プレイヤーデータを更新
        playerData.minespeedlv = minespeedlv
      }

      //前の上昇量と今の上昇量が違うか内訳表示フラグがオンの時告知する
      if (playerData.lastminespeedlv != minespeedlv || playerData.settings.receiveFastDiggingEffectStats) {
        player.sendMessage(s"$YELLOW★${WHITE}採掘速度上昇レベルが$YELLOW${minespeedlv + 1}${WHITE}になりました")
        if (playerData.settings.receiveFastDiggingEffectStats) {
          player.sendMessage("----------------------------内訳-----------------------------")
          for (ed <- playerData.effectdatalist) {
            player.sendMessage(s"$RESET$RED${ed.effectDescription}")
          }
          player.sendMessage("-------------------------------------------------------------")
        }
      }

      //プレイヤーデータを更新
      playerData.lastminespeedlv = minespeedlv

      /*
       * ガチャ券付与の処理
       */
      if (playerData.gachapoint >= config.getGachaPresentInterval && playerData.settings.receiveGachaTicketEveryMinute) {
        val skull = Util.getskull(name)
        playerData.gachapoint = playerData.gachapoint - config.getGachaPresentInterval
        if (player.getInventory.contains(skull) || !Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, skull)
          player.sendMessage(s"${GOLD}ガチャ券${WHITE}プレゼントフォーユー。右クリックで使えるゾ")
        } else {
          Util.dropItem(player, skull)
          player.playSound(player.getLocation, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
          player.sendMessage(s"${GOLD}ガチャ券${WHITE}がドロップしました。右クリックで使えるゾ")
        }
      } else {
        if (increase != 0 && playerData.settings.receiveGachaTicketEveryMinute) {
          player.sendMessage(s"あと$AQUA${config.getGachaPresentInterval - playerData.gachapoint % config.getGachaPresentInterval}${WHITE}ブロック整地すると${GOLD}ガチャ券${WHITE}獲得ダヨ")
        }
      }


      import SeichiAchievement._
      import cats.implicits._

      /*
       * 実績解除判定
       */
      autoUnlockedAchievements
        .filterNot(achievement => playerData.TitleFlags.contains(achievement.id))
        .map { achievement => achievement.asUnlockable.shouldUnlockFor(player).map((achievement.id, _)) }
        .toList
        .sequence
        .map(_.flatMap {
          case (achievementId, true) => Some(achievementId)
          case _ => None
        })
        .flatMap(unlockTargets => IO {
          playerData.TitleFlags.addAll(unlockTargets)
          unlockTargets
            .map("実績No" + _ + "が解除されました！おめでとうございます！")
            .foreach(player.sendMessage)
        })
        .unsafeRunSync()

      //投票妖精関連
      if (playerData.usingVotingFairy) {
        VotingFairyTask.run(player)
      }

      //GiganticBerserk
      playerData.GBcd = 0

    }
  }

  override val runRoutine: IO[Unit] = for {
    _ <- IO.shift(syncContext)
    _ <- routineOnMainThread
  } yield ()
}

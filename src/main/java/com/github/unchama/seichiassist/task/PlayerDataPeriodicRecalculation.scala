package com.github.unchama.seichiassist.task

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.achievement.SeichiAchievement
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import org.bukkit.Bukkit
import org.bukkit.ChatColor._
import org.bukkit.potion.PotionEffectType
object PlayerDataPeriodicRecalculation extends RepeatedTaskLauncher() {
  override def getRepeatIntervalTicks(): Long = if (SeichiAssist.DEBUG) 20 * 10 else 20 * 60

  override @SuspendingMethod def runRoutine() {
    val playerMap = SeichiAssist.playermap
    val config = SeichiAssist.seichiAssistConfig

    //playermapが空の時return
    if (playerMap.isEmpty()) {
      return
    }

    //オンラインプレイヤーの人数を取得
    val onlinenums = Bukkit.getServer().onlinePlayers.size

    //プレイヤーマップに記録されているすべてのplayerdataについての処理
    for (playerData in playerMap.values) {
      //プレイヤーのオンラインオフラインに関係なく処理
      //エフェクトデータの持続時間を1200tick引いて、０以下のものを削除
      playerData.calcEffectData()

      //プレイヤーがオフラインの時処理を終了、次のプレイヤーへ
      if (playerData.isOffline) {
        continue
      }

      //プレイﾔｰが必ずオンラインと分かっている処理
      val player = Bukkit.getPlayer(playerData.uuid)

      //放置判定
      if (player.location == playerData.loc) {
        // idletime加算
        playerData.idleMinute = playerData.idleMinute + 1
      } else {
        // 現在地点再取得
        playerData.loc = player.location
        // idletimeリセット
        playerData.idleMinute = 0
      }

      //プレイヤー名を取得
      val name = player.name
      //総整地量を更新(返り血で重み分け済みの1分間のブロック破壊量が返ってくる)
      val increase = playerData.updateAndCalcMinedBlockAmount()
      //Levelを設定(必ず総整地量更新後に実施！)
      playerData.updateLevel()
      //activeskillpointを設定
      playerData.activeskilldata.updateActiveSkillPoint(player, playerData.level)
      //総プレイ時間更新
      playerData.updatePlayTick()

      //スターレベル更新
      playerData.updateStarLevel()

      //effectの大きさ
      var amplifier: Double
      //effectのメッセージ
      //１分間のブロック破壊量による上昇
      amplifier = increase.toDouble() * config.minuteMineSpeed
      playerData.effectdatalist.add(FastDiggingEffect(amplifier, 2))

      //プレイヤー数による上昇
      amplifier = onlinenums.toDouble() * config.loginPlayerMineSpeed
      playerData.effectdatalist.add(FastDiggingEffect(amplifier, 1))

      //effect追加の処理
      //実際に適用されるeffect量
      var minespeedlv = 0

      //effectflag ONの時のみ実行
      if (playerData.settings.fastDiggingEffectSuppression.isSuppressionActive()) {
        //合計effect量
        var sum = 0.0
        //最大持続時間
        var maxduration = 0
        //effectdatalistにある全てのeffectについて計算
        for (ed in playerData.effectdatalist) {
          //effect量を加算
          sum += ed.amplifier
          //持続時間の最大値を取得
          if (maxduration < ed.duration) {
            maxduration = ed.duration
          }
        }
        //実際のeffect値をsum-1の切り捨て整数値に設定
        minespeedlv = (sum - 1).toInt()

        //effect上限値を判定
        val maxSpeed = playerData.settings.fastDiggingEffectSuppression.maximumAllowedEffectAmplifier()

        //effect追加の処理
        //実際のeffect値が0より小さいときはeffectを適用しない
        if (minespeedlv < 0 || maxSpeed == 0) {
          player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 0, 0, false, false), true)
        } else {
          if (minespeedlv > maxSpeed) {
            player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, maxSpeed, false, false), true)
          } else {
            player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, maxduration, minespeedlv, false, false), true)
          }
        }

        //プレイヤーデータを更新
        playerData.minespeedlv = minespeedlv
      }

      //前の上昇量と今の上昇量が違うか内訳表示フラグがオンの時告知する
      if (playerData.lastminespeedlv != minespeedlv || playerData.settings.receiveFastDiggingEffectStats) {
        player.sendMessage(s"${YELLOW}★${WHITE}採掘速度上昇レベルが$YELLOW${minespeedlv + 1}${WHITE}になりました")
        if (playerData.settings.receiveFastDiggingEffectStats) {
          player.sendMessage("----------------------------内訳-----------------------------")
          for (ed in playerData.effectdatalist) {
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
      if (playerData.gachapoint >= config.gachaPresentInterval && playerData.settings.receiveGachaTicketEveryMinute) {
        val skull = Util.skull(name)
        playerData.gachapoint = playerData.gachapoint - config.gachaPresentInterval
        if (player.inventory.contains(skull) || !Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, skull)
          player.sendMessage(s"${GOLD}ガチャ券${WHITE}プレゼントフォーユー。右クリックで使えるゾ")
        } else {
          Util.dropItem(player, skull)
          player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
          player.sendMessage(s"${GOLD}ガチャ券${WHITE}がドロップしました。右クリックで使えるゾ")
        }
      } else {
        if (increase != 0 && playerData.settings.receiveGachaTicketEveryMinute) {
          player.sendMessage(s"あと$AQUA${config.gachaPresentInterval - playerData.gachapoint % config.gachaPresentInterval}${WHITE}ブロック整地すると${GOLD}ガチャ券${WHITE}獲得ダヨ")
        }
      }


      /*
			 * 実績解除判定
			 */
      List(
          1001 until 1013,
          3001 until 3019,
          4001 until 4023,
          5001 until 5008,
          5101 until 5020,
          6001 until 6008,
          8001 until 8002
      ).flatten().forEach { achievementNumber =>
        if (!playerData.TitleFlags.(achievementNumber)) {
          SeichiAchievement.tryAchieve(player, achievementNumber)
        }
      }

      //投票妖精関連
      if (playerData.usingVotingFairy) {
        VotingFairyTask.run(player)
      }

      //GiganticBerserk
      playerData.GBcd = 0

    }
  }
}

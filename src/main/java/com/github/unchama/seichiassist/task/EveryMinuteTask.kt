package com.github.unchama.seichiassist.task


import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.achievement.SeichiAchievement
import com.github.unchama.seichiassist.data.EffectData
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

/**
 * 1分に1回回してる処理
 * @author unchama
 */
//newインスタンスが立ち上がる際に変数を初期化したり代入したりする処理
class EveryMinuteTask : BukkitRunnable() {
  private val plugin = SeichiAssist.instance
  private val playermap = SeichiAssist.playermap
  private val config = SeichiAssist.seichiAssistConfig

  override fun run() {
    // プレイヤーの１分間の処理を実行

    //playermapが空の時return
    if (playermap.isEmpty()) {
      return
    }

    //オンラインプレイヤーの人数を取得
    val onlinenums = plugin.server.onlinePlayers.size

    //プレイヤーマップに記録されているすべてのplayerdataについての処理
    for (playerdata in playermap.values) {
      //プレイヤーのオンラインオフラインに関係なく処理
      //エフェクトデータの持続時間を1200tick引いて、０以下のものを削除
      playerdata.calcEffectData()

      //プレイヤーがオフラインの時処理を終了、次のプレイヤーへ
      if (playerdata.isOffline) {
        continue
      }
      //プレイﾔｰが必ずオンラインと分かっている処理
      //プレイヤーを取得
      val player = plugin.server.getPlayer(playerdata.uuid)

      //放置判定
      if (player.location == playerdata.loc) {
        // idletime加算
        playerdata.idletime = playerdata.idletime + 1
      } else {
        // 現在地点再取得
        playerdata.loc = player.location
        // idletimeリセット
        playerdata.idletime = 0
      }

      //プレイヤー名を取得
      val name = Util.getName(player)
      //総整地量を更新(返り血で重み分け済みの1分間のブロック破壊量が返ってくる)
      val increase = playerdata.calcMineBlock(player)
      //Levelを設定(必ず総整地量更新後に実施！)
      playerdata.updateLevel(player)
      //activeskillpointを設定
      playerdata.activeskilldata.updateActiveSkillPoint(player, playerdata.level)
      //総プレイ時間更新
      playerdata.calcPlayTick(player)

      //スターレベル更新
      playerdata.calcStarLevel(player)

      //effectの大きさ
      var amplifier: Double
      //effectのメッセージ
      //１分間のブロック破壊量による上昇
      amplifier = increase.toDouble() * config.minuteMineSpeed
      playerdata.effectdatalist.add(EffectData(amplifier, 2))

      //プレイヤー数による上昇
      amplifier = onlinenums.toDouble() * config.loginPlayerMineSpeed
      playerdata.effectdatalist.add(EffectData(amplifier, 1))

      //effect追加の処理
      //実際に適用されるeffect量
      var minespeedlv = 0

      //effectflag ONの時のみ実行
      if (playerdata.effectflag != 5) {
        //合計effect量
        var sum = 0.0
        //最大持続時間
        var maxduration = 0
        //effectdatalistにある全てのeffectについて計算
        for (ed in playerdata.effectdatalist) {
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
        var maxSpeed = 0
        if (playerdata.effectflag == 0) {
          maxSpeed = 25565
        } else if (playerdata.effectflag == 1) {
          maxSpeed = 127
        } else if (playerdata.effectflag == 2) {
          maxSpeed = 200
        } else if (playerdata.effectflag == 3) {
          maxSpeed = 400
        } else if (playerdata.effectflag == 4) {
          maxSpeed = 600
        }

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
        playerdata.minespeedlv = minespeedlv
      }

      //プレイヤーにメッセージ送信
      if (playerdata.lastminespeedlv != minespeedlv || playerdata.messageflag) {//前の上昇量と今の上昇量が違うか内訳表示フラグがオンの時告知する
        player.sendMessage(ChatColor.YELLOW.toString() + "★" + ChatColor.WHITE + "採掘速度上昇レベルが" + ChatColor.YELLOW + (minespeedlv + 1) + ChatColor.WHITE + "になりました")
        if (playerdata.messageflag) {
          player.sendMessage("----------------------------内訳-----------------------------")
          for (ed in playerdata.effectdatalist) {
            player.sendMessage(ChatColor.RESET.toString() + "" + ChatColor.RED + "" + ed.EDtoString(ed.id, ed.duration, ed.amplifier))
          }
          player.sendMessage("-------------------------------------------------------------")
        }
      }

      //プレイヤーデータを更新
      playerdata.lastminespeedlv = minespeedlv

      /*
			 * ガチャ券付与の処理
			 */

      //ガチャポイントに合算
      playerdata.gachapoint = playerdata.gachapoint + increase

      if (playerdata.gachapoint >= config.gachaPresentInterval && playerdata.gachaflag) {
        val skull = Util.getskull(name)
        playerdata.gachapoint = playerdata.gachapoint - config.gachaPresentInterval
        if (player.inventory.contains(skull) || !Util.isPlayerInventoryFull(player)) {
          Util.addItem(player, skull)
          player.sendMessage(ChatColor.GOLD.toString() + "ガチャ券" + ChatColor.WHITE + "プレゼントフォーユー。右クリックで使えるゾ")
        } else {
          Util.dropItem(player, skull)
          player.playSound(player.location, Sound.BLOCK_ANVIL_PLACE, 1f, 1f)
          player.sendMessage(ChatColor.GOLD.toString() + "ガチャ券" + ChatColor.WHITE + "がドロップしました。右クリックで使えるゾ")
        }
      } else {
        if (increase != 0 && playerdata.gachaflag) {
          player.sendMessage("あと" + ChatColor.AQUA + (config.gachaPresentInterval - playerdata.gachapoint % config.gachaPresentInterval) + ChatColor.WHITE + "ブロック整地すると" + ChatColor.GOLD + "ガチャ券" + ChatColor.WHITE + "獲得ダヨ")
        }
      }
      //プレイヤーデータを更新
      playerdata.lastgachapoint = playerdata.gachapoint


      /*
			 * 実績解除判定
			 */
      //No1000系統の解禁チェック
      var checkNo = 1001
      while (checkNo < 1013) {
        if (!playerdata.TitleFlags.get(checkNo)) {
          SeichiAchievement.tryAchieve(player, checkNo)
        }
        checkNo++
      }
      //No3000系統の解禁チェック
      checkNo = 3001
      while (checkNo < 3020) {
        if (!playerdata.TitleFlags.get(checkNo)) {
          SeichiAchievement.tryAchieve(player, checkNo)
        }
        checkNo++
      }
      //No4000系統の解禁チェック
      checkNo = 4001
      while (checkNo < 4024) {
        if (!playerdata.TitleFlags.get(checkNo)) {
          SeichiAchievement.tryAchieve(player, checkNo)
        }
        checkNo++
      }
      //No5000系統の解禁チェック
      checkNo = 5001
      while (checkNo < 5009) {
        if (!playerdata.TitleFlags.get(checkNo)) {
          SeichiAchievement.tryAchieve(player, checkNo)
        }
        checkNo++
      }
      //No5100系統の解禁チェック
      checkNo = 5101
      while (checkNo < 5121) {
        if (!playerdata.TitleFlags.get(checkNo)) {
          SeichiAchievement.tryAchieve(player, checkNo)
        }
        checkNo++
      }
      //No6000系統の解禁チェック
      checkNo = 6001
      while (checkNo < 6009) {
        if (!playerdata.TitleFlags.get(checkNo)) {
          SeichiAchievement.tryAchieve(player, checkNo)
        }
        checkNo++
      }
      //No8000系統の解禁チェック
      checkNo = 8001
      while (checkNo < 8003) {
        if (!playerdata.TitleFlags.get(checkNo)) {
          SeichiAchievement.tryAchieve(player, checkNo)
        }
        checkNo++
      }

      //投票妖精関連
      if (playerdata.usingVotingFairy) {
        VotingFairyTask.run(player)
      }

      //GiganticBerserk
      playerdata.GBcd = 0

    }

  }
}

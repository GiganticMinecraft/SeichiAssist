package com.github.unchama.seichiassist.listener

import cats.effect.{IO, SyncIO}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.seichiassist.subsystems.mana.domain.ManaAmount
import com.github.unchama.seichiassist.subsystems.mana.{ManaApi, ManaReadApi}
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.event.Listener

import java.util.{Calendar, GregorianCalendar}
import scala.util.Random

object VotingFairyListener {
  def summon(p: Player)(implicit manaApi: ManaReadApi[IO, SyncIO, Player]): Unit = {
    val playermap = SeichiAssist.playermap
    val uuid = p.getUniqueId
    val playerdata = playermap.apply(uuid)

    // 召喚した時間を取り出す
    playerdata.votingFairyStartTime = new GregorianCalendar(
      Calendar.getInstance.get(Calendar.YEAR),
      Calendar.getInstance.get(Calendar.MONTH),
      Calendar.getInstance.get(Calendar.DATE),
      Calendar.getInstance.get(Calendar.HOUR_OF_DAY),
      Calendar.getInstance.get(Calendar.MINUTE)
    )

    var min = Calendar.getInstance.get(Calendar.MINUTE) + 1
    var hour = Calendar.getInstance.get(Calendar.HOUR_OF_DAY)
    min = if ((playerdata.toggleVotingFairy % 2) != 0) min + 30 else min
    hour =
      if (playerdata.toggleVotingFairy == 2) hour + 1
      else if (playerdata.toggleVotingFairy == 3) hour + 1
      else if (playerdata.toggleVotingFairy == 4) hour + 2
      else hour

    playerdata.votingFairyEndTime = new GregorianCalendar(
      Calendar.getInstance.get(Calendar.YEAR),
      Calendar.getInstance.get(Calendar.MONTH),
      Calendar.getInstance.get(Calendar.DATE),
      hour,
      min
    )

    // 投票ptを減らす
    playerdata.effectPoint_$eq(playerdata.effectPoint - playerdata.toggleVotingFairy * 2)

    // フラグ
    playerdata.usingVotingFairy = true

    // マナ回復量最大値の決定
    val n = manaApi.readManaAmount(p).unsafeRunSync().cap.value
    playerdata.VotingFairyRecoveryValue =
      ((n / 10 - n / 30 + new Random().nextInt((n / 20).toInt)) / 2.9).toInt + 200

    p.sendMessage(s"$RESET$YELLOW${BOLD}妖精を呼び出しました！")
    p.sendMessage(s"$RESET$YELLOW${BOLD}この子は1分間に約${playerdata.VotingFairyRecoveryValue}マナ")
    p.sendMessage(s"$RESET$YELLOW${BOLD}回復させる力を持っているようです。")

    // メッセージ

    val morning = List(
      "おはよ！[str1]",
      "ヤッホー[str1]！",
      "ふわぁ。。。[str1]の朝は早いね。",
      "うーん、今日も一日頑張ろ！",
      "今日は整地日和だね！[str1]！"
    )
    val day = List(
      "やあ！[str1]",
      "ヤッホー[str1]！",
      "あっ、[str1]じゃん。丁度お腹空いてたんだ！",
      "この匂い…[str1]ってがちゃりんごいっぱい持ってる…?",
      "今日のおやつはがちゃりんごいっぱいだ！"
    )
    val night = List(
      "やあ！[str1]",
      "ヤッホー[str1]！",
      "ふわぁ。。。[str1]は夜も元気だね。",
      "もう寝ようと思ってたのにー。[str1]はしょうがないなぁ",
      "こんな時間に呼ぶなんて…りんごははずんでもらうよ？"
    )

    if (Util.getTimeZone(playerdata.votingFairyStartTime) == "morning") {
      VotingFairyTask.speak(p, getMessage(morning, p.getName), playerdata.toggleVFSound)
    } else if (Util.getTimeZone(playerdata.votingFairyStartTime) == "day") {
      VotingFairyTask.speak(p, getMessage(day, p.getName), playerdata.toggleVFSound)
    } else VotingFairyTask.speak(p, getMessage(night, p.getName), playerdata.toggleVFSound)
  }

  private def getMessage(messages: List[String], str1: String) = {
    val msg = messages(Random.nextInt(messages.size))
    if (str1.nonEmpty) msg.replace("[str1]", str1 + RESET)
    else msg
  }

  def regeneMana(player: Player)(implicit manaApi: ManaApi[IO, SyncIO, Player]): Unit = {
    val playermap = SeichiAssist.playermap
    val uuid = player.getUniqueId
    val playerdata = playermap.apply(uuid)

    val oldManaAmount = manaApi.readManaAmount(player).unsafeRunSync()

    if (oldManaAmount.isFull) {
      // マナが最大だった場合はメッセージを送信して終わり
      val msg = List(
        "整地しないのー？",
        "たくさん働いて、たくさんりんごを食べようね！",
        "僕はいつか大きながちゃりんごを食べ尽して見せるっ！",
        "ちょっと食べ疲れちゃった",
        "[str1]はどのりんごが好き？僕はがちゃりんご！",
        "動いてお腹を空かしていっぱい食べるぞー！"
      )
      VotingFairyTask.speak(player, getMessage(msg, player.getName), playerdata.toggleVFSound)
    } else {
      val playerLevel =
        SeichiAssist
          .instance
          .breakCountSystem
          .api
          .seichiAmountDataRepository(player)
          .read
          .unsafeRunSync()
          .levelCorrespondingToExp

      var n = playerdata.VotingFairyRecoveryValue // 実際のマナ回復量
      var m = getGiveAppleValue(playerLevel) // りんご消費量

      // 連続投票によってりんご消費量を抑える
      if (playerdata.ChainVote >= 30) m /= 2
      else if (playerdata.ChainVote >= 10) m = (m / 1.5).toInt
      else if (playerdata.ChainVote >= 3) m = (m / 1.25).toInt

      // トグルで数値変更
      if (playerdata.toggleGiveApple == 2)
        if (oldManaAmount.ratioToCap.exists(_ >= 0.75)) {
          n /= 2
          m /= 2
        } else if (playerdata.toggleGiveApple == 3) {
          n /= 2
          m /= 2
        }

      if (m == 0) m = 1

      if (playerdata.toggleGiveApple == 4) {
        n /= 4
        m = 0
      } else {
        // ちょっとつまみ食いする
        if (m >= 10) m += new Random().nextInt(m / 10)
      }

      // りんご所持数で値変更
      val gachaimoObject = Util.findMineStackObjectByName("gachaimo").get
      val l = playerdata.minestack.getStackedAmountOf(gachaimoObject)
      if (m > l) {
        if (l == 0) {
          n /= 2
          if (playerdata.toggleGiveApple == 1) n /= 2
          if (playerdata.toggleGiveApple == 2 && oldManaAmount.ratioToCap.exists(_ < 0.75))
            n /= 2
          player.sendMessage(s"$RESET$YELLOW${BOLD}MineStackにがちゃりんごがないようです。。。")
        } else {
          val M = m
          val L = l
          n = if ((L / M) <= 0.5) (n * 0.5).toInt else (n * L / M).toInt
        }

        m = l.toInt
      }

      // 回復量に若干乱数をつける
      n = (n - n / 100) + Random.nextInt(n / 50)

      // マナ回復
      manaApi.manaAmount(player).restoreAbsolute(ManaAmount(n)).unsafeRunSync()

      // りんごを減らす
      playerdata
        .minestack
        .subtractStackedAmountOf(Util.findMineStackObjectByName("gachaimo").get, m)

      // 減ったりんごの数をplayerdataに加算
      playerdata.p_apple += m

      val yes =
        List("(´～｀)ﾓｸﾞﾓｸﾞ…", "がちゃりんごって美味しいよね！", "あぁ！幸せ！", "[str1]のりんごはおいしいなぁ", "いつもりんごをありがとう！")
      val no = List(
        "お腹空いたなぁー。",
        "がちゃりんごがっ！食べたいっ！",
        "(´；ω；`)ｳｩｩ ﾋﾓｼﾞｲ...",
        "＠うんちゃま [str1]が意地悪するんだっ！",
        "うわーん！お腹空いたよー！"
      )

      player.sendMessage(s"$RESET$YELLOW${BOLD}マナ妖精が${n}マナを回復してくれました")

      if (m != 0) {
        player.sendMessage(s"$RESET$YELLOW${BOLD}あっ！${m}個のがちゃりんごが食べられてる！")
        VotingFairyTask.speak(player, getMessage(yes, player.getName), playerdata.toggleVFSound)
      } else {
        player.sendMessage(s"$RESET$YELLOW${BOLD}あなたは妖精にりんごを渡しませんでした。")
        VotingFairyTask.speak(player, getMessage(no, player.getName), playerdata.toggleVFSound)
      }
    }
  }

  private def getGiveAppleValue(playerLevel: SeichiLevel): Int = {
    // 10で切り捨て除算して二乗する。最低でも1は返す。
    val levelDividedByTen = playerLevel.level / 10

    (levelDividedByTen * levelDividedByTen) max 1
  }
}

class VotingFairyListener extends Listener {}

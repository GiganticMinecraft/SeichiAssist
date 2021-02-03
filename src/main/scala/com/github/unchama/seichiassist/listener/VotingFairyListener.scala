package com.github.unchama.seichiassist.listener

import com.github.unchama.seichiassist.data.player.PlayerData
import com.github.unchama.seichiassist.task.VotingFairyTask
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.seichiassist.util.enumeration.TimePeriodOfDay
import com.github.unchama.seichiassist.util.typeclass.OrderedCollection
import com.github.unchama.seichiassist.{MineStackObjectList, SeichiAssist}
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.entity.Player

import java.util.{Calendar, GregorianCalendar, Random}

object VotingFairyListener {
  /**
   * メッセージが送信されるときにプレイヤー名に置き換えられる任意のプレースホルダー
   */
  private val playerNameMacro = "[str1]"
  private val messagesOnSummon: Map[TimePeriodOfDay, List[String]] = Map(
    TimePeriodOfDay.Morning -> List(
      s"おはよ！$playerNameMacro",
      s"ヤッホー$playerNameMacro！",
      s"ふわぁ。。。${playerNameMacro}の朝は早いね。",
      s"うーん、今日も一日頑張ろ！",
      s"今日は整地日和だね！$playerNameMacro！"
    ),

    TimePeriodOfDay.Day -> List(
      s"やあ！$playerNameMacro",
      s"ヤッホー$playerNameMacro！",
      s"あっ、${playerNameMacro}じゃん。丁度お腹空いてたんだ！",
      s"この匂い…${playerNameMacro}ってがちゃりんごいっぱい持ってる…?",
      "今日のおやつはがちゃりんごいっぱいだ！"
    ),

    TimePeriodOfDay.Night -> List(
      s"やあ！$playerNameMacro",
      s"ヤッホー$playerNameMacro！",
      s"ふわぁ。。。${playerNameMacro}は夜も元気だね。",
      s"もう寝ようと思ってたのにー。${playerNameMacro}はしょうがないなぁ",
      "こんな時間に呼ぶなんて…りんごははずんでもらうよ？"
    )
  )
  private val mesWhenFull = List(
    "整地しないのー？",
    "たくさん働いて、たくさんりんごを食べようね！",
    "僕はいつか大きながちゃりんごを食べ尽して見せるっ！",
    "ちょっと食べ疲れちゃった",
    s"${playerNameMacro}はどのりんごが好き？僕はがちゃりんご！",
    "動いてお腹を空かしていっぱい食べるぞー！"
  )
  private val yes = List(
    "(´～｀)ﾓｸﾞﾓｸﾞ…",
    "がちゃりんごって美味しいよね！",
    "あぁ！幸せ！",
    s"${playerNameMacro}のりんごはおいしいなぁ",
    "いつもりんごをありがとう！"
  )
  private val no = List(
    "お腹空いたなぁー。",
    "がちゃりんごがっ！食べたいっ！",
    "(´；ω；`)ｳｩｩ ﾋﾓｼﾞｲ...",
    s"＠うんちゃま ${playerNameMacro}が意地悪するんだっ！",
    "うわーん！お腹空いたよー！"
  )
  def summon(p: Player): Unit = {
    val playermap = SeichiAssist.playermap
    val uuid = p.getUniqueId
    val playerdata = playermap(uuid)
    val mana = playerdata.manaState
    //召喚した時間を取り出す
    val currentCalendar = Calendar.getInstance
    playerdata.votingFairyStartTime = new GregorianCalendar(
      currentCalendar.get(Calendar.YEAR),
      currentCalendar.get(Calendar.MONTH),
      currentCalendar.get(Calendar.DATE),
      currentCalendar.get(Calendar.HOUR_OF_DAY),
      currentCalendar.get(Calendar.MINUTE)
    )
    val min = currentCalendar.get(Calendar.MINUTE) + 1
    val hour = currentCalendar.get(Calendar.HOUR_OF_DAY)
    val (addHour, addMin) = {
      playerdata.toggleVotingFairy match {
        case 1 => (0, 30)
        case 2 => (1, 0)
        case 3 => (1, 30)
        case 4 => (2, 0)
      }
    }
    val (finalHour, finalMin) = (hour + addHour, min + addMin)
    playerdata.votingFairyEndTime = new GregorianCalendar(
      currentCalendar.get(Calendar.YEAR),
      currentCalendar.get(Calendar.MONTH),
      currentCalendar.get(Calendar.DATE),
      finalHour,
      finalMin
    )
    //投票ptを減らす
    playerdata.effectPoint -= playerdata.toggleVotingFairy * 2
    //フラグ
    playerdata.usingVotingFairy = true
    //マナ回復量最大値の決定
    val n = mana.getMax
    val increasingMana = ((n / 10 - n / 30 + new Random().nextInt((n / 20).toInt)) / 2.9).toInt + 200
    playerdata.VotingFairyRecoveryValue = increasingMana
    SequentialEffect(
      MessageEffect(s"$RESET$YELLOW${BOLD}妖精を呼び出しました！"),
      MessageEffect(s"$RESET$YELLOW${BOLD}この子は1分間に約${increasingMana}マナ"),
      MessageEffect(s"$RESET$YELLOW${BOLD}回復させる力を持っているようです。"),
      VotingFairyTask.speak(
        getMessage(messagesOnSummon(Util.getTimePeriod(playerdata.votingFairyStartTime)), p.getName),
        playerdata.playFairySound
      )
    )
      .run(p)
      .unsafeRunAsyncAndForget()
  }

  def regeneMana(p: Player): Unit = {
    val playermap = SeichiAssist.playermap
    val uuid = p.getUniqueId
    val playerdata = playermap(uuid)
    val mana = playerdata.manaState
    if (mana.getMana == mana.getMax) {
      //マナが最大だった場合はメッセージを送信して終わり
      VotingFairyTask.speak(getMessage(mesWhenFull, p.getName), playerdata.playFairySound)
        .run(p)
        .unsafeRunAsyncAndForget()
    } else {
      var increasingMana = playerdata.VotingFairyRecoveryValue.toDouble
      var consumingQuantity = getGiveAppleValue(playerdata)
      //連続投票によってりんご消費量を抑える
      val discountRate = playerdata.ChainVote match {
        case _ >= 30 => 2
        case _ >= 10 => 1.5
        case _ >= 5  => 1.25
        case _       => 1
      }

      consumingQuantity /= discountRate

      //トグルで数値変更
      if (playerdata.toggleGiveApple == 2) {
        if (mana.getMana / mana.getMax >= 0.75) {
          increasingMana /= 2
          consumingQuantity /= 2
        }
      } else if (playerdata.toggleGiveApple == 3) {
        increasingMana /= 2
        consumingQuantity /= 2
      }

      if (consumingQuantity == 0) consumingQuantity = 1

      if (playerdata.toggleGiveApple == 4) {
        increasingMana /= 4
        consumingQuantity = 0
      } else { //ちょっとつまみ食いする
        if (consumingQuantity >= 10) consumingQuantity += new Random().nextInt(consumingQuantity / 10)
      }
      //りんご所持数で値変更
      val gachaimoObject = MineStackObjectList.findByName("gachaimo").get
      val left = playerdata.minestack.getStackedAmountOf(gachaimoObject)
      if (consumingQuantity > left) {
        if (left == 0) {
          increasingMana /= 2
          if (playerdata.toggleGiveApple == 1) increasingMana /= 2
          if (playerdata.toggleGiveApple == 2 && (mana.getMana / mana.getMax < 0.75)) increasingMana /= 2
          p.sendMessage(s"$RESET$YELLOW${BOLD}MineStackにがちゃりんごがないようです。。。")
        }
        else {
          val M = consumingQuantity.toDouble
          val L = left.toDouble
          val percentage = L / M
          increasingMana = if (percentage <= 0.5) increasingMana * 0.5
          else increasingMana * percentage
        }
        consumingQuantity = left.toInt
      }
      //回復量に若干乱数をつける
      increasingMana = (increasingMana - increasingMana / 100) + new Random().nextInt((increasingMana / 50).toInt)
      //マナ回復
      mana.increase(increasingMana, p, playerdata.level)
      //りんごを減らす
      playerdata.minestack.subtractStackedAmountOf(MineStackObjectList.findByName("gachaimo").get, consumingQuantity)
      //減ったりんごの数をplayerdataに加算
      playerdata.p_apple += consumingQuantity

      val afterEffect = if (consumingQuantity == 0) {
        List(
          MessageEffect(s"$RESET$YELLOW${BOLD}あなたは妖精にりんごを渡しませんでした。"),
          VotingFairyTask.speak(getMessage(no, p.getName), playerdata.playFairySound),
        )
      } else {
        List(
          MessageEffect(s"$RESET$YELLOW${BOLD}あっ！${consumingQuantity}個のがちゃりんごが食べられてる！"),
          VotingFairyTask.speak(getMessage(yes, p.getName), playerdata.playFairySound)
        )
      }

      SequentialEffect(
        List(
          MessageEffect(s"$RESET$YELLOW${BOLD}マナ妖精が${increasingMana.toInt}マナを回復してくれました"),
        ) ::: afterEffect
      ).run(p).unsafeRunAsyncAndForget()
    }
  }

  private def getGiveAppleValue(playerdata: PlayerData): Int = {
    val i: Int = playerdata.level / 10
    val s = i * i
    //0になるなら1を返す (2乗がマイナスになることはない)
    Math.max(s, 1)
  }

  private def getMessage(messages: OrderedCollection[String], playerName: String): String = {
    import com.github.unchama.seichiassist.util.OrderedCollectionOps._
    messages
      .sample
      .replace(playerNameMacro, playerName + RESET)
  }
}

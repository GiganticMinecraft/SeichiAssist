package com.github.unchama.seichiassist.task

import com.github.unchama.contextualexecutor.builder.Result
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.listener.VotingFairyListener
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.{Bukkit, Sound}

object VotingFairyTask { //MinuteTaskRunnableから、妖精召喚中のプレイヤーを対象に毎分実行される
  def run(p: Player): Unit = {
    val playermap = SeichiAssist.playermap
    val uuid = p.getUniqueId
    val playerdata = playermap.apply(uuid)
    //マナ回復
    VotingFairyListener.regeneMana(p)
    //効果時間中か
    if (!Util.isVotingFairyPeriod(playerdata.votingFairyStartTime, playerdata.votingFairyEndTime)) {
      speak(p, "あっ、もうこんな時間だ！", false)
      speak(p, s"じゃーねー！${p.getName}", true)
      p.sendMessage(s"$RESET$YELLOW${BOLD}妖精はどこかへ行ってしまった")
      playerdata.usingVotingFairy = false
    }
  }

  def speak(p: Player, msg: String, b: Boolean): Unit = {
    if (b) playSe(p)
    p.sendMessage(s"$AQUA$BOLD<マナ妖精>$RESET$msg")
  }

  //妖精効果音
  private def playSe(p: Player): Unit = {
    p.playSound(p.getLocation, Sound.BLOCK_NOTE_PLING, 2.0f, 1.0f)
    // TODO: [[PlaySoundEffect]]
    Bukkit.getServer.getScheduler.runTaskLater(SeichiAssist.instance, () => {
      // TODO: Remove this nest
      def foo() = {
        p.playSound(p.getLocation, Sound.BLOCK_NOTE_PLING, 2.0f, 1.5f)
        Bukkit.getServer.getScheduler.runTaskLater(SeichiAssist.instance, () => p.playSound(p.getLocation, Sound.BLOCK_NOTE_PLING, 2.0f, 2.0f), 2)
      }

      foo()
    }, 2)
  }

  def dispToggleVFTimeZ(toggle: Int): Result[String, String] = toggle match {
    case 1 => Right("30分")
    case 2 => Right("1時間")
    case 3 => Right("1時間30分")
    case 4 => Right("2時間")
    case _ => Left("エラー")
  }

  @deprecated
  def dispToggleVFTime(toggle: Int): String = if (toggle == 1) "30分"
  else if (toggle == 2) "1時間"
  else if (toggle == 3) "1時間30分"
  else if (toggle == 4) "2時間"
  else "エラー"
}

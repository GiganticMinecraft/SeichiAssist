package com.github.unchama.seichiassist.task

import com.github.unchama.contextualexecutor.builder.Result
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.timer
import com.github.unchama.seichiassist.listener.VotingFairyListener
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{DelayEffect, SequentialEffect, TargetedEffect, UnfocusedEffect}
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.entity.Player

object VotingFairyTask {
  /**
   *   MinuteTaskRunnableから、妖精召喚中のプレイヤーを対象に毎分実行される
   */
  def run(p: Player): Unit = {
    val playermap = SeichiAssist.playermap
    val uuid = p.getUniqueId
    val playerdata = playermap.apply(uuid)
    //マナ回復
    VotingFairyListener.regeneMana(p)
    //効果時間中なら表示しない
    if (Util.isVotingFairyPeriod(playerdata.votingFairyStartTime, playerdata.votingFairyEndTime)) {
      return
    }

    SequentialEffect(
      speak("あっ、もうこんな時間だ！", false),
      speak(s"じゃーねー！${p.getName}", true),
      MessageEffect(s"$RESET$YELLOW${BOLD}妖精はどこかへ行ってしまった"),
      UnfocusedEffect {
        playerdata.usingVotingFairy = false
      }
    ).run(p).unsafeRunAsyncAndForget()
  }

  def speak(mes: String, playSound: Boolean): TargetedEffect[Player] = {
    SequentialEffect(
      if (playSound) {
        import com.github.unchama.concurrent.syntax._
        SequentialEffect(
          FocusedSoundEffect(Sound.BLOCK_NOTE_PLING, 2.0f, 1.0f),
          DelayEffect(2.ticks),
          FocusedSoundEffect(Sound.BLOCK_NOTE_PLING, 2.0f, 1.5f),
          DelayEffect(2.ticks),
          FocusedSoundEffect(Sound.BLOCK_NOTE_PLING, 2.0f, 2.0f),
        )
      } else {
        TargetedEffect.emptyEffect
      },
      MessageEffect(s"$AQUA$BOLD<マナ妖精>$RESET$mes")
    )
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

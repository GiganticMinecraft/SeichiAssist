package com.github.unchama.seichiassist.task

import cats.effect.{IO, SyncIO}
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.listener.VotingFairyListener
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import com.github.unchama.seichiassist.util.TimeUtils
import net.md_5.bungee.api.ChatColor
import org.bukkit.ChatColor._
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.{Bukkit, Sound}

object VotingFairyTask {
  // MinuteTaskRunnableから、妖精召喚中のプレイヤーを対象に毎分実行される
  def run(p: Player)(
    implicit manaApi: ManaApi[IO, SyncIO, Player],
    mineStackAPI: MineStackAPI[IO, Player, ItemStack]
  ): Unit = {
    val playermap = SeichiAssist.playermap
    val uuid = p.getUniqueId
    val playerdata = playermap.apply(uuid)
    // マナ回復
    VotingFairyListener.regeneMana(p)
    // 効果時間中か
    if (
      !TimeUtils.isVotingFairyPeriod(
        playerdata.votingFairyStartTime,
        playerdata.votingFairyEndTime
      )
    ) {
      speak(p, "あっ、もうこんな時間だ！", b = false)
      speak(p, s"じゃーねー！${p.getName}", b = true)
      p.sendMessage(s"$RESET$YELLOW${BOLD}妖精はどこかへ行ってしまった")
      playerdata.usingVotingFairy_$eq(false)
    }
  }

  def speak(p: Player, msg: String, b: Boolean): Unit = {
    if (b) playSe(p)
    p.sendMessage(s"${ChatColor.AQUA}${ChatColor.BOLD}<マナ妖精>${ChatColor.RESET}$msg")
  }

  // 妖精効果音
  private def playSe(p: Player): Unit = {
    p.playSound(p.getLocation, Sound.BLOCK_NOTE_PLING, 2.0f, 1.0f)
    Bukkit
      .getServer
      .getScheduler
      .runTaskLater(
        SeichiAssist.instance,
        (() => {
          p.playSound(p.getLocation, Sound.BLOCK_NOTE_PLING, 2.0f, 1.5f)
          Bukkit
            .getServer
            .getScheduler
            .runTaskLater(
              SeichiAssist.instance,
              (() => p.playSound(p.getLocation, Sound.BLOCK_NOTE_PLING, 2.0f, 2.0f)): Runnable,
              2
            )
        }): Runnable,
        2
      )
  }

  def dispToggleVFTime(toggle: Int): String = {
    if (toggle == 1) "30分"
    else if (toggle == 2) "1時間"
    else if (toggle == 3) "1時間30分"
    else if (toggle == 4) "2時間"
    else "エラー"
  }
}

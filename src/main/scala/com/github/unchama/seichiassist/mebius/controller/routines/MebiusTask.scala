package com.github.unchama.seichiassist.mebius.controller.routines

import java.util.UUID

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.mebius.controller.listeners.MebiusListener
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.{Bukkit, ChatColor, Sound}

import scala.util.Random

/**
 * 2分に1回呼び出される
 * 定型Tipsを喋ってsilence true or 喋らずsilence false
 * PlayerDataに実体
 *
 * コンストラクタはプレイヤー接続時に呼び出される
 *
 * @author CrossHearts
 */
class MebiusTask(val uuid: UUID) extends BukkitRunnable {
  private val p = Bukkit.getPlayer(uuid)

  {
    runTaskTimerAsynchronously(SeichiAssist.instance, 2400, 2400)
  }

  private var silence = false

  // 2分周期で呼び出される
  override def run(): Unit = {
    // 前回喋って2分経過によりお喋り解禁
    silence = false
  }

  // silence OFFかつ50%でmessageを喋って、silence trueにする
  def speak(message: String): Unit = {
    // 50%乱数
    val isSpeak = new Random().nextBoolean
    if (!silence && isSpeak) {
      // 引数のメッセージを表示
      val name = MebiusListener.getName(p.getInventory.getHelmet)
      playSe()
      p.sendMessage(s"${ChatColor.RESET}<$name${ChatColor.RESET}> $message")
      // 次タスクまでお喋り禁止
      silence = true
    }
  }

  // 喋る時の効果音
  // HARPちゃんは聞こえんのだよ…
  private def playSe() = {
    p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.0f)
    p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.0f)
    p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.0f)
    Bukkit.getServer.getScheduler.runTaskLater(SeichiAssist.instance, () => {
      p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f)
      p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f)
      p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f)
    }, 2)
  }

}

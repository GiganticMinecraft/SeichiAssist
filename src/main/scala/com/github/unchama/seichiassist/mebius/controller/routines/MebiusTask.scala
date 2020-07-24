package com.github.unchama.seichiassist.mebius.controller.routines

import java.util.UUID

import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import com.github.unchama.seichiassist.mebius.controller.listeners.MebiusListener
import com.github.unchama.seichiassist.mebius.domain.resources.{MebiusMessages, MebiusTalks}
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

    ItemStackMebiusCodec
      .decodeMebiusProperty(p.getInventory.getHelmet)
      .foreach { property =>
        val no = Random.nextInt(MebiusMessages.tips.size + 1)

        if (no == MebiusMessages.tips.size) {
          speak(MebiusTalks.at(property.level).mebiusMessage)
        } else {
          // tipsの中身を設定
          speak(MebiusMessages.tips(no))
        }
      }
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

  // 無条件で喋らせる
  def speakForce(message: String): Unit = {
    val name = MebiusListener.getName(p.getInventory.getHelmet)
    playSeForce()
    p.sendMessage(s"${ChatColor.RESET}<$name${ChatColor.RESET}> $message")
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

  // 強制時の効果音
  private def playSeForce() = {
    p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f)
    p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f)
    p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f)
    p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f)
    p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f)
    p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 1.5f)
    Bukkit.getServer.getScheduler.runTaskLater(SeichiAssist.instance, () => {
      p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f)
      p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f)
      p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f)
      p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f)
      p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f)
      p.playSound(p.getLocation, Sound.BLOCK_NOTE_HARP, 2.0f, 2.0f)
    }, 2)
  }
}

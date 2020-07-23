package com.github.unchama.seichiassist.mebius.controller.listeners

import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusMessages
import com.github.unchama.seichiassist.{MaterialSets, SeichiAssist}
import org.bukkit.ChatColor.RESET
import org.bukkit.Sound
import org.bukkit.entity.{Monster, Player}
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.{EntityDamageByEntityEvent, EntityDeathEvent}
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import scala.util.Random

class MebiusInteractionResponder extends Listener {

  // メッセージリストからランダムに取り出し、タグを置換する
  private def getMessage(messages: Set[String], str1: String, str2: String) = {
    var msg = messages.toList(Random.nextInt(messages.size))

    if (!str1.isEmpty) msg = msg.replace("[str1]", s"$str1$RESET")
    if (!str2.isEmpty) msg = msg.replace("[str2]", s"$str2$RESET")

    msg
  }

  /** Mebiusを装備しているか */
  private def isEquip(player: Player): Boolean = ItemStackMebiusCodec.isMebius(player.getInventory.getHelmet)

  // ダメージを受けた時
  @EventHandler def onDamage(event: EntityDamageByEntityEvent): Unit = {
    // プレイヤーがダメージを受けた場合
    event.getEntity match {
      case player: Player =>
        // プレイヤーがMebiusを装備していない場合は除外
        if (!isEquip(player)) return
        val mebius = player.getInventory.getHelmet
        // 耐久無限じゃない場合
        if (!mebius.getItemMeta.isUnbreakable) { // 耐久閾値を超えていたら破損警告
          val max = mebius.getType.getMaxDurability
          val dur = mebius.getDurability
          if (dur >= max - 10) {
            SeichiAssist.playermap(player.getUniqueId).mebius
              .speak(getMessage(MebiusMessages.onDamageBreaking, MebiusListener.getNickname(player).get, ""))
          }
        }
        // モンスターからダメージを受けた場合
        event.getDamager match {
          case monster: Monster =>
            // 対モンスターメッセージ
            SeichiAssist.playermap(player.getUniqueId).mebius
              .speak(getMessage(MebiusMessages.onDamageWarnEnemy, MebiusListener.getNickname(player).get, monster.getName))
          case _ =>
        }
      case _ =>
    }
  }

  // 壊れたとき
  @EventHandler def onBreak(event: PlayerItemBreakEvent): Unit = {
    val messages = MebiusMessages.onMebiusBreak
    val brokenItem = event.getBrokenItem

    ItemStackMebiusCodec
      .decodeMebiusProperty(brokenItem)
      .foreach { property =>
        val player = event.getPlayer
        SeichiAssist.playermap(event.getPlayer.getUniqueId).mebius
          .speak(getMessage(messages, property.ownerNickname.getOrElse(event.getPlayer.getDisplayName), ""))
        player.sendMessage(s"${MebiusListener.getName(brokenItem)}${RESET}が旅立ちました。")
        // エンドラが叫ぶ
        player.playSound(player.getLocation, Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 0.1f)
      }
  }

  // モンスターを倒した時
  @EventHandler def onKill(event: EntityDeathEvent): Unit = {
    val messages = MebiusMessages.onMonsterKill

    // プレイヤーがモンスターを倒した場合以外は除外
    val killedMonster = event.getEntity
    if (killedMonster == null) return

    val killerPlayer = killedMonster.getKiller
    if (killerPlayer == null) return

    if (!isEquip(killerPlayer)) return

    //もしモンスター名が取れなければ除外
    val killedMonsterName = killedMonster.getName
    if (killedMonsterName == "") return

    SeichiAssist.playermap(killerPlayer.getUniqueId).mebius
      .speak(getMessage(messages, MebiusListener.getNickname(killerPlayer).get, killedMonsterName))
  }

  /**
   * ブロックを破壊した時
   * 保護と重力値に問題無く、ブロックタイプがmateriallistに登録されていたらメッセージを送る。
   */
  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  def sendMebiusMessageOn(event: BlockBreakEvent): Unit = {
    if (!MaterialSets.materials.contains(event.getBlock.getType)) return

    val player = event.getPlayer
    if (isEquip(player)) {
      val message = getMessage(MebiusMessages.onBlockBreak, MebiusListener.getNickname(player).get, "")
      SeichiAssist.playermap(player.getUniqueId).mebius.speak(message)
    }
  }
}

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
  // TODO check owner

  // メッセージリストからランダムに取り出し、タグを置換する
  private def getMessage(messages: Set[String], str1: String, str2: String) = {
    var msg = messages.toList(Random.nextInt(messages.size))

    if (!str1.isEmpty) msg = msg.replace("[str1]", s"$str1$RESET")
    if (!str2.isEmpty) msg = msg.replace("[str2]", s"$str2$RESET")

    msg
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onDamage(event: EntityDamageByEntityEvent): Unit = {
    // プレイヤーがダメージを受けた場合
    event.getEntity match {
      case player: Player =>
        val helmet = player.getInventory.getHelmet
        val mebiusProperty = ItemStackMebiusCodec.decodeMebiusProperty(helmet).getOrElse(return)

        // 耐久閾値を超えていたら破損警告
        if (helmet.getDurability >= helmet.getType.getMaxDurability - 10) {
          SeichiAssist.playermap(player.getUniqueId).mebius
            .speak(getMessage(MebiusMessages.onDamageBreaking, mebiusProperty.ownerNickname, ""))
        }

        // モンスターからダメージを受けた場合
        event.getDamager match {
          case monster: Monster =>
            // 対モンスターメッセージ
            SeichiAssist.playermap(player.getUniqueId).mebius
              .speak(getMessage(MebiusMessages.onDamageWarnEnemy, mebiusProperty.ownerNickname, monster.getName))
          case _ =>
        }
      case _ =>
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onBreak(event: PlayerItemBreakEvent): Unit = {
    val brokenItem = event.getBrokenItem
    val player = event.getPlayer

    ItemStackMebiusCodec
      .decodeMebiusProperty(brokenItem)
      .foreach { property =>
        SeichiAssist.playermap(event.getPlayer.getUniqueId).mebius
          .speak(getMessage(MebiusMessages.onMebiusBreak, property.ownerNickname, ""))
        player.sendMessage(s"${MebiusListener.getName(brokenItem)}${RESET}が旅立ちました。")
        // エンドラが叫ぶ
        player.playSound(player.getLocation, Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 0.1f)
      }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onKill(event: EntityDeathEvent): Unit = {
    val messages = MebiusMessages.onMonsterKill

    // プレイヤーがモンスターを倒した場合以外は除外
    val killedMonster = event.getEntity
    if (killedMonster == null) return

    val killerPlayer = killedMonster.getKiller
    if (killerPlayer == null) return

    //もしモンスター名が取れなければ除外
    val killedMonsterName = killedMonster.getName
    if (killedMonsterName == "") return

    val mebiusProperty =
      ItemStackMebiusCodec
        .decodeMebiusProperty(killerPlayer.getInventory.getHelmet)
        .getOrElse(return)

    SeichiAssist.playermap(killerPlayer.getUniqueId).mebius
      .speak(getMessage(messages, mebiusProperty.ownerNickname, killedMonsterName))
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
  def sendMebiusMessageOn(event: BlockBreakEvent): Unit = {
    if (!MaterialSets.materials.contains(event.getBlock.getType)) return

    val player = event.getPlayer

    val mebiusProperty = ItemStackMebiusCodec
      .decodeMebiusProperty(player.getInventory.getHelmet)
      .getOrElse(return)

    SeichiAssist.playermap(player.getUniqueId).mebius
      .speak(getMessage(MebiusMessages.onBlockBreak, mebiusProperty.ownerNickname, ""))
  }
}

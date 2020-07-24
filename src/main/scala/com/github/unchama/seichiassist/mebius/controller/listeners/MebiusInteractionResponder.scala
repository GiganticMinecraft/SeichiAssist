package com.github.unchama.seichiassist.mebius.controller.listeners

import cats.effect.IO
import com.github.unchama.playerdatarepository.PlayerDataRepository
import com.github.unchama.seichiassist.MaterialSets
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusMessages
import com.github.unchama.seichiassist.mebius.domain.{MebiusSpeech, MebiusSpeechGateway, MebiusSpeechStrength}
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor.RESET
import org.bukkit.Sound
import org.bukkit.entity.{Monster, Player}
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.{EntityDamageByEntityEvent, EntityDeathEvent}
import org.bukkit.event.player.PlayerItemBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import scala.util.Random

class MebiusInteractionResponder(implicit gatewayRepository: PlayerDataRepository[MebiusSpeechGateway[IO]],
                                 effectEnvironment: SeichiAssistEffectEnvironment)
  extends Listener {
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onDamage(event: EntityDamageByEntityEvent): Unit = {
    // プレイヤーがダメージを受けた場合
    event.getEntity match {
      case player: Player =>
        val helmet = player.getInventory.getHelmet
        val mebiusProperty = ItemStackMebiusCodec
          .decodeMebiusProperty(helmet)
          .filter(ItemStackMebiusCodec.ownershipMatches(player))
          .getOrElse(return)

        val gateway = gatewayRepository(player)

        val messageProgram = if (helmet.getDurability >= helmet.getType.getMaxDurability - 10) {
          // 耐久閾値を超えていたら破損警告
          gateway.speak(
            mebiusProperty,
            MebiusSpeech(
              getMessage(MebiusMessages.onDamageBreaking, mebiusProperty.ownerNickname, ""),
              MebiusSpeechStrength.Medium
            )
          )
        } else event.getDamager match {
          case monster: Monster =>
            // モンスターからダメージを受けた場合の対モンスターメッセージ
            gateway.speak(
              mebiusProperty,
              MebiusSpeech(
                getMessage(MebiusMessages.onDamageWarnEnemy, mebiusProperty.ownerNickname, monster.getName),
                MebiusSpeechStrength.Medium
              )
            )
          case _ => IO.unit
        }

        effectEnvironment.runEffectAsync("プレーヤー被攻撃時のMebiusのメッセージを再生する", messageProgram)
      case _ =>
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onBreak(event: PlayerItemBreakEvent): Unit = {
    val brokenItem = event.getBrokenItem
    val player = event.getPlayer

    ItemStackMebiusCodec
      .decodeMebiusProperty(brokenItem)
      .filter(ItemStackMebiusCodec.ownershipMatches(player))
      .foreach { property =>
        val gateway = gatewayRepository(player)

        import cats.implicits._

        effectEnvironment.runEffectAsync(
          "Mebius破壊時のエフェクトを再生する",
          gateway.forceMakingSpeech(
            property,
            MebiusSpeech(
              getMessage(MebiusMessages.onMebiusBreak, property.ownerNickname, ""),
              MebiusSpeechStrength.Medium
            )
          ) >> SequentialEffect(
            MessageEffect(s"${ItemStackMebiusCodec.displayNameOfMaterializedItem(property)}${RESET}が旅立ちました。"),
            FocusedSoundEffect(Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 0.1f)
          ).run(player)
        )
      }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onKill(event: EntityDeathEvent): Unit = {
    // プレイヤーがモンスターを倒した場合以外は除外
    val killedMonster = event.getEntity
    val player = killedMonster.getKiller
    if (killedMonster == null || player == null) return

    //もしモンスター名が取れなければ除外
    val killedMonsterName = killedMonster.getName
    if (killedMonsterName == "") return

    val mebiusProperty =
      ItemStackMebiusCodec
        .decodeMebiusProperty(player.getInventory.getHelmet)
        .filter(ItemStackMebiusCodec.ownershipMatches(player))
        .getOrElse(return)

    val gateway = gatewayRepository(player)

    effectEnvironment.runEffectAsync(
      "モンスターを倒した際のMebiusのメッセージを再生する",
      gateway.speak(
        mebiusProperty,
        MebiusSpeech(
          getMessage(MebiusMessages.onMonsterKill, mebiusProperty.ownerNickname, killedMonsterName),
          MebiusSpeechStrength.Medium
        )
      )
    )
  }

  // メッセージリストからランダムに取り出し、タグを置換する
  // TODO 何らかのクラスに入れるべき
  private def getMessage(messages: Set[String], str1: String, str2: String) = {
    var msg = messages.toList(Random.nextInt(messages.size))

    if (!str1.isEmpty) msg = msg.replace("[str1]", s"$str1$RESET")
    if (!str2.isEmpty) msg = msg.replace("[str2]", s"$str2$RESET")

    msg
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
  def sendMebiusMessageOn(event: BlockBreakEvent): Unit = {
    if (!MaterialSets.materials.contains(event.getBlock.getType)) return

    val player = event.getPlayer

    val mebiusProperty = ItemStackMebiusCodec
      .decodeMebiusProperty(player.getInventory.getHelmet)
      .filter(ItemStackMebiusCodec.ownershipMatches(player))
      .getOrElse(return)

    val gateway = gatewayRepository(player)

    effectEnvironment.runEffectAsync(
      "ブロック破壊時のMebiusのメッセージを再生する",
      gateway.speak(
        mebiusProperty,
        MebiusSpeech(
          getMessage(MebiusMessages.onBlockBreak, mebiusProperty.ownerNickname, ""),
          MebiusSpeechStrength.Medium
        )
      )
    )
  }
}

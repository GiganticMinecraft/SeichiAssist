package com.github.unchama.seichiassist.mebius.bukkit.listeners

import cats.effect.IO
import com.github.unchama.playerdatarepository.PlayerDataRepository
import com.github.unchama.seichiassist.MaterialSets
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusMessages
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeech, MebiusSpeechStrength}
import com.github.unchama.seichiassist.mebius.service.MebiusSpeechService
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

class MebiusInteractionResponder(implicit serviceRepository: PlayerDataRepository[MebiusSpeechService[IO]],
                                 effectEnvironment: SeichiAssistEffectEnvironment)
  extends Listener {
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def onDamage(event: EntityDamageByEntityEvent): Unit = {
    // プレイヤーがダメージを受けた場合
    event.getEntity match {
      case player: Player =>
        val helmet = player.getInventory.getHelmet
        val mebiusProperty = BukkitMebiusItemStackCodec
          .decodeMebiusProperty(helmet)
          .filter(BukkitMebiusItemStackCodec.ownershipMatches(player))
          .getOrElse(return)

        val speechService = serviceRepository(player)

        val messageProgram = if (helmet.getDurability >= helmet.getType.getMaxDurability - 10) {
          MebiusMessages.onDamageBreaking.pickOne.flatMap { message =>
            // 耐久閾値を超えていたら破損警告
            speechService.tryMakingSpeech(
              mebiusProperty,
              MebiusSpeech(message.interpolate(mebiusProperty.ownerNickname), MebiusSpeechStrength.Medium)
            )
          }
        } else event.getDamager match {
          case monster: Monster =>
            // モンスターからダメージを受けた場合の対モンスターメッセージ
            MebiusMessages.onDamageWarnEnemy.pickOne.flatMap { message =>
              speechService.tryMakingSpeech(
                mebiusProperty,
                MebiusSpeech(
                  message.interpolate(mebiusProperty.ownerNickname, monster.getName), MebiusSpeechStrength.Medium
                )
              )
            }
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

    BukkitMebiusItemStackCodec
      .decodeMebiusProperty(brokenItem)
      .filter(BukkitMebiusItemStackCodec.ownershipMatches(player))
      .foreach { property =>
        val speechService = serviceRepository(player)

        import cats.implicits._

        effectEnvironment.runEffectAsync(
          "Mebius破壊時のエフェクトを再生する",
          MebiusMessages.onMebiusBreak.pickOne.flatMap { message =>
            speechService.makeSpeechIgnoringBlockage(
              property,
              MebiusSpeech(message.interpolate(property.ownerNickname), MebiusSpeechStrength.Medium)
            ) >> SequentialEffect(
              MessageEffect(s"${BukkitMebiusItemStackCodec.displayNameOfMaterializedItem(property)}${RESET}が旅立ちました。"),
              FocusedSoundEffect(Sound.ENTITY_ENDERDRAGON_DEATH, 1.0f, 0.1f)
            ).run(player)
          }
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
      BukkitMebiusItemStackCodec
        .decodeMebiusProperty(player.getInventory.getHelmet)
        .filter(BukkitMebiusItemStackCodec.ownershipMatches(player))
        .getOrElse(return)

    val speechService = serviceRepository(player)

    effectEnvironment.runEffectAsync(
      "モンスターを倒した際のMebiusのメッセージを再生する",
      MebiusMessages.onDamageWarnEnemy.pickOne.flatMap { message =>
        speechService.tryMakingSpeech(
          mebiusProperty,
          MebiusSpeech(
            message.interpolate(mebiusProperty.ownerNickname, killedMonsterName), MebiusSpeechStrength.Medium
          )
        )
      }
    )
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
  def onBlockBreak(event: BlockBreakEvent): Unit = {
    if (!MaterialSets.materials.contains(event.getBlock.getType)) return

    val player = event.getPlayer

    val mebiusProperty = BukkitMebiusItemStackCodec
      .decodeMebiusProperty(player.getInventory.getHelmet)
      .filter(BukkitMebiusItemStackCodec.ownershipMatches(player))
      .getOrElse(return)

    val speechService = serviceRepository(player)

    effectEnvironment.runEffectAsync(
      "ブロック破壊時のMebiusのメッセージを再生する",
      MebiusMessages.onBlockBreak.pickOne.flatMap { message =>
        speechService.tryMakingSpeech(
          mebiusProperty,
          MebiusSpeech(message.interpolate(mebiusProperty.ownerNickname), MebiusSpeechStrength.Medium)
        )
      }
    )
  }
}

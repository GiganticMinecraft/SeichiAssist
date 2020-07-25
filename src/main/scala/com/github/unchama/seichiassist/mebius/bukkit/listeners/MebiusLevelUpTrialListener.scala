package com.github.unchama.seichiassist.mebius.bukkit.listeners

import cats.effect.IO
import com.github.unchama.playerdatarepository.PlayerDataRepository
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.mebius.domain.message.PropertyModificationMessages
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusTalks
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeech, MebiusSpeechGateway, MebiusSpeechStrength}
import com.github.unchama.seichiassist.mebius.service.MebiusLevellingService
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class MebiusLevelUpTrialListener(implicit gatewayRepository: PlayerDataRepository[MebiusSpeechGateway[IO]],
                                 effectEnvironment: SeichiAssistEffectEnvironment,
                                 messages: PropertyModificationMessages) extends Listener {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def tryMebiusLevelUpOn(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer

    val oldMebiusProperty = BukkitMebiusItemStackCodec
      .decodeMebiusProperty(player.getInventory.getHelmet)
      .filter(BukkitMebiusItemStackCodec.ownershipMatches(player))
      .getOrElse(return)
    val newMebiusProperty = MebiusLevellingService.attemptLevelUp(oldMebiusProperty).unsafeRunSync()

    if (newMebiusProperty != oldMebiusProperty) {
      player.getInventory.setHelmet {
        BukkitMebiusItemStackCodec.materialize(newMebiusProperty, damageValue = 0)
      }

      import cats.implicits._
      effectEnvironment.runEffectAsync(
        "Mebiusのレベルアップ時の通知を行う",
        gatewayRepository(player).forceMakingSpeech(
          newMebiusProperty,
          MebiusSpeech(
            MebiusTalks.at(newMebiusProperty.level).mebiusMessage,
            MebiusSpeechStrength.Loud
          )
        ) >> MessageEffect(messages.onLevelUp(oldMebiusProperty, newMebiusProperty)).run(player)
      )
    }
  }

}

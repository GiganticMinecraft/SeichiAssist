package com.github.unchama.seichiassist.mebius.bukkit.listeners

import cats.effect.IO
import com.github.unchama.playerdatarepository.PlayerDataRepository
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.mebius.domain.message.PropertyModificationMessages
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusTalks
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeech, MebiusSpeechStrength}
import com.github.unchama.seichiassist.mebius.service.{MebiusLevellingService, MebiusSpeechService}
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class MebiusLevelUpTrialListener(implicit serviceRepository: PlayerDataRepository[MebiusSpeechService[IO]],
                                 effectEnvironment: SeichiAssistEffectEnvironment,
                                 messages: PropertyModificationMessages) extends Listener {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def tryMebiusLevelUpOn(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer

    val oldMebiusProperty =
      BukkitMebiusItemStackCodec.decodePropertyOfOwnedMebius(player)(player.getInventory.getHelmet).getOrElse(return)
    val newMebiusProperty = MebiusLevellingService.attemptLevelUp(oldMebiusProperty).unsafeRunSync()

    if (newMebiusProperty != oldMebiusProperty) {
      player.getInventory.setHelmet {
        BukkitMebiusItemStackCodec.materialize(newMebiusProperty, damageValue = 0)
      }

      import cats.implicits._
      effectEnvironment.runEffectAsync(
        "Mebiusのレベルアップ時の通知を行う",
        serviceRepository(player).makeSpeechIgnoringBlockage(
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

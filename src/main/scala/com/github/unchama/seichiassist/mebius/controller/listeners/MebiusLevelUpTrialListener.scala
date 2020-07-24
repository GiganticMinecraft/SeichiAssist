package com.github.unchama.seichiassist.mebius.controller.listeners

import cats.data.Kleisli
import cats.effect.IO
import com.github.unchama.playerdatarepository.PlayerDataRepository
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import com.github.unchama.seichiassist.mebius.controller.repository.SpeechGatewayRepository
import com.github.unchama.seichiassist.mebius.domain.resources.MebiusTalks
import com.github.unchama.seichiassist.mebius.domain.{MebiusSpeech, MebiusSpeechGateway, MebiusSpeechStrength, PropertyModificationMessages}
import com.github.unchama.seichiassist.mebius.service.MebiusLevellingService
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class MebiusLevelUpTrialListener(implicit gatewayRepository: PlayerDataRepository[MebiusSpeechGateway[IO]],
                                 effectEnvironment: SeichiAssistEffectEnvironment,
                                 messages: PropertyModificationMessages) extends Listener {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def tryMebiusLevelUpOn(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer

    val oldMebiusProperty = ItemStackMebiusCodec
      .decodeMebiusProperty(player.getInventory.getHelmet)
      .filter(ItemStackMebiusCodec.ownershipMatches(player))
      .getOrElse(return)
    val newMebiusProperty = MebiusLevellingService.attemptLevelUp(oldMebiusProperty).unsafeRunSync()

    if (newMebiusProperty != oldMebiusProperty) {
      player.getInventory.setHelmet {
        ItemStackMebiusCodec.materialize(newMebiusProperty, damageValue = 0)
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

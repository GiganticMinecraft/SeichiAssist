package com.github.unchama.seichiassist.subsystems.mebius.bukkit.listeners

import cats.effect.SyncIO
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.ManagedWorld._
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.subsystems.mebius.domain.message.PropertyModificationMessages
import com.github.unchama.seichiassist.subsystems.mebius.domain.resources.MebiusTalks
import com.github.unchama.seichiassist.subsystems.mebius.domain.speech.{
  MebiusSpeech,
  MebiusSpeechStrength
}
import com.github.unchama.seichiassist.subsystems.mebius.service.MebiusSpeechService
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class MebiusLevelUpTrialListener(
  implicit serviceRepository: PlayerDataRepository[MebiusSpeechService[SyncIO]],
  effectEnvironment: EffectEnvironment,
  messages: PropertyModificationMessages
) extends Listener {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def tryMebiusLevelUpOn(event: BlockBreakEvent): Unit = {
    val player = event.getPlayer

    if (!player.getWorld.isSeichi) return

    val oldMebiusProperty =
      BukkitMebiusItemStackCodec
        .decodePropertyOfOwnedMebius(player)(player.getInventory.getHelmet)
        .getOrElse(return)

    val newMebiusProperty = oldMebiusProperty.tryUpgradeByOneLevel[SyncIO].unsafeRunSync()

    if (newMebiusProperty != oldMebiusProperty) {
      player.getInventory.setHelmet {
        BukkitMebiusItemStackCodec.materialize(newMebiusProperty)
      }

      import cats.implicits._
      effectEnvironment.unsafeRunEffectAsync(
        "Mebiusのレベルアップ時の通知を行う",
        serviceRepository(player)
          .makeSpeechIgnoringBlockage(
            newMebiusProperty,
            MebiusSpeech(
              MebiusTalks.at(newMebiusProperty.level).mebiusMessage,
              MebiusSpeechStrength.Loud
            )
          )
          .toIO >> MessageEffect(messages.onLevelUp(oldMebiusProperty, newMebiusProperty))
          .run(player)
      )
    }
  }

}

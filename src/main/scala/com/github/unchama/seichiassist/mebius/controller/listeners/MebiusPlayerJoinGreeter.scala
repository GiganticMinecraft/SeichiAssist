package com.github.unchama.seichiassist.mebius.controller.listeners

import cats.data.Kleisli
import cats.effect.Effect
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import com.github.unchama.seichiassist.mebius.controller.repository.SpeechGatewayRepository
import com.github.unchama.seichiassist.mebius.domain.{MebiusSpeech, MebiusSpeechStrength}
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class MebiusPlayerJoinGreeter[F[_] : Effect](implicit effectEnvironment: SeichiAssistEffectEnvironment,
                                             speechGatewayRepository: SpeechGatewayRepository[Kleisli[F, Player, *]]
                                            ) extends Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  def onJoin(event: PlayerJoinEvent): Unit = {
    ItemStackMebiusCodec
      .decodeMebiusProperty(event.getPlayer.getInventory.getHelmet)
      .foreach { property =>
        effectEnvironment.runEffectAsync(
          "参加時のMebiusのメッセージを送信する",
          speechGatewayRepository(event.getPlayer)
            .tryMakingSpeech(
              property,
              MebiusSpeech(s"おかえり${property.ownerNickname}！待ってたよ！", MebiusSpeechStrength.Medium)
            )
            .run(event.getPlayer)
        )
      }
  }

}

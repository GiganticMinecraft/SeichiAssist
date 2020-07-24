package com.github.unchama.seichiassist.mebius.controller.listeners

import cats.effect.{Effect, IO}
import com.github.unchama.playerdatarepository.PlayerDataRepository
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import com.github.unchama.seichiassist.mebius.domain.{MebiusSpeech, MebiusSpeechGateway, MebiusSpeechStrength}
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

class MebiusPlayerJoinGreeter[F[_] : Effect](implicit effectEnvironment: SeichiAssistEffectEnvironment,
                                             speechGatewayRepository: PlayerDataRepository[MebiusSpeechGateway[IO]]
                                            ) extends Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  def onJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    ItemStackMebiusCodec
      .decodeMebiusProperty(player.getInventory.getHelmet)
      .filter(ItemStackMebiusCodec.ownershipMatches(player))
      .foreach { property =>
        effectEnvironment.runEffectAsync(
          "参加時のMebiusのメッセージを送信する",
          speechGatewayRepository(event.getPlayer)
            .tryMakingSpeech(
              property,
              MebiusSpeech(s"おかえり${property.ownerNickname}！待ってたよ！", MebiusSpeechStrength.Medium)
            )
        )
      }
  }

}

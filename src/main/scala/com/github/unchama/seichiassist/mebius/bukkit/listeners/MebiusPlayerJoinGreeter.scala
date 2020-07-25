package com.github.unchama.seichiassist.mebius.bukkit.listeners

import java.util.concurrent.TimeUnit

import cats.effect.{Effect, IO, Timer}
import com.github.unchama.playerdatarepository.PlayerDataRepository
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.mebius.domain.{MebiusSpeech, MebiusSpeechGateway, MebiusSpeechStrength}
import com.github.unchama.targetedeffect.DelayEffect
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import scala.concurrent.duration.FiniteDuration

class MebiusPlayerJoinGreeter[F[_] : Effect](implicit effectEnvironment: SeichiAssistEffectEnvironment,
                                             speechGatewayRepository: PlayerDataRepository[MebiusSpeechGateway[IO]],
                                             timer: Timer[IO]
                                            ) extends Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  def onJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    import cats.implicits._

    BukkitMebiusItemStackCodec
      .decodeMebiusProperty(player.getInventory.getHelmet)
      .filter(BukkitMebiusItemStackCodec.ownershipMatches(player))
      .foreach { property =>
        effectEnvironment.runEffectAsync(
          "参加時のMebiusのメッセージを送信する",
          DelayEffect(FiniteDuration(500, TimeUnit.MILLISECONDS)).run(player) >>
            speechGatewayRepository(player)
              .tryMakingSpeech(
                property,
                MebiusSpeech(s"おかえり${property.ownerNickname}！待ってたよ！", MebiusSpeechStrength.Medium)
              )
        )
      }
  }

}

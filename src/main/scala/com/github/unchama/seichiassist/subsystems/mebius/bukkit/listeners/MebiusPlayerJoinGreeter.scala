package com.github.unchama.seichiassist.subsystems.mebius.bukkit.listeners

import java.util.concurrent.TimeUnit

import cats.effect.{Effect, IO, SyncIO, Timer}
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.subsystems.mebius.domain.speech.{MebiusSpeech, MebiusSpeechStrength}
import com.github.unchama.seichiassist.subsystems.mebius.service.MebiusSpeechService
import com.github.unchama.targetedeffect.DelayEffect
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import scala.concurrent.duration.FiniteDuration

class MebiusPlayerJoinGreeter[F[_] : Effect](implicit effectEnvironment: EffectEnvironment,
                                             speechServiceRepository: PlayerDataRepository[MebiusSpeechService[SyncIO]],
                                             timer: Timer[IO]) extends Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  def onJoin(event: PlayerJoinEvent): Unit = {
    val player = event.getPlayer

    import cats.implicits._

    BukkitMebiusItemStackCodec
      .decodePropertyOfOwnedMebius(player)(player.getInventory.getHelmet)
      .foreach { property =>
        effectEnvironment.runEffectAsync(
          "参加時のMebiusのメッセージを送信する",
          DelayEffect(FiniteDuration(500, TimeUnit.MILLISECONDS)).run(player) >>
            speechServiceRepository(player)
              .makeSpeechIgnoringBlockage(
                property,
                MebiusSpeech(s"おかえり${property.ownerNickname}！待ってたよ！", MebiusSpeechStrength.Medium)
              ).toIO
        )
      }
  }

}

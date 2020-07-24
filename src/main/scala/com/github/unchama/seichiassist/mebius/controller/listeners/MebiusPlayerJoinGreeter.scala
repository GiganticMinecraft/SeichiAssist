package com.github.unchama.seichiassist.mebius.controller.listeners

import cats.effect.Effect
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.controller.codec.ItemStackMebiusCodec
import com.github.unchama.seichiassist.mebius.domain.{MebiusEffects, MebiusSpeechStrength}
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.event.player.PlayerJoinEvent

case class MebiusPlayerJoinGreeter[F[_] : MebiusEffects : Effect](implicit val effectEnvironment: SeichiAssistEffectEnvironment) extends Listener {
  private val F = implicitly[MebiusEffects[F]]

  @EventHandler(priority = EventPriority.MONITOR)
  def onJoin(event: PlayerJoinEvent): Unit = {
    ItemStackMebiusCodec
      .decodeMebiusProperty(event.getPlayer.getInventory.getHelmet)
      .foreach { property =>
        effectEnvironment.runEffectAsync(
          "参加時のMebiusのメッセージを送信する",
          F.speak(property, s"おかえり${property.ownerNickname}！待ってたよ！", MebiusSpeechStrength.Medium)
        )
      }
  }

}

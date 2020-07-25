package com.github.unchama.seichiassist.mebius.bukkit.listeners

import java.util.concurrent.TimeUnit

import cats.effect.{IO, Timer}
import com.github.unchama.playerdatarepository.PlayerDataRepository
import com.github.unchama.seichiassist.MaterialSets
import com.github.unchama.seichiassist.domain.unsafe.SeichiAssistEffectEnvironment
import com.github.unchama.seichiassist.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.mebius.domain.speech.{MebiusSpeech, MebiusSpeechGateway, MebiusSpeechStrength}
import com.github.unchama.seichiassist.mebius.service.MebiusDroppingService
import com.github.unchama.seichiassist.util.Util
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{DelayEffect, SequentialEffect}
import org.bukkit.ChatColor.{RED, RESET}
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}
import org.bukkit.{ChatColor, Sound}

import scala.concurrent.duration.FiniteDuration

class MebiusDropTrialListener(implicit gatewayRepository: PlayerDataRepository[MebiusSpeechGateway[IO]],
                              effectEnvironment: SeichiAssistEffectEnvironment,
                              ioTimer: Timer[IO]) extends Listener {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def tryMebiusDropOn(event: BlockBreakEvent): Unit = {
    if (!MaterialSets.materials.contains(event.getBlock.getType)) return

    val player = event.getPlayer

    val droppedMebiusProperty = MebiusDroppingService
      .tryForDrop(player.getPlayer.getName).unsafeRunSync()
      .getOrElse(return)

    val mebius = BukkitMebiusItemStackCodec.materialize(droppedMebiusProperty, damageValue = 0.toShort)

    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}おめでとうございます。採掘中にMEBIUSを発見しました。")
    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}MEBIUSはプレイヤーと共に成長するヘルメットです。")
    player.sendMessage(s"$RESET${ChatColor.YELLOW}${ChatColor.BOLD}あなただけのMEBIUSを育てましょう！")

    import cats.implicits._
    effectEnvironment.runEffectAsync(
      "Mebiusのドロップ時メッセージを再生する",
      gatewayRepository(player).forceMakingSpeech(
        droppedMebiusProperty,
        MebiusSpeech(
          s"こんにちは、${player.getName}$RESET。" +
            s"僕は${BukkitMebiusItemStackCodec.displayNameOfMaterializedItem(droppedMebiusProperty)}" +
            s"$RESET！これからよろしくね！",
          MebiusSpeechStrength.Loud
        )
      ) >> SequentialEffect(
        FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f),
        DelayEffect(FiniteDuration(500, TimeUnit.MILLISECONDS))
      ).run(player)
    )

    if (!Util.isPlayerInventoryFull(player)) {
      Util.addItem(player, mebius)
    } else {
      player.sendMessage(s"$RESET$RED${ChatColor.BOLD}所持しきれないためMEBIUSをドロップしました。")
      Util.dropItem(player, mebius)
    }
  }
}

package com.github.unchama.seichiassist.subsystems.mebius.bukkit.listeners

import cats.effect.{IO, SyncEffect, SyncIO, Timer}
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.seichiassist.ManagedWorld._
import com.github.unchama.seichiassist.MaterialSets
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.codec.BukkitMebiusItemStackCodec
import com.github.unchama.seichiassist.subsystems.mebius.domain.MebiusDrop
import com.github.unchama.seichiassist.subsystems.mebius.domain.speech.{
  MebiusSpeech,
  MebiusSpeechStrength
}
import com.github.unchama.seichiassist.subsystems.mebius.service.MebiusSpeechService
import com.github.unchama.seichiassist.subsystems.seasonalevents.api.ChristmasEventsAPI
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import com.github.unchama.targetedeffect.{DelayEffect, SequentialEffect}
import com.github.unchama.util.RandomEffect
import org.bukkit.ChatColor._
import org.bukkit.Sound
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.{EventHandler, EventPriority, Listener}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class MebiusDropTrialListener[G[_]: ChristmasEventsAPI: RandomEffect: SyncEffect](
  implicit serviceRepository: PlayerDataRepository[MebiusSpeechService[SyncIO]],
  effectEnvironment: EffectEnvironment,
  timer: Timer[IO]
) extends Listener {

  import cats.effect.implicits._
  import cats.implicits._

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  def tryMebiusDropOn(event: BlockBreakEvent): Unit = {
    if (!MaterialSets.materials.contains(event.getBlock.getType)) return

    val player = event.getPlayer

    if (!player.getWorld.isSeichi) return

    val droppedMebiusProperty = MebiusDrop
      .tryOnce[G](player.getName, player.getUniqueId.toString)
      .runSync[SyncIO]
      .unsafeRunSync()
      .getOrElse(
        return
      )

    val mebius =
      BukkitMebiusItemStackCodec.materialize(droppedMebiusProperty, damageValue = 0.toShort)

    player.sendMessage(s"$RESET$YELLOW${BOLD}おめでとうございます。採掘中にMEBIUSを発見しました。")
    player.sendMessage(s"$RESET$YELLOW${BOLD}MEBIUSはプレイヤーと共に成長するヘルメットです。")
    player.sendMessage(s"$RESET$YELLOW${BOLD}あなただけのMEBIUSを育てましょう！")

    effectEnvironment.unsafeRunEffectAsync(
      "Mebiusのドロップ時メッセージを再生する",
      serviceRepository(player)
        .makeSpeechIgnoringBlockage(
          droppedMebiusProperty,
          MebiusSpeech(
            s"こんにちは、${player.getName}$RESET。" +
              s"僕は${BukkitMebiusItemStackCodec.displayNameOfMaterializedItem(droppedMebiusProperty)}" +
              s"$RESET！これからよろしくね！",
            MebiusSpeechStrength.Loud
          )
        )
        .toIO >> SequentialEffect(
        FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f),
        DelayEffect(FiniteDuration(500, TimeUnit.MILLISECONDS))
      ).run(player)
    )

    if (!InventoryOperations.isPlayerInventoryFull(player)) {
      InventoryOperations.addItem(player, mebius)
    } else {
      player.sendMessage(s"$RESET$RED${BOLD}所持しきれないためMEBIUSをドロップしました。")
      InventoryOperations.dropItem(player, mebius)
    }
  }
}

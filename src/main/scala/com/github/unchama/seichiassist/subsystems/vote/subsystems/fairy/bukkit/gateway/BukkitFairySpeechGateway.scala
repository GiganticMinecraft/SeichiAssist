package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.gateway

import cats.effect.{IO, SyncIO}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairySpeechGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor.{AQUA, BOLD, RESET}
import org.bukkit.Sound
import org.bukkit.entity.Player

class BukkitFairySpeechGateway(player: Player) extends FairySpeechGateway[SyncIO] {

  override def sendMessage(fairyMessage: FairyMessage): SyncIO[Unit] = {
    MessageEffect(s"$AQUA$BOLD<マナ妖精>$RESET${fairyMessage.message}")
      .run(player)
      .runAsync(_ => IO.unit)
  }

  override def playSpeechSound: SyncIO[Unit] = {
    FocusedSoundEffect(Sound.BLOCK_NOTE_PLING, 2.0f, 1.0f).run(player).runAsync(_ => IO.unit)
  }

}

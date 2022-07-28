package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.gateway

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairySpeechGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor.{AQUA, BOLD, RESET}
import org.bukkit.Sound
import org.bukkit.entity.Player

class BukkitFairySpeechGateway[G[_]: Sync](player: Player) extends FairySpeechGateway[G] {

  override def sendMessage(fairyMessage: FairyMessage): G[Unit] = Sync[G].delay {
    MessageEffect(s"$AQUA$BOLD<マナ妖精>$RESET${fairyMessage.message}").run(player).unsafeRunSync()
  }

  override def playSpeechSound: G[Unit] = Sync[G].delay {
    FocusedSoundEffect(Sound.BLOCK_NOTE_PLING, 2.0f, 1.0f).run(player).unsafeRunSync()
  }

}

package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.gateway

import cats.effect.{LiftIO, Sync}
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.FairySpeechGateway
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property.FairyMessage
import com.github.unchama.targetedeffect.commandsender.MessageEffectF
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor.{AQUA, BOLD, RESET}
import org.bukkit.Sound
import org.bukkit.entity.Player

class BukkitFairySpeechGateway[G[_]: Sync: LiftIO](player: Player)
    extends FairySpeechGateway[G] {

  override def sendMessage(fairyMessage: FairyMessage): G[Unit] =
    MessageEffectF[G](s"$AQUA$BOLD<マナ妖精>$RESET${fairyMessage.message}").run(player)

  override def playSpeechSound: G[Unit] =
    LiftIO[G].liftIO {
      FocusedSoundEffect(Sound.BLOCK_NOTE_PLING, 2.0f, 1.0f).run(player)
    }

}

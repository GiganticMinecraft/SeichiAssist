package com.github.unchama.seichiassist.subsystems.gachapoint.bukkit

import cats.effect.{IO, LiftIO}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories.BukkitGachaSkullData
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.GrantGachaTicketToAPlayer
import com.github.unchama.seichiassist.subsystems.playerheadskin.PlayerHeadSkinAPI
import com.github.unchama.seichiassist.util.InventoryOperations
import com.github.unchama.targetedeffect.SequentialEffect
import com.github.unchama.targetedeffect.TargetedEffect.emptyEffect
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import com.github.unchama.targetedeffect.player.FocusedSoundEffect
import org.bukkit.ChatColor.{GOLD, WHITE}
import org.bukkit.Sound
import org.bukkit.entity.Player

case class GrantBukkitGachaTicketToAPlayer[F[_]: LiftIO](player: Player)(
  implicit ioOnMainThread: OnMinecraftServerThread[IO],
  playerHeadSkinAPI: PlayerHeadSkinAPI[IO, Player]
) extends GrantGachaTicketToAPlayer[F] {

  override def give(count: Int): F[Unit] = {
    val effect =
      if (count > 0) {
        val itemToGive = BukkitGachaSkullData.gachaSkull
        val itemStacksToGive = Seq.fill(count)(itemToGive)

        SequentialEffect(
          InventoryOperations.grantItemStacksEffect(itemStacksToGive: _*),
          MessageEffect(s"${GOLD}ガチャ券${count}枚${WHITE}プレゼントフォーユー"),
          FocusedSoundEffect(Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f)
        )
      } else emptyEffect

    LiftIO[F].liftIO {
      effect(player)
    }
  }
}

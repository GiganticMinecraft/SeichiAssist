package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import cats.effect.{Async, Sync}
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.commands.legacy.GachaCommand
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{
  Gift,
  GiftItemInterpreter,
  GrantLevelUpGift
}
import org.bukkit.entity.Player

class BukkitGrantLevelUpGift[F[_]: Async: OnMinecraftServerThread]
    extends GrantLevelUpGift[F, Player] {
  override def grant(gift: Gift): Kleisli[F, Player, Unit] = {
    val giftItemInterpreter: GiftItemInterpreter[F] = new BukkitGiftItemInterpreter[F]
    gift match {
      case item: Gift.Item =>
        giftItemInterpreter(item)
      case Gift.AutomaticGachaRun =>
        Kleisli { player =>
          Sync[F].delay {
            GachaCommand.Gachagive(player, 1, player.getName)
          }
        }
    }
  }
}

object BukkitGrantLevelUpGift {

  implicit def apply[F[_]: Async: OnMinecraftServerThread]: GrantLevelUpGift[F, Player] =
    new BukkitGrantLevelUpGift[F]

}

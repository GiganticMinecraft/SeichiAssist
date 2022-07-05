package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import cats.effect.Sync
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.commands.legacy.GachaCommand
import com.github.unchama.seichiassist.subsystems.gachapoint.GachaPointApi
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{
  Gift,
  GiftItemInterpreter,
  GrantLevelUpGift
}
import org.bukkit.entity.Player

class BukkitGrantLevelUpGift[F[_]: Sync: OnMinecraftServerThread, G[_]: ContextCoercion[*[
  _
], F]]
    extends GrantLevelUpGift[F, Player, G] {
  override def grant(
    gift: Gift
  )(implicit gachaPointApi: GachaPointApi[F, G, Player]): Kleisli[F, Player, Unit] = {
    val giftItemInterpreter: GiftItemInterpreter[F, G] = new BukkitGiftItemInterpreter[F, G]
    gift match {
      case item: Gift.Item =>
        giftItemInterpreter(item, gachaPointApi)
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

  implicit def apply[F[_]: Sync: OnMinecraftServerThread, G[_]: ContextCoercion[*[_], F]]
    : GrantLevelUpGift[F, Player, G] =
    new BukkitGrantLevelUpGift[F, G]

}

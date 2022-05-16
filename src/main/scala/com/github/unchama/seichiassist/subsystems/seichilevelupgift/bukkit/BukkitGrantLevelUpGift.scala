package com.github.unchama.seichiassist.subsystems.seichilevelupgift.bukkit

import cats.data.Kleisli
import cats.effect.Sync
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.subsystems.seichilevelupgift.domain.{
  Gift,
  GiftItemInterpreter,
  GrantLevelUpGift
}
import org.bukkit.entity.Player

class BukkitGrantLevelUpGift[F[_]: Sync: OnMinecraftServerThread]
    extends GrantLevelUpGift[F, Player] {
  override def grant(gift: Gift): Kleisli[F, Player, Unit] = {
    val giftItemInterpreter: GiftItemInterpreter[F] = new BukkitGiftItemInterpreter[F]
    gift match {
      case item: Gift.Item =>
        giftItemInterpreter(item)
      case Gift.AutomaticGachaRun =>
        Kleisli { player =>
          Sync[F].delay {
            SeichiAssist.instance.gachaSystem.api.pull(player, 1).unsafeRunAsyncAndForget()
          }
        }
    }
  }
}

object BukkitGrantLevelUpGift {

  implicit def apply[F[_]: Sync: OnMinecraftServerThread]: GrantLevelUpGift[F, Player] =
    new BukkitGrantLevelUpGift[F]

}
